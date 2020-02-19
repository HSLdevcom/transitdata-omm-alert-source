package fi.hsl.transitdata.omm.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DAOImplBase {
    static final Logger log = LoggerFactory.getLogger(DAOImplBase.class);

    protected Connection connection;

    public static final DateTimeFormatter OMM_DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

    static String pastLocalDatetimeAsString(String zoneId, int intervalSecs) {
        Instant pastNow = Instant.now().minusSeconds(intervalSecs);
        return localDatetimeAsString(pastNow, zoneId);
    }

    static String localDatetimeAsString(Instant instant, String zoneId) {
        return OMM_DT_FORMATTER.format(instant.atZone(ZoneId.of(zoneId)));
    }

    static String localDateAsString(Instant instant, String zoneId) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(instant.atZone(ZoneId.of(zoneId)));
    }

    public static LocalDateTime parseOmmLocalDateTime(String dt) {
        return LocalDateTime.parse(dt.replace(" ", "T")); // Make java.sql.Timestamp ISO compatible
    }

    static Optional<LocalDateTime> parseNullableOmmLocalDateTime(String dt) {
        if (dt != null) {
            return Optional.of(parseOmmLocalDateTime(dt));
        } else {
            return Optional.empty();
        }
    }
}
