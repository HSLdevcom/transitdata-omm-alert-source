package fi.hsl.transitdata.omm;

import com.google.transit.realtime.GtfsRealtime;
import fi.hsl.common.pulsar.PulsarApplicationContext;
import fi.hsl.common.transitdata.TransitdataProperties;
import fi.hsl.transitdata.omm.db.BulletinDAO;
import fi.hsl.transitdata.omm.db.RouteDAO;
import fi.hsl.transitdata.omm.db.StopDAO;
import fi.hsl.transitdata.omm.models.AlertState;
import fi.hsl.transitdata.omm.models.Bulletin;
import fi.hsl.transitdata.omm.models.Route;
import fi.hsl.transitdata.omm.models.Stop;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.transit.realtime.GtfsRealtime.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

public class OmmAlertHandler {
    static final Logger log = LoggerFactory.getLogger(OmmAlertHandler.class);

    String timeZone;
    private final Producer<byte[]> producer;
    private AlertState previousState = null;

    BulletinDAO bulletinDAO;
    RouteDAO routeDAO;
    StopDAO stopDAO;

    public OmmAlertHandler(PulsarApplicationContext context, BulletinDAO bulletinDAO, RouteDAO routeDAO, StopDAO stopDAO) {
        producer = context.getProducer();
        timeZone = context.getConfig().getString("omm.timezone");

        this.bulletinDAO = bulletinDAO;
        this.routeDAO = routeDAO;
        this.stopDAO = stopDAO;
    }

    public void pollAndSend() throws SQLException, PulsarClientException {
        List<Bulletin> bulletins = bulletinDAO.getActiveBulletins();
        AlertState latestState = new AlertState(bulletins);

        if (!latestState.equals(previousState)) {
            List<Route> routes = routeDAO.getAllRoutes();
            List<Stop> stops = stopDAO.getAllStops();

            GtfsRealtime.FeedMessage message = createFeedMessage(bulletins, routes, stops);

            final long timestamp = System.currentTimeMillis(); //TODO read from feedMessage?
            sendPulsarMessage(message, timestamp);
        }
        previousState = latestState;
    }


    FeedMessage createFeedMessage(List<Bulletin> bulletins, List<Route> routes, List<Stop> stops) {
        List<GtfsRealtime.FeedEntity> entities = createFeedEntities(bulletins, routes, stops);

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

    List<FeedEntity> createFeedEntities(final List<Bulletin> bulletins, final List<Route> routes, final List<Stop> stops) {
        return bulletins.stream().map(bulletin -> {

            final Alert alert = createAlert(bulletin, routes, stops);

            FeedEntity.Builder builder = FeedEntity.newBuilder();
            builder.setId(Long.toString(bulletin.id));
            builder.setAlert(alert);
            return builder.build();
        }).collect(Collectors.toList());
    }


    // TODO Refactor these time conversion methods to Commons
    public long toUtcEpochSecs(LocalDateTime localTimestamp) {
        return toUtcEpochSecs(localTimestamp, timeZone);
    }

    public static long toUtcEpochSecs(LocalDateTime localTimestamp, String zoneId) {
        ZoneId zone = ZoneId.of(zoneId);
        return localTimestamp.atZone(zone).toInstant().toEpochMilli() / 1000;
    }

    Alert createAlert(Bulletin bulletin, List<Route> routes, List<Stop> stops) {
        long startInUtcSecs = toUtcEpochSecs(bulletin.validFrom);
        long stopInUtcSecs = toUtcEpochSecs(bulletin.validTo);

        TimeRange timeRange = TimeRange.newBuilder()
                .setStart(startInUtcSecs)
                .setEnd(stopInUtcSecs)
                .build();

        //TODO logic for entity selection
        EntitySelector.Builder entityBuilder = EntitySelector.newBuilder()
                .setAgencyId("HSL"); //TODO get from somewhere
        if (!bulletin.affectsAllStops) {
            //TODO add stops
        }
        if (!bulletin.affectsAllRoutes) {
            //TODO add all routes
        }

        EntitySelector informedEntity = entityBuilder.build();

        Alert.Builder builder = Alert.newBuilder();
        builder.addActivePeriod(timeRange);
        builder.setCause(bulletin.category.toGtfsCause());
        builder.setDescriptionText(bulletin.descriptions);
        builder.setHeaderText(bulletin.headers);

        //builder.setEffect() //TODO add
        builder.addInformedEntity(informedEntity);

        return builder.build();
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
