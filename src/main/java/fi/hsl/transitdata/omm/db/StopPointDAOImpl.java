package fi.hsl.transitdata.omm.db;

import fi.hsl.transitdata.omm.models.StopPoint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class StopPointDAOImpl extends DAOImplBase implements StopPointDAO {

    String queryString;
    String timezone;

    public StopPointDAOImpl(Connection connection, String timezone) {
        super(connection);
        queryString = createQuery();
        this.timezone = timezone;
    }

    @Override
    public List<StopPoint> getAllStopPoints() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(queryString)) {
            statement.setString(1, localDateAsString(timezone));

            ResultSet results = performQuery(statement);
            return parseStopPoints(results);
        }
        catch (Exception e) {
            log.error("Error while  querying and processing StopPoints", e);
            throw e;
        }
    }

    private List<StopPoint> parseStopPoints(ResultSet resultSet) throws SQLException {
        List<StopPoint> stopPoints = new LinkedList<>();
        while (resultSet.next()) {
            StopPoint stopPoint = new StopPoint();
            stopPoint.gid = resultSet.getLong("Gid");;
            stopPoint.stopId = resultSet.getLong("Number");
            stopPoints.add(stopPoint);
        }
        return stopPoints;
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
