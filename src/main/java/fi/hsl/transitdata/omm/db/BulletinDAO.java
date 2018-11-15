package fi.hsl.transitdata.omm.db;

import fi.hsl.transitdata.omm.models.Bulletin;

import java.sql.SQLException;
import java.util.List;

public interface BulletinDAO {
    List<Bulletin> getActiveBulletins() throws SQLException;
}
