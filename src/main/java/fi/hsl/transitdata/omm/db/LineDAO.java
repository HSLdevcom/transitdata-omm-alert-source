package fi.hsl.transitdata.omm.db;

import fi.hsl.transitdata.omm.models.Line;

import java.sql.SQLException;
import java.util.Map;

public interface LineDAO {
    Map<Long, Line> getAllLines() throws SQLException;
}
