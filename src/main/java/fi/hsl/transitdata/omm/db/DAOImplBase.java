package fi.hsl.transitdata.omm.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DAOImplBase {
    static final Logger log = LoggerFactory.getLogger(DAOImplBase.class);

    protected Connection connection;

    DAOImplBase(Connection connection) {
        this.connection = connection;
    }

    protected ResultSet performQuery(PreparedStatement statement) throws SQLException {
        long queryStartTime = System.currentTimeMillis();
        ResultSet resultSet = statement.executeQuery();
        long elapsed = System.currentTimeMillis() - queryStartTime;
        log.info("Total query and processing time: {} ms", elapsed);
        return resultSet;
    }

    static String localDateAsString(String zoneId) {
        return localDateAsString(Instant.now(), zoneId);
    }

    static String localDatetimeAsString(String zoneId) {
        return localDatetimeAsString(Instant.now(), zoneId);
    }

    static String localDatetimeAsString(Instant instant, String zoneId) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(instant.atZone(ZoneId.of(zoneId)));
    }

    static String localDateAsString(Instant instant, String zoneId) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(instant.atZone(ZoneId.of(zoneId)));
    }
}
