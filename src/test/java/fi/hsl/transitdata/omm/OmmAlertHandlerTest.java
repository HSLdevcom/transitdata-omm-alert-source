package fi.hsl.transitdata.omm;

import fi.hsl.common.transitdata.proto.InternalMessages;
import fi.hsl.transitdata.omm.db.MockOmmConnector;
import fi.hsl.transitdata.omm.models.Bulletin;
import fi.hsl.transitdata.omm.models.Line;
import fi.hsl.transitdata.omm.models.StopPoint;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class OmmAlertHandlerTest {
    final static String TIMEZONE = "Europe/Helsinki";

    private MockOmmConnector readDefaultMockData() throws Exception {
        return MockOmmConnector.newInstance("2019_05_alert_dump.tsv");
    }

    private InternalMessages.ServiceAlert createServiceAlertFromDefaultMockData() throws Exception {
        MockOmmConnector connector = readDefaultMockData();
        List<Bulletin> bulletins = connector.getBulletinDAO().getActiveBulletins();
        Map<Long, Line> lines = connector.getLineDAO().getAllLines();
        Map<Long, List<StopPoint>> stops = connector.getStopPointDAO().getAllStopPoints();

        final InternalMessages.ServiceAlert alert = OmmAlertHandler.createServiceAlert(bulletins, lines, stops, TIMEZONE);
        assertEquals(bulletins.size(), alert.getBulletinsCount());
        validateMockDataFirstEntity(alert.getBulletinsList().get(0));
        return alert;
    }

    @Test
    public void testCreateServiceAlert() throws Exception {
        InternalMessages.ServiceAlert alert = createServiceAlertFromDefaultMockData();

        alert.getBulletinsList().forEach(bulletin -> {
            assertTrue(bulletin.hasBulletinId());
            assertTrue(bulletin.hasImpact());
            assertTrue(bulletin.hasCategory());
            assertTrue(bulletin.hasPriority());
            assertTrue(bulletin.hasLastModifiedUtcMs());
            assertTrue(bulletin.hasValidFromUtcMs());
            assertTrue(bulletin.hasValidToUtcMs());
            assertEquals(3, bulletin.getTitlesCount());
            assertEquals(3, bulletin.getDescriptionsCount());

            if (bulletin.getAffectedRoutesCount() > 0) {
                bulletin.getAffectedRoutesList().forEach(entity -> {
                    assertTrue(entity.hasEntityId());
                });
            }
            if (bulletin.getAffectedStopsCount() > 0) {
                bulletin.getAffectedStopsList().forEach(entity -> {
                    assertTrue(entity.hasEntityId());
                });
            }
        });
    }

    private void validateMockDataFirstEntity(final InternalMessages.Bulletin bulletin) {
        assertEquals("6340", bulletin.getBulletinId());
        assertTrue(bulletin.hasImpact());
        assertTrue(bulletin.hasCategory());
        assertTrue(bulletin.hasPriority());
        assertEquals(InternalMessages.Bulletin.Impact.POSSIBLE_DEVIATIONS, bulletin.getImpact());
        assertEquals(InternalMessages.Category.NO_TRAFFIC_DISRUPTION, bulletin.getCategory());
        assertEquals(InternalMessages.Bulletin.Priority.INFO, bulletin.getPriority());

        assertTrue(bulletin.hasAffectsAllRoutes());
        assertTrue(bulletin.hasAffectsAllStops());
        assertFalse(bulletin.getAffectsAllRoutes());
        assertFalse(bulletin.getAffectsAllStops());

        assertEquals(3, bulletin.getAffectedRoutesCount());
        assertEquals(0, bulletin.getAffectedStopsCount());

        InternalMessages.Bulletin.AffectedEntity route = bulletin.getAffectedRoutes(0);
        assertTrue(route.hasEntityId());
        assertEquals(MockOmmConnector.lineGidToRouteId(9011301065700000L), route.getEntityId());

        assertEquals(3, bulletin.getTitlesCount());
        assertEquals(3, bulletin.getDescriptionsCount());
    }

    @Ignore
    @Test
    public void testOneBulletinThoroughly() throws Exception {
        final MockOmmConnector connector = readDefaultMockData();
        final List<Bulletin> bulletins = connector.getBulletinDAO().getActiveBulletins();
        final Map<Long, Line> lines = connector.getLineDAO().getAllLines();
        final Map<Long, List<StopPoint>> stops = connector.getStopPointDAO().getAllStopPoints();

        final Optional<Bulletin> maybeSelectedBulletin = bulletins.stream().filter(b -> b.id == 6431).findFirst();
        assertTrue(maybeSelectedBulletin.isPresent());
        final Bulletin selectedBulletin = maybeSelectedBulletin.get();

        final Optional<InternalMessages.Bulletin> maybeBulletin = OmmAlertHandler.createBulletin(selectedBulletin, lines, stops, TIMEZONE);
        assertTrue(maybeBulletin.isPresent());
        final InternalMessages.Bulletin bulletin = maybeBulletin.get();

        assertEquals(InternalMessages.Bulletin.Impact.DISRUPTION_ROUTE, bulletin.getImpact());
        assertEquals(InternalMessages.Category.ROAD_CLOSED, bulletin.getCategory());
        assertEquals(InternalMessages.Bulletin.Priority.INFO, bulletin.getPriority());

        assertEquals(1557228675107L, bulletin.getLastModifiedUtcMs());
        assertEquals(1557885600000L, bulletin.getValidFromUtcMs());
        assertEquals(1558198800000L, bulletin.getValidToUtcMs());

        assertTrue(bulletin.hasAffectsAllRoutes());
        assertTrue(bulletin.hasAffectsAllStops());
        assertFalse(bulletin.getAffectsAllRoutes());
        assertFalse(bulletin.getAffectsAllStops());

        assertEquals(0, bulletin.getAffectedRoutesCount());
        assertEquals(5, bulletin.getAffectedStopsCount());

        long[] affectedStopGids = {
                9022301110755001L,
                9022301110755002L,
                9022301110754001L,
                9022301110792001L,
                9022301110793001L
        };
        for (int i = 0; i < bulletin.getAffectedStopsCount(); ++i) {
            final InternalMessages.Bulletin.AffectedEntity stop = bulletin.getAffectedStops(i);
            assertTrue(stop.hasEntityId());
            assertEquals(MockOmmConnector.stopGidtoStopPointId(affectedStopGids[i]), stop.getEntityId());
        }

        assertEquals(3, bulletin.getTitlesCount());
        bulletin.getTitlesList().forEach(title -> {
            switch (title.getLanguage()) {
                case "fi": assertEquals("Hämeentie suljettu 18.5. Arabian katufestivaalin ", title.getText());
                    break;
                case "sv": assertEquals("Gatan avstängd", title.getText());
                    break;
                case "en": assertEquals("Road closed", title.getText());
                    break;
                default: assertTrue(false);
            }
        });
        assertEquals(3, bulletin.getDescriptionsCount());
        bulletin.getDescriptionsList().forEach(description -> {
            switch (description.getLanguage()) {
                case "fi": assertEquals("Linjat 52, 55, 71, 78N ja 506 Arabiassa poikkeusreiteillä la 18.5. klo 9-20. /Info: hsl.fi.", description.getText());
                    break;
                case "sv": assertEquals("Linjerna 52, 55, 71, 78N och 506 kör avvikande rutter i Arabia 18.5 kl. 9-20. /Info: hsl.fi/sv", description.getText());
                    break;
                case "en": assertEquals("Buses 52, 55, 71, 78N and 506 diverted in Arabia on 18 May 9am-8pm. /Info: hsl.fi/en", description.getText());
                    break;
                default: assertTrue(false);
            }
        });
    }
}
