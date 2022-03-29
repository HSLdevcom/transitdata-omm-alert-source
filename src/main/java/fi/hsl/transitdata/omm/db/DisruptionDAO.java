package fi.hsl.transitdata.omm.db;

import fi.hsl.transitdata.omm.models.DisruptionRoute;

import java.sql.SQLException;
import java.util.List;

public interface DisruptionDAO {
    List<DisruptionRoute> getActiveDisruptions() throws SQLException;
}
