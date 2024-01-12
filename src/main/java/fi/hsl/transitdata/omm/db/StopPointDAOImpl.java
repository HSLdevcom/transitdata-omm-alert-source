package fi.hsl.transitdata.omm.db;

import fi.hsl.common.files.FileUtils;
import fi.hsl.transitdata.omm.models.StopPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class StopPointDAOImpl extends DAOImplBase implements StopPointDAO {
    
    private static final Logger log = LoggerFactory.getLogger(StopPointDAOImpl.class);

    String queryString;
    String timezone;
    
    boolean pubtransDev;

    public StopPointDAOImpl(Connection connection, String timezone, boolean pubtransDev) {
        super(connection);
        queryString = createQuery();
        this.timezone = timezone;
        this.pubtransDev = pubtransDev;
    }

    @Override
    public Map<Long, List<StopPoint>> getAllStopPoints() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(queryString)) {
            ResultSet results = performQuery(statement);
            return parseStopPoints(results);
        }
        catch (Exception e) {
            log.error("Error while  querying and processing StopPoints", e);
            throw e;
        }
    }

    private Map<Long, List<StopPoint>> parseStopPoints(ResultSet resultSet) throws SQLException {
        Map<Long, List<StopPoint>> stopPointsMap = new HashMap<>();
        while (resultSet.next()) {
            long stopGid = resultSet.getLong("Gid");
            String stopId = resultSet.getString("Number");
            String existsFromDate = resultSet.getString("ExistsFromDate");
            String existsUptoDate = resultSet.getString("ExistsUptoDate");
            StopPoint stopPoint = new StopPoint(stopGid, stopId, existsFromDate, existsUptoDate);
            // there may be multiple stopPoints with same gid
            if (!stopPointsMap.containsKey(stopGid)) {
                stopPointsMap.put(stopGid, new ArrayList<StopPoint>());
            }
            stopPointsMap.get(stopGid).add(stopPoint);
        }
        return stopPointsMap;
    }

    private String createQuery() {
        //String sqlFile = pubtransDev ? "/stop_points_all_dev.sql" : "/stop_points_all.sql";
        String sqlFile = "/stop_points_all_dev.sql";
        log.info("Using SQL file '{}'", sqlFile);
        InputStream stream = getClass().getResourceAsStream(sqlFile);
        try {
            return FileUtils.readFileFromStreamOrThrow(stream);
        } catch (Exception e) {
            log.error("Error in reading sql from file:", e);
            return null;
        }
    }

}
