package fi.hsl.transitdata.omm;

import fi.hsl.common.pulsar.PulsarApplicationContext;
import fi.hsl.common.transitdata.TransitdataProperties;
import fi.hsl.common.transitdata.proto.InternalMessages;
import fi.hsl.transitdata.omm.db.BulletinDAO;
import fi.hsl.transitdata.omm.db.LineDAO;
import fi.hsl.transitdata.omm.db.OmmDbConnector;
import fi.hsl.transitdata.omm.db.StopPointDAO;
import fi.hsl.transitdata.omm.models.AlertState;
import fi.hsl.transitdata.omm.models.Bulletin;
import fi.hsl.transitdata.omm.models.Line;
import fi.hsl.transitdata.omm.models.StopPoint;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    OmmDbConnector ommConnector;

    public OmmAlertHandler(PulsarApplicationContext context, OmmDbConnector omm) {
        producer = context.getProducer();
        timeZone = context.getConfig().getString("omm.timezone");
        ommConnector = omm;
    }

    public void pollAndSend() throws SQLException, PulsarClientException, Exception {
        try {
            //For some reason the connection seem to be flaky, let's reconnect on each request.
            ommConnector.connect();

            BulletinDAO bulletinDAO = ommConnector.getBulletinDAO();
            LineDAO lineDAO = ommConnector.getLineDAO();
            StopPointDAO stopPointDAO = ommConnector.getStopPointDAO();

            List<Bulletin> bulletins = bulletinDAO.getActiveBulletins();
            AlertState latestState = new AlertState(bulletins);

            if (!latestState.equals(previousState)) {
                log.info("Service Alerts changed, creating new FeedMessage.");

                Map<Long, Line> lines = lineDAO.getAllLines();
                Map<Long, StopPoint> stopPoints = stopPointDAO.getAllStopPoints();

                final long timestampUtcMs = lastModifiedInUtcMs(latestState, timeZone);

                final InternalMessages.ServiceAlert alert = createServiceAlert(bulletins, lines, stopPoints, timeZone);
                sendPulsarMessage(alert, timestampUtcMs);
            } else {
                log.info("No changes to current Service Alerts.");
            }
            previousState = latestState;
        }
        finally {
            ommConnector.close();
        }
    }

    static long lastModifiedInUtcMs(AlertState state, String timezone) {
        // We want to keep Pulsar internal timestamps as accurate as possible (ms) but GTFS-RT expects seconds
        Optional<LocalDateTime> lastModified = state.lastModified();
        if (!lastModified.isPresent()) {
            log.error("Could not determine last modified timestamp from AlertState! Using currentTimeMillis().");
        }
        return lastModified.map(localDateTime -> toUtcEpochMs(localDateTime, timezone)).orElse(System.currentTimeMillis());
    }

    static InternalMessages.ServiceAlert createServiceAlert(final List<Bulletin> bulletins, final Map<Long, Line> lines, final Map<Long, StopPoint> stopPoints, final String timeZone) {
        final InternalMessages.ServiceAlert.Builder builder = InternalMessages.ServiceAlert.newBuilder();
        builder.setSchemaVersion(builder.getSchemaVersion());
        final List<InternalMessages.Bulletin> internalMessageBulletins = bulletins.stream()
                .map(bulletin -> createBulletin(bulletin, lines, stopPoints, timeZone))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        builder.addAllBulletins(internalMessageBulletins);
        return builder.build();
    }

    private static long toUtcEpochMs(final LocalDateTime localTimestamp, final String zoneId) {
        ZoneId zone = ZoneId.of(zoneId);
        return localTimestamp.atZone(zone).toInstant().toEpochMilli();
    }

    static Optional<InternalMessages.Bulletin> createBulletin(final Bulletin bulletin, final Map<Long, Line> lines, final Map<Long, StopPoint> stopPoints, final String timezone) {
        Optional<InternalMessages.Bulletin> maybeBulletin;
        try {
            final InternalMessages.Bulletin.Builder builder = InternalMessages.Bulletin.newBuilder();

            builder.setBulletinId(Long.toString(bulletin.id));
            builder.setCategory(bulletin.category.toCategory());

            long lastModifiedInUtcMs = toUtcEpochMs(bulletin.lastModified, timezone);
            builder.setLastModifiedUtcMs(lastModifiedInUtcMs);

            Optional<Long> startInUtcMs = bulletin.validFrom.map(from -> toUtcEpochMs(from, timezone));
            if (startInUtcMs.isPresent()) {
                builder.setValidFromUtcMs(startInUtcMs.get());
            } else {
                log.error("No start time specified for bulletin {}", bulletin.id);
            }

            Optional<Long> stopInUtcMs = bulletin.validTo.map(to -> toUtcEpochMs(to, timezone));
            if (stopInUtcMs.isPresent()) {
                builder.setValidToUtcMs(stopInUtcMs.get());
            } else {
                log.error("No end time specified for bulletin {}", bulletin.id);
            }

            builder.setAffectsAllRoutes(bulletin.affectsAllRoutes);
            builder.setAffectsAllStops(bulletin.affectsAllStops);
            builder.setImpact(bulletin.impact.toImpact());
            builder.setPriority(bulletin.priority.toPriority());

            builder.addAllTitles(bulletin.titles);
            builder.addAllDescriptions(bulletin.descriptions);
            builder.addAllUrls(bulletin.urls);

            List<InternalMessages.Bulletin.AffectedEntity> affectedRoutes = getAffectedRoutes(bulletin, lines);
            List<InternalMessages.Bulletin.AffectedEntity> affectedStops = getAffectedStops(bulletin, stopPoints);
            if (affectedRoutes.isEmpty() && affectedStops.isEmpty() && !bulletin.affectsAllRoutes && !bulletin.affectsAllStops) {
                log.error("Failed to find any Affected Entities for bulletin Id {}. Discarding alert.", bulletin.id);
                maybeBulletin = Optional.empty();
            }
            else {
                builder.addAllAffectedRoutes(affectedRoutes);
                builder.addAllAffectedStops(affectedStops);
                maybeBulletin = Optional.of(builder.build());
            }
        }
        catch (Exception e) {
            log.error("Exception while creating an alert!", e);
            maybeBulletin = Optional.empty();
        }
        return maybeBulletin;
    }

    static List<InternalMessages.Bulletin.AffectedEntity> getAffectedRoutes(final Bulletin bulletin, final Map<Long, Line> routes) {
        List<InternalMessages.Bulletin.AffectedEntity> affectedRoutes = new LinkedList<>();
        if (bulletin.affectedLineGids.size() > 0) {
            for (Long lineGid : bulletin.affectedLineGids) {
                Optional<Line> route = Optional.ofNullable(routes.get(lineGid));
                if (route.isPresent()) {
                    String lineId = route.get().lineId;
                    InternalMessages.Bulletin.AffectedEntity entity = InternalMessages.Bulletin.AffectedEntity.newBuilder()
                            .setEntityId(lineId).build();
                    affectedRoutes.add(entity);
                }
                else {
                    log.error("Failed to find line ID for line GID: {}", lineGid);
                }
            }
            log.debug("Found {} entity selectors for routes (should have been {})", affectedRoutes.size(), bulletin.affectedLineGids.size());
        }
        return affectedRoutes;
    }

    static List<InternalMessages.Bulletin.AffectedEntity> getAffectedStops(final Bulletin bulletin, final Map<Long, StopPoint> stops) {
        List<InternalMessages.Bulletin.AffectedEntity> affectedStops = new LinkedList<>();
        if (bulletin.affectedStopGids.size() > 0) {
            for (Long stopGid : bulletin.affectedStopGids) {
                Optional<StopPoint> stop = Optional.ofNullable(stops.get(stopGid));
                if (stop.isPresent()) {
                    String stopId = stop.get().stopId;
                    InternalMessages.Bulletin.AffectedEntity entity = InternalMessages.Bulletin.AffectedEntity.newBuilder()
                            .setEntityId(stopId).build();
                    affectedStops.add(entity);
                }
                else {
                    log.error("Failed to find stop ID for stop GID: {}", stopGid);
                }
            }
            log.debug("Found {} entity selectors for routes (should have been {})", affectedStops.size(), bulletin.affectedStopGids.size());
        }
        return affectedStops;
    }

    private void sendPulsarMessage(final InternalMessages.ServiceAlert message, long timestamp) throws PulsarClientException {
        try {
            producer.newMessage().value(message.toByteArray())
                    .eventTime(timestamp)
                    .property(TransitdataProperties.KEY_PROTOBUF_SCHEMA, TransitdataProperties.ProtobufSchema.TransitdataServiceAlert.toString())
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
