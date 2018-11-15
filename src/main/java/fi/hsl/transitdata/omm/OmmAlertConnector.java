package fi.hsl.transitdata.omm;

import fi.hsl.common.pulsar.PulsarApplicationContext;
import fi.hsl.transitdata.omm.db.*;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class OmmAlertConnector {

    private static final Logger log = LoggerFactory.getLogger(OmmAlertConnector.class);

    private final String timezone;

    private BulletinDAO bulletinDAO;
    private RouteDAO routeDAO;
    private StopDAO stopDAO;

    private OmmAlertConnector(PulsarApplicationContext context, Connection connection) {
        timezone = context.getConfig().getString("omm.timezone");
        log.info("Using timezone " + timezone);

        bulletinDAO = new BulletinDAOImpl(connection, timezone);
        stopDAO = new StopDAOImpl(connection, timezone);
        routeDAO = new RouteDAOImpl(connection);
    }

    public static OmmAlertConnector newInstance(PulsarApplicationContext context, String jdbcConnectionString) throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcConnectionString);
        return new OmmAlertConnector(context, connection);
    }

    public BulletinDAO getBulletinDAO() {
        return bulletinDAO;
    }

    public RouteDAO getRouteDAO() {
        return routeDAO;
    }

    public StopDAO getStopDAO() {
        return stopDAO;
    }

    /*
    public void queryAndProcessResults() throws SQLException, PulsarClientException {
        //Let's use Strings in the query since JDBC driver tends to convert timestamps automatically to local jvm time.
        String now = localDatetimeAsString(timezone);

        log.info("Querying results from database with timestamp {}", now);
        long queryStartTime = System.currentTimeMillis();

        log.trace("Running query " + queryString);

        try (PreparedStatement statement = dbConnection.prepareStatement(queryString)) {
            statement.setString(1, now);

            ResultSet resultSet = statement.executeQuery();
            handler.handleAndSend(resultSet);

            long elapsed = System.currentTimeMillis() - queryStartTime;
            log.info("Messages handled. Total query and processing time: {} ms", elapsed);
        }
        catch (Exception e) {
            log.error("Error while  querying and processing messages", e);
            throw e;
        }
    }*/

}
