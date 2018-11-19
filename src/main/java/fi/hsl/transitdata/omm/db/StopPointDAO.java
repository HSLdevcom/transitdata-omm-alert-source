package fi.hsl.transitdata.omm.db;

import fi.hsl.transitdata.omm.models.StopPoint;

import java.sql.SQLException;
import java.util.List;

public interface StopPointDAO {
    List<StopPoint> getAllStopPoints() throws SQLException;
}
