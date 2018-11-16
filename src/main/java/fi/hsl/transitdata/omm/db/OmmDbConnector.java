package fi.hsl.transitdata.omm.db;

import fi.hsl.common.pulsar.PulsarApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class OmmDbConnector {

    private static final Logger log = LoggerFactory.getLogger(OmmDbConnector.class);

    private final String timezone;

    private BulletinDAO bulletinDAO;
    private RouteDAO routeDAO;
    private StopDAO stopDAO;

    private OmmDbConnector(PulsarApplicationContext context, Connection connection) {
        timezone = context.getConfig().getString("omm.timezone");
        log.info("Using timezone " + timezone);

        bulletinDAO = new BulletinDAOImpl(connection, timezone);
        stopDAO = new StopDAOImpl(connection, timezone);
        routeDAO = new RouteDAOImpl(connection);
    }

    public static OmmDbConnector newInstance(PulsarApplicationContext context, String jdbcConnectionString) throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcConnectionString);
        return new OmmDbConnector(context, connection);
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

}
