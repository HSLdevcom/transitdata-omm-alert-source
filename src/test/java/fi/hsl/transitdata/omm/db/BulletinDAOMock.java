package fi.hsl.transitdata.omm.db;

import com.google.transit.realtime.GtfsRealtime;
import fi.hsl.transitdata.omm.models.Bulletin;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class BulletinDAOMock implements BulletinDAO {
    List<Bulletin> mockData;

    BulletinDAOMock(List<Bulletin> data) {
        mockData = data;
    }

    @Override
    public List<Bulletin> getActiveBulletins() throws SQLException {
        return mockData;
    }

    public static Bulletin newMockBulletin(long id) {
        Bulletin b = new Bulletin();
        b.id = id;
        b.category = Bulletin.Category.TrafficAccident;
        b.impact = Bulletin.Impact.Delayed;
        /*b.lastModified =
        public Bulletin.Impact impact;
        public LocalDateTime lastModified;
        public LocalDateTime validFrom;
        public LocalDateTime validTo;
        public boolean affectsAllRoutes;
        public boolean affectsAllStops;
        public List<Long> affectedLineGids;
        public List<Long> affectedStopGids;
        public GtfsRealtime.TranslatedString descriptions;
        public GtfsRealtime.TranslatedString headers;*/
        return b;
    }

    public static BulletinDAOMock newMockDAO(List<Long> ids) {
        List<Bulletin> mocks = ids.stream()
                .map(BulletinDAOMock::newMockBulletin)
                .collect(Collectors.toList());
        return new BulletinDAOMock(mocks);
    }

}
