package fi.hsl.transitdata.omm.db;

import fi.hsl.common.files.FileUtils;
import fi.hsl.transitdata.omm.models.StopPoint;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class StopPointDAOImpl extends DAOImplBase implements StopPointDAO {

    String queryString;
    String timezone;

    public StopPointDAOImpl(Connection connection, String timezone) {
        super(connection);
        queryString = createQuery();
        this.timezone = timezone;
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
        InputStream stream = getClass().getResourceAsStream("/stop_points_all.sql");
        try {
            return FileUtils.readFileFromStreamOrThrow(stream);
        } catch (Exception e) {
            log.error("Error in reading sql from file:", e);
            return null;
        }
    }

}
