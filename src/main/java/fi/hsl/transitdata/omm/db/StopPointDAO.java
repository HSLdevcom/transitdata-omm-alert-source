package fi.hsl.transitdata.omm.db;

import fi.hsl.transitdata.omm.models.StopPoint;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface StopPointDAO {
    Map<Long, List<StopPoint>> getAllStopPoints() throws SQLException;
}
