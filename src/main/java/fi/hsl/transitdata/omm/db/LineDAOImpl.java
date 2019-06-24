package fi.hsl.transitdata.omm.db;

import fi.hsl.transitdata.omm.models.Line;
import fi.hsl.transitdata.omm.models.Route;
import fi.hsl.common.files.FileUtils;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LineDAOImpl extends DAOImplBase implements LineDAO {

    String queryString;

    public LineDAOImpl(Connection connection) {
        super(connection);
        queryString = createQuery();
    }

    @Override
    public Map<Long, Line> getAllLines() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(queryString)) {
            ResultSet results = performQuery(statement);
            return parseLines(results);
        } catch (Exception e) {
            log.error("Error while  querying and processing Lines", e);
            throw e;
        }
    }

    private Map<Long, Line> parseLines(ResultSet resultSet) throws SQLException {
        Map<Long, Line> lines = new HashMap<>();
        while (resultSet.next()) {
            long lineGid = resultSet.getLong("Gid");
            String routeId = resultSet.getString("StringValue");
            Route route = new Route(lineGid, routeId);
            Line line;
            if (lines.containsKey(lineGid)) {
                line = lines.get(lineGid);
            } else {
                line = new Line(lineGid);
                lines.put(line.gid, line);
            }
            line.addRouteToLine(route);
        }
        return lines;
    }


    private String createQuery() {
        URL resource = getClass().getClassLoader().getResource("routes.sql");
        try {
            return FileUtils.readFileFromURLOrThrow(resource);
        } catch (Exception e) {
            log.error("Could not read query text file at " + resource, e);
            return null;
        }

    }

}
