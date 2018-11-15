package fi.hsl.transitdata.omm.db;

import fi.hsl.transitdata.omm.models.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class RouteDAOImpl extends DAOImplBase implements RouteDAO {

    String queryString;

    public RouteDAOImpl(Connection connection) {
        super(connection);
        queryString = createQuery();
    }

    @Override
    public List<Route> getAllRoutes() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(queryString)) {
            ResultSet results = performQuery(statement);
            return parseRoutes(results);
        }
        catch (Exception e) {
            log.error("Error while  querying and processing Routes", e);
            throw e;
        }
    }

    private List<Route> parseRoutes(ResultSet resultSet) throws SQLException {
        List<Route> routes = new LinkedList<>();
        while (resultSet.next()) {
            Route route = new Route();
            route.gid = resultSet.getLong("Gid");;
            route.routeId = resultSet.getString("StringValue");
            routes.add(route);
        }
        return routes;
    }

    private String createQuery() {
        return "SELECT " +
                "  L.Gid," +
                "  KVV.StringValue" +
                "  FROM [ptDOI4_Community].[dbo].[Line] AS L" +
                "  JOIN [ptDOI4_Community].[L].[doi4_KeyVariantValue] AS KVV ON KVV.IsForObjectId = L.Id" +
                "  JOIN [ptDOI4_Community].[dbo].[KeyVariantType] AS KVT ON KVT.Id = KVV.IsOfKeyVariantTypeId" +
                "  JOIN [ptDOI4_Community].[dbo].[KeyType] AS KT ON KT.Id = KVT.IsForKeyTypeId" +
                "  JOIN [ptDOI4_Community].[dbo].[ObjectType] AS OT ON OT.Number = KT.ExtendsObjectTypeNumber" +
                "  WHERE KT.Name = 'JoreIdentity'" +
                "  GROUP BY L.Gid, KVV.StringValue" +
                "  ORDER BY L.Gid DESC";
    }

}
