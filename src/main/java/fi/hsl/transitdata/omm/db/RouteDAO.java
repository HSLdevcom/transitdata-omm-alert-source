package fi.hsl.transitdata.omm.db;

import fi.hsl.transitdata.omm.models.Route;

import java.sql.SQLException;
import java.util.List;

public interface RouteDAO {
    List<Route> getAllRoutes() throws SQLException;
}
