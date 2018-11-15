package fi.hsl.transitdata.omm.db;

import fi.hsl.transitdata.omm.models.Route;
import fi.hsl.transitdata.omm.models.Stop;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class StopDAOImpl extends DAOImplBase implements StopDAO {

    String queryString;
    String timezone;

    public StopDAOImpl(Connection connection, String timezone) {
        super(connection);
        queryString = createQuery();
        this.timezone = timezone;
    }

    @Override
    public List<Stop> getAllStops() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(queryString)) {
            statement.setString(1, localDateAsString(timezone));

            ResultSet results = performQuery(statement);
            return parseStops(results);
        }
        catch (Exception e) {
            log.error("Error while  querying and processing Routes", e);
            throw e;
        }
    }

    private List<Stop> parseStops(ResultSet resultSet) throws SQLException {
        List<Stop> stops = new LinkedList<>();
        while (resultSet.next()) {
            Stop stop = new Stop();
            stop.gid = resultSet.getLong("Gid");;
            stop.stopId = resultSet.getLong("Number");
            stops.add(stop);
        }
        return stops;
    }

    private String createQuery() {
        return "SELECT " +
                "    SP.Gid," +
                "    JPP.Number" +
                "  FROM [ptDOI4_Community].[dbo].[StopPoint] AS SP" +
                "  JOIN [ptDOI4_Community].[dbo].[JourneyPatternPoint] AS JPP ON JPP.Gid = SP.IsJourneyPatternPointGid" +
                "  WHERE SP.ExistsUptoDate IS NULL OR SP.ExistsUptoDate >= ?" +
                "  GROUP BY SP.Gid, JPP.Number" +
                "  ORDER BY SP.Gid";
    }

}
