package fi.hsl.transitdata.omm;

import com.google.transit.realtime.GtfsRealtime;
import fi.hsl.common.pulsar.PulsarApplicationContext;
import fi.hsl.common.transitdata.TransitdataProperties;
import fi.hsl.transitdata.omm.db.BulletinDAO;
import fi.hsl.transitdata.omm.db.LineDAO;
import fi.hsl.transitdata.omm.db.StopPointDAO;
import fi.hsl.transitdata.omm.models.AlertState;
import fi.hsl.transitdata.omm.models.Bulletin;
import fi.hsl.transitdata.omm.models.Line;
import fi.hsl.transitdata.omm.models.StopPoint;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.transit.realtime.GtfsRealtime.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class OmmAlertHandler {

    public static final String AGENCY_ENTITY_SELECTOR = "HSL";

    static final Logger log = LoggerFactory.getLogger(OmmAlertHandler.class);

    String timeZone;
    private final Producer<byte[]> producer;
    private AlertState previousState = null;

    BulletinDAO bulletinDAO;
    LineDAO lineDAO;
    StopPointDAO stopPointDAO;

    public OmmAlertHandler(PulsarApplicationContext context, BulletinDAO bulletinDAO, LineDAO lineDAO, StopPointDAO stopPointDAO) {
        producer = context.getProducer();
        timeZone = context.getConfig().getString("omm.timezone");

        this.bulletinDAO = bulletinDAO;
        this.lineDAO = lineDAO;
        this.stopPointDAO = stopPointDAO;
    }

    public void pollAndSend() throws SQLException, PulsarClientException {
        List<Bulletin> bulletins = bulletinDAO.getActiveBulletins();
        AlertState latestState = new AlertState(bulletins);

        if (!latestState.equals(previousState)) {
            Map<Long, Line> lines = lineDAO.getAllLines();
            Map<Long, StopPoint> stopPoints = stopPointDAO.getAllStopPoints();

            GtfsRealtime.FeedMessage message = createFeedMessage(bulletins, lines, stopPoints);

            final long timestamp = System.currentTimeMillis(); //TODO read from feedMessage?
            sendPulsarMessage(message, timestamp);
        }
        previousState = latestState;
    }


    FeedMessage createFeedMessage(List<Bulletin> bulletins, Map<Long, Line> lines, Map<Long, StopPoint> stopPoints) {
        List<GtfsRealtime.FeedEntity> entities = createFeedEntities(bulletins, lines, stopPoints);

        //TODO define where to get the timestamp!?
        final long timestamp = System.currentTimeMillis() / 1000; //TODO: entities.stream().max(_.timestamp)
        GtfsRealtime.FeedHeader header = GtfsRealtime.FeedHeader.newBuilder()
                .setGtfsRealtimeVersion("2.0")
                .setIncrementality(FeedHeader.Incrementality.FULL_DATASET)
                .setTimestamp(timestamp)
                .build();

        return FeedMessage.newBuilder()
                .addAllEntity(entities)
                .setHeader(header)
                .build();
    }

    List<FeedEntity> createFeedEntities(final List<Bulletin> bulletins, final Map<Long, Line> lines, final Map<Long, StopPoint> stopPoints) {
        return bulletins.stream().map(bulletin -> {
            final Optional<Alert> maybeAlert = createAlert(bulletin, lines, stopPoints);
            return maybeAlert.map(alert -> {
                FeedEntity.Builder builder = FeedEntity.newBuilder();
                builder.setId(Long.toString(bulletin.id));
                builder.setAlert(alert);
                return builder.build();
            });
        }).filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toList());
    }


    // TODO Refactor these time conversion methods to Commons
    public long toUtcEpochSecs(LocalDateTime localTimestamp) {
        return toUtcEpochSecs(localTimestamp, timeZone);
    }

    public static long toUtcEpochSecs(LocalDateTime localTimestamp, String zoneId) {
        ZoneId zone = ZoneId.of(zoneId);
        return localTimestamp.atZone(zone).toInstant().toEpochMilli() / 1000;
    }

    Optional<Alert> createAlert(Bulletin bulletin, Map<Long, Line> lines, Map<Long, StopPoint> stopPoints) {
        Optional<Alert> maybeAlert = Optional.empty();
        try {
            long startInUtcSecs = toUtcEpochSecs(bulletin.validFrom);
            long stopInUtcSecs = toUtcEpochSecs(bulletin.validTo);

            TimeRange timeRange = TimeRange.newBuilder()
                    .setStart(startInUtcSecs)
                    .setEnd(stopInUtcSecs)
                    .build();

            final Alert.Builder builder = Alert.newBuilder();
            builder.addActivePeriod(timeRange);
            builder.setCause(bulletin.category.toGtfsCause());
            builder.setEffect(bulletin.impact.toGtfsEffect());
            builder.setDescriptionText(bulletin.descriptions);
            builder.setHeaderText(bulletin.headers);

            List<EntitySelector> entitySelectors = entitySelectorsForBulletin(bulletin, lines, stopPoints);
            if (entitySelectors.isEmpty()) {
                log.error("Failed to find any Informed Entities for bulletin Id {}. Discarding alert.", bulletin.id);
                maybeAlert = Optional.empty();
            }
            else {
                builder.addAllInformedEntity(entitySelectors);
                maybeAlert = Optional.of(builder.build());
            }
        }
        catch (Exception e) {
            log.error("Exception while creating an alert!", e);
            maybeAlert = Optional.empty();
        }
        return maybeAlert;
    }

    static List<EntitySelector> entitySelectorsForBulletin(Bulletin bulletin, Map<Long, Line> lines, Map<Long, StopPoint> stopPoints) {
        List<EntitySelector> selectors = new LinkedList<>();
        if (bulletin.affectsAllRoutes || bulletin.affectsAllStops) {
            log.debug("Bulletin {} affects all routes or stops", bulletin.id);

            EntitySelector agency = EntitySelector.newBuilder()
                    .setAgencyId(AGENCY_ENTITY_SELECTOR)
                    .build();
            selectors.add(agency);
        } else if (bulletin.affectedLineGids.size() > 0) {
            for (Long lineGid : bulletin.affectedLineGids) {
                Optional<Line> line = Optional.ofNullable(lines.get(lineGid));
                if (line.isPresent()) {
                    String lineId = line.get().lineId;
                    EntitySelector entity = EntitySelector.newBuilder()
                            .setRouteId(lineId).build();
                    selectors.add(entity);
                }
                else {
                    log.error("Failed to find line ID for line GID: {}", lineGid);
                }
            }
            log.debug("Found {} entity selectors for routes (should have been {})", selectors.size(), bulletin.affectedLineGids.size());
        } else if (bulletin.affectedStopGids.size() > 0) {
            for (Long stopGid : bulletin.affectedStopGids) {
                Optional<StopPoint> stop = Optional.ofNullable(stopPoints.get(stopGid));
                if (stop.isPresent()) {
                    String stopId = stop.get().stopId;
                    EntitySelector entity = EntitySelector.newBuilder()
                            .setStopId(stopId).build();
                    selectors.add(entity);
                }
                else {
                    log.error("Failed to find stop ID for stop GID: {}", stopGid);
                }
            }
            log.debug("Found {} entity selectors for routes (should have been {})", selectors.size(), bulletin.affectedStopGids.size());
        }

        return selectors;
    }

    private void sendPulsarMessage(GtfsRealtime.FeedMessage message, long timestamp) throws PulsarClientException {
        try {
            producer.newMessage().value(message.toByteArray())
                    .eventTime(timestamp)
                    .property(TransitdataProperties.KEY_PROTOBUF_SCHEMA, TransitdataProperties.ProtobufSchema.GTFS_ServiceAlert.toString())
                    .send();

            log.info("Produced a new alert with timestamp {}", timestamp);

        }
        catch (PulsarClientException pe) {
            log.error("Failed to send message to Pulsar", pe);
            throw pe;
        }
        catch (Exception e) {
            log.error("Failed to handle alert message", e);
        }
    }

}
