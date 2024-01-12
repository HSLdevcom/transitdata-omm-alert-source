package fi.hsl.transitdata.omm.db;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class OmmDbConnector implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(OmmDbConnector.class);

    private final String timezone;
    private int pollIntervalInSeconds;
    private boolean queryAllModifiedAlerts;

    private BulletinDAO bulletinDAO;
    private LineDAO lineDAO;
    private StopPointDAO stopPointDAO;

    private Connection connection;
    private String connectionString;
    private String databaseSchema;

    public OmmDbConnector(Config config, int pollIntervalInSeconds, String jdbcConnectionString, String databaseSchema) {
        timezone = config.getString("omm.timezone");
        log.info("Using timezone " + timezone);
        queryAllModifiedAlerts = config.getBoolean("omm.queryAllModifiedAlerts");
        log.info("Set queryAllModifiedAlerts to: {}", queryAllModifiedAlerts);
        this.pollIntervalInSeconds = pollIntervalInSeconds;

        connectionString = jdbcConnectionString;
        this.databaseSchema = databaseSchema;
    }

    public void connect() throws SQLException {
        connection = DriverManager.getConnection(connectionString);
        bulletinDAO = new BulletinDAOImpl(
                connection, timezone, pollIntervalInSeconds, queryAllModifiedAlerts, databaseSchema);
        stopPointDAO = new StopPointDAOImpl(connection, timezone);
        lineDAO = new LineDAOImpl(connection);
    }

    @Override
    public void close() throws Exception {
        bulletinDAO = null;
        stopPointDAO = null;
        lineDAO = null;
        connection.close();

    }

    public BulletinDAO getBulletinDAO() {
        return bulletinDAO;
    }

    public LineDAO getLineDAO() {
        return lineDAO;
    }

    public StopPointDAO getStopPointDAO() {
        return stopPointDAO;
    }

}
