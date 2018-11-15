package fi.hsl.transitdata.omm.db;

import fi.hsl.transitdata.omm.models.Stop;

import java.sql.SQLException;
import java.util.List;

public interface StopDAO {
    List<Stop> getAllStops() throws SQLException;
}
