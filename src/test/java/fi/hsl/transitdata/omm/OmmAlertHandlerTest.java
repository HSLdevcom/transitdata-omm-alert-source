package fi.hsl.transitdata.omm;

import com.google.transit.realtime.GtfsRealtime;
import fi.hsl.transitdata.omm.db.MockOmmConnector;
import fi.hsl.transitdata.omm.models.Bulletin;
import fi.hsl.transitdata.omm.models.Line;
import fi.hsl.transitdata.omm.models.StopPoint;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class OmmAlertHandlerTest {
    final static String TIMEZONE = "Europe/Helsinki";

    private MockOmmConnector readDefaultMockData() throws Exception {
        return MockOmmConnector.newInstance("2018_11_alert_dump.tsv");
    }

    private List<GtfsRealtime.FeedEntity> createFeedEntitiesFromDefaultMockData() throws Exception {
        MockOmmConnector connector = readDefaultMockData();
        List<Bulletin> bulletins = connector.getBulletinDAO().getActiveBulletins();
        Map<Long, Line> lines = connector.getLineDAO().getAllLines();
        Map<Long, StopPoint> stops = connector.getStopPointDAO().getAllStopPoints();

        List<GtfsRealtime.FeedEntity> feedEntities = OmmAlertHandler.createFeedEntities(bulletins, lines, stops, TIMEZONE);
        assertEquals(bulletins.size(), feedEntities.size());
        return feedEntities;
    }

    @Test
    public void testCreateFeedEntities() throws Exception {
        List<GtfsRealtime.FeedEntity> feedEntities = createFeedEntitiesFromDefaultMockData();

        feedEntities.forEach(entity -> {
            assertTrue(entity.hasAlert());
            assertTrue(entity.hasId());

            assertFalse(entity.hasTripUpdate());
            assertFalse(entity.hasVehicle());

            GtfsRealtime.Alert alert = entity.getAlert();
            assertTrue(alert.hasCause());
            assertTrue(alert.hasEffect());
            assertTrue(alert.hasDescriptionText());
            assertTrue(alert.hasHeaderText());

        });
    }

    @Test
    public void testCreateFeedMessage() throws Exception {
        List<GtfsRealtime.FeedEntity> feedEntities = createFeedEntitiesFromDefaultMockData();
        final long timestamp = System.currentTimeMillis() / 1000;
        GtfsRealtime.FeedMessage msg = OmmAlertHandler.createFeedMessage(feedEntities, timestamp);

        assertNotNull(msg);
        assertEquals(timestamp, msg.getHeader().getTimestamp());
        assertEquals(feedEntities.size(), msg.getEntityCount());
    }

}
