package fi.hsl.transitdata.omm.db;

import fi.hsl.transitdata.omm.models.Line;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class LineDAOImpl extends DAOImplBase implements LineDAO {

    String queryString;

    public LineDAOImpl(Connection connection) {
        super(connection);
        queryString = createQuery();
    }

    @Override
    public List<Line> getAllLines() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(queryString)) {
            ResultSet results = performQuery(statement);
            return parseLines(results);
        }
        catch (Exception e) {
            log.error("Error while  querying and processing Lines", e);
            throw e;
        }
    }

    private List<Line> parseLines(ResultSet resultSet) throws SQLException {
        List<Line> lines = new LinkedList<>();
        while (resultSet.next()) {
            Line line = new Line();
            line.gid = resultSet.getLong("Gid");;
            line.lineId = resultSet.getString("StringValue");
            lines.add(line);
        }
        return lines;
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
