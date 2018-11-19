package fi.hsl.transitdata.omm.db;

import fi.hsl.transitdata.omm.models.Line;

import java.sql.SQLException;
import java.util.List;

public interface LineDAO {
    List<Line> getAllLines() throws SQLException;
}
