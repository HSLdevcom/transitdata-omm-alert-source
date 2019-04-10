package fi.hsl.transitdata.omm;

import com.google.transit.realtime.GtfsRealtime;
import fi.hsl.common.gtfsrt.FeedMessageFactory;
import fi.hsl.transitdata.omm.db.MockOmmConnector;
import fi.hsl.transitdata.omm.models.AlertState;
import fi.hsl.transitdata.omm.models.Bulletin;
import fi.hsl.transitdata.omm.models.Line;
import fi.hsl.transitdata.omm.models.StopPoint;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class OmmAlertHandlerTest {
    final static String TIMEZONE = "Europe/Helsinki";

    private MockOmmConnector readDefaultMockData() throws Exception {
        return MockOmmConnector.newInstance("2019_04_alert_dump.tsv");
    }

    private List<GtfsRealtime.FeedEntity> createFeedEntitiesFromDefaultMockData() throws Exception {
        MockOmmConnector connector = readDefaultMockData();
        List<Bulletin> bulletins = connector.getBulletinDAO().getActiveBulletins();
        Map<Long, Line> lines = connector.getLineDAO().getAllLines();
        Map<Long, StopPoint> stops = connector.getStopPointDAO().getAllStopPoints();

        List<GtfsRealtime.FeedEntity> feedEntities = OmmAlertHandler.createFeedEntities(bulletins, lines, stops, TIMEZONE);
        assertEquals(bulletins.size(), feedEntities.size());
        validateMockDataFirstEntity(feedEntities.get(0));
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
            assertTrue(alert.hasSeverityLevel());

        });
    }

    @Test
    public void testCreateFeedMessage() throws Exception {
        List<GtfsRealtime.FeedEntity> feedEntities = createFeedEntitiesFromDefaultMockData();
        final long timestamp = System.currentTimeMillis() / 1000;

        GtfsRealtime.FeedMessage msg = FeedMessageFactory.createFullFeedMessage(feedEntities, timestamp);

        assertNotNull(msg);
        assertEquals(timestamp, msg.getHeader().getTimestamp());
        assertEquals(feedEntities.size(), msg.getEntityCount());

        validateMockDataFirstEntity(msg.getEntity(0));
    }

    private void validateMockDataFirstEntity(GtfsRealtime.FeedEntity entity) {
        assertTrue(entity.hasAlert());
        assertFalse(entity.hasTripUpdate());
        assertFalse(entity.hasVehicle());
        assertFalse(entity.hasIsDeleted());

        assertEquals("6237", entity.getId());
        GtfsRealtime.Alert alert = entity.getAlert();
        assertNotNull(alert);
        assertEquals(GtfsRealtime.Alert.Effect.OTHER_EFFECT, alert.getEffect());
        assertEquals(GtfsRealtime.Alert.Cause.OTHER_CAUSE, alert.getCause());
        assertEquals(1, alert.getActivePeriodCount());
        assertEquals(4, alert.getInformedEntityList().size());
        assertEquals(GtfsRealtime.Alert.SeverityLevel.INFO, alert.getSeverityLevel());

        GtfsRealtime.EntitySelector selector = alert.getInformedEntity(0);
        assertFalse(selector.hasTrip());
        assertFalse(selector.hasStopId());
        assertFalse(selector.hasAgencyId());
        assertTrue(selector.hasRouteId());
        assertEquals(MockOmmConnector.lineGidToLineId(9011301067600000L), selector.getRouteId());

        GtfsRealtime.TranslatedString header = alert.getHeaderText();
        assertEquals(3, header.getTranslationCount());
    }

    @Test
    public void testTimestampConversion() throws Exception {
        MockOmmConnector connector = readDefaultMockData();
        List<Bulletin> bulletins = connector.getBulletinDAO().getActiveBulletins();
        AlertState state = new AlertState(bulletins);
        long utcMs = OmmAlertHandler.lastModifiedInUtcMs(state, TIMEZONE);
        assertEquals(1554726084480L, utcMs);
    }

    @Test
    public void testOneFeedEntityThoroughly() throws Exception {
        MockOmmConnector connector = readDefaultMockData();
        List<Bulletin> bulletins = connector.getBulletinDAO().getActiveBulletins();
        Map<Long, Line> lines = connector.getLineDAO().getAllLines();
        Map<Long, StopPoint> stops = connector.getStopPointDAO().getAllStopPoints();

        List<GtfsRealtime.FeedEntity> feedEntities = OmmAlertHandler.createFeedEntities(bulletins, lines, stops, TIMEZONE);
        Optional<GtfsRealtime.FeedEntity> maybeEntity = feedEntities.stream().filter(entity -> entity.getId().equals("6298")).findFirst();
        assertTrue(maybeEntity.isPresent());

        GtfsRealtime.Alert alert = maybeEntity.get().getAlert();

        assertEquals(Bulletin.Category.NO_TRAFFIC_DISRUPTION.toGtfsCause(), alert.getCause());
        assertEquals(Bulletin.Impact.DEVIATING_SCHEDULE.toGtfsEffect(), alert.getEffect());
        assertEquals(1, alert.getActivePeriodCount());
        assertEquals( 1555304400L, alert.getActivePeriod(0).getStart());
        assertEquals( 1556625600L, alert.getActivePeriod(0).getEnd());
        assertEquals(2, alert.getInformedEntityCount());
        assertEquals(Bulletin.Priority.INFO.toGtfsSeverityLevel().get(), alert.getSeverityLevel());

        List<GtfsRealtime.EntitySelector> entities = alert.getInformedEntityList();
        validateEntitySelector(entities.get(0),  MockOmmConnector.lineGidToLineId(9011301004400000L));
        validateEntitySelector(entities.get(1),  MockOmmConnector.lineGidToLineId(9011301004500000L));

        GtfsRealtime.TranslatedString description = alert.getDescriptionText();
        assertEquals(3, description.getTranslationCount());
        description.getTranslationList().forEach(translation -> {
            switch (translation.getLanguage()) {
                case "fi": assertEquals("Korkeasaareen ajetaan lisälähtöjä linjoilla 16B ja 16X klo 8 – 15 ajalla 15.4. - 30.4. //Info: hsl.fi", translation.getText());
                    break;
                case "sv": assertEquals("Linjerna 16B och 16X kör extra avgångar till Högholmen kl. 8-15 under perioden 15.-30.4. //Info: hsl.fi.", translation.getText());
                    break;
                case "en": assertEquals("Bus routes 16B and 16X will provide additional service to Helsinki Zoo in Korkeasaari between 15 April and 30 April. //Info: hsl.fi.", translation.getText());
                    break;
                default: assertTrue(false);
            }
        });

        GtfsRealtime.TranslatedString header = alert.getHeaderText();
        assertEquals(3, header.getTranslationCount());
        header.getTranslationList().forEach(translation -> {
            switch (translation.getLanguage()) {
                case "fi": assertEquals("Korkeasaaren lisäliikenne 15.-30.4.", translation.getText());
                    break;
                case "sv": assertEquals("Inga trafikstörningar", translation.getText());
                    break;
                case "en": assertEquals("No traffic disruption", translation.getText());
                    break;
                default: assertTrue(false);
            }
        });

    }

    private void validateEntitySelector(GtfsRealtime.EntitySelector entity, String id) {
        assertFalse(entity.hasAgencyId());
        assertFalse(entity.hasStopId());
        assertTrue(entity.hasRouteId());
        assertFalse(entity.hasRouteType());
        assertFalse(entity.hasTrip());
        assertEquals(id, entity.getRouteId());
    }


}
