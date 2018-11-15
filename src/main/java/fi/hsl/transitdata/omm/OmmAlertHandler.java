package fi.hsl.transitdata.omm;

import com.google.transit.realtime.GtfsRealtime;
import fi.hsl.common.pulsar.PulsarApplicationContext;
import fi.hsl.common.transitdata.TransitdataProperties;
import fi.hsl.common.transitdata.proto.InternalMessages;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

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


    GtfsRealtime.FeedMessage createFeedMessage(List<Bulletin> bulletins, List<Route> routes, List<Stop> stops) {
        List<GtfsRealtime.FeedEntity> entities = createFeedEntities(bulletins, routes, stops);
        return null;
    }

    static List<GtfsRealtime.FeedEntity> createFeedEntities(List<Bulletin> bulletins, List<Route> routes, List<Stop> stops) {
        return new LinkedList<>();
    }


    /*
    private static AlertState fetchAlerts(BulletinDAO bulletinDAO, RouteDAO routeDAO, StopDAO stopDAO) throws SQLException {
        List<Bulletin> bulletins = bulletinDAO.getActiveBulletins();
        List<Route> routes = routeDAO.getAllRoutes();
        List<Stop> stops = stopDAO.getAllStops();
        return handle
    }

    */

    private void sendPulsarMessage(GtfsRealtime.FeedMessage message, long timestamp) throws PulsarClientException {
        try {
            producer.newMessage().value(message.toByteArray())
                    .eventTime(timestamp)
                    .property(TransitdataProperties.KEY_PROTOBUF_SCHEMA, TransitdataProperties.ProtobufSchema.GTFS_ServiceAlert.toString())
                    .send();

            log.info("Produced a new alert");

        }
        catch (PulsarClientException pe) {
            log.error("Failed to send message to Pulsar", pe);
            throw pe;
        }
        catch (Exception e) {
            log.error("Failed to handle cancellation message", e);
        }
    }

}
