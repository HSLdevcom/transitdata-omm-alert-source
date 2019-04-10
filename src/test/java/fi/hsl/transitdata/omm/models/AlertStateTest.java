package fi.hsl.transitdata.omm.models;

import com.google.transit.realtime.GtfsRealtime;
import fi.hsl.transitdata.omm.OmmAlertHandler;
import fi.hsl.transitdata.omm.db.BulletinDAOMock;
import fi.hsl.transitdata.omm.db.DAOImplBase;
import fi.hsl.transitdata.omm.db.MockOmmConnector;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

import static org.junit.Assert.*;

public class AlertStateTest {

    List<Long> ids = Arrays.asList(1L, 2L, 3L);
    List<Long> idsInReverse = Arrays.asList(3L, 2L, 1L);

    @Test
    public void testSorting() throws Exception {
        List<Bulletin> ordered = BulletinDAOMock.newMockDAO(ids).getActiveBulletins();
        List<Bulletin> reversed = BulletinDAOMock.newMockDAO(idsInReverse).getActiveBulletins();

        assertNotEquals(ordered, reversed);
        assertEquals(ordered, AlertState.asSorted(reversed));
    }

    @Test
    public void testSortingReturnsNewList() throws Exception {
        List<Bulletin> ordered = BulletinDAOMock.newMockDAO(ids).getActiveBulletins();
        List<Bulletin> reversed = BulletinDAOMock.newMockDAO(idsInReverse).getActiveBulletins();

        List<Bulletin> sortedReversed = AlertState.asSorted(reversed);
        assertEquals(ordered, sortedReversed);

        assertEquals(reversed.size(), sortedReversed.size());
        reversed.remove(0);
        assertEquals(reversed.size() + 1, sortedReversed.size());

    }

    private List<Bulletin> readDefaultTestBulletins() throws Exception {
        MockOmmConnector connector = MockOmmConnector.newInstance("2019_04_alert_dump.tsv");
        return connector.getBulletinDAO().getActiveBulletins();
    }

    @Test
    public void testEqualsForSameLists() throws Exception {
        List<Bulletin> bulletins = readDefaultTestBulletins();
        AlertState first = new AlertState(bulletins);

        ArrayList<Bulletin> copy = new ArrayList<>(bulletins);
        AlertState second = new AlertState(copy);

        assertEquals(first, second);
    }

    @Test
    public void testEqualsForOrderingChanged() throws Exception {
        //We don't care about the order, just about the actual state.
        List<Bulletin> bulletins = readDefaultTestBulletins();
        AlertState first = new AlertState(bulletins);

        ArrayList<Bulletin> shuffled = new ArrayList<>(bulletins);
        Collections.shuffle(shuffled);
        ArrayList<Bulletin> copyOfShuffled = new ArrayList<>(shuffled);

        AlertState second = new AlertState(shuffled);

        assertNotEquals(bulletins, shuffled); //Ordering different -> Lists not equal
        assertEquals(first, second); // we don't care about ordering within the state
        assertEquals(shuffled, copyOfShuffled); // AlertState.equals should not change the underlying lists
    }

    @Test
    public void testEqualsWhenOneRemoved() throws Exception {
        List<Bulletin> bulletins = readDefaultTestBulletins();
        AlertState first = new AlertState(bulletins);

        ArrayList<Bulletin> copy = new ArrayList<>(bulletins);
        copy.remove(0);
        AlertState second = new AlertState(copy);

        assertNotEquals(first, second);
    }

    @Test
    public void testEqualsWhenSomethingChanged() throws Exception {
        List<Bulletin> firstBulletins = readDefaultTestBulletins();
        final AlertState first = new AlertState(firstBulletins);

        final List<Bulletin> secondBulletins = readDefaultTestBulletins();

        //Change fields one at the time and make sure each change gets noticed
        AlertState modified;

        modified = createModifiedAlertState(secondBulletins, bulletin -> {
            bulletin.impact = Bulletin.Impact.DISRUPTION_ROUTE;
            return bulletin;
        });
        assertNotEquals(first, modified);

        modified = createModifiedAlertState(secondBulletins, bulletin -> {
            bulletin.category = Bulletin.Category.ROAD_CLOSED;
            return bulletin;
        });
        assertNotEquals(first, modified);

        modified = createModifiedAlertState(secondBulletins, bulletin -> {
            bulletin.priority = Bulletin.Priority.SEVERE;
            return bulletin;
        });
        assertNotEquals(first, modified);

        modified = createModifiedAlertState(secondBulletins, bulletin -> {
            bulletin.id = 404L;
            return bulletin;
        });
        assertNotEquals(first, modified);

        modified = createModifiedAlertState(secondBulletins, bulletin -> {
            bulletin.lastModified = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
            return bulletin;
        });
        assertNotEquals(first, modified);

        modified = createModifiedAlertState(secondBulletins, bulletin -> {
            bulletin.validFrom = Optional.of(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
            return bulletin;
        });
        assertNotEquals(first, modified);

        modified = createModifiedAlertState(secondBulletins, bulletin -> {
            bulletin.validTo = Optional.of(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
            return bulletin;
        });
        assertNotEquals(first, modified);

        modified = createModifiedAlertState(secondBulletins, bulletin -> {
            bulletin.affectsAllRoutes = !bulletin.affectsAllRoutes;
            return bulletin;
        });
        assertNotEquals(first, modified);

        modified = createModifiedAlertState(secondBulletins, bulletin -> {
            bulletin.affectsAllStops = !bulletin.affectsAllStops;
            return bulletin;
        });
        assertNotEquals(first, modified);

        modified = createModifiedAlertState(secondBulletins, bulletin -> {
            bulletin.affectedStopGids.add(123456789L);
            return bulletin;
        });
        assertNotEquals(first, modified);

        modified = createModifiedAlertState(secondBulletins, bulletin -> {
            bulletin.affectedLineGids.add(987654321L);
            return bulletin;
        });
        assertNotEquals(first, modified);

        modified = createModifiedAlertState(secondBulletins, bulletin -> {
            GtfsRealtime.TranslatedString.Translation changedTranslation = bulletin.descriptions.getTranslation(0).toBuilder().setText("changing this").build();
            bulletin.descriptions = bulletin.descriptions.toBuilder().removeTranslation(0).addTranslation(0, changedTranslation).build();
            return bulletin;
        });
        assertNotEquals(first, modified);

        modified = createModifiedAlertState(secondBulletins, bulletin -> {
            GtfsRealtime.TranslatedString.Translation changedTranslation = bulletin.headers.getTranslation(0).toBuilder().setText("changing the header").build();
            bulletin.headers = bulletin.headers.toBuilder().removeTranslation(0).addTranslation(0, changedTranslation).build();
            return bulletin;
        });
        assertNotEquals(first, modified);

        //As last let's validate our test method. State should be equal if lambda does nothing
        AlertState unchanged = createModifiedAlertState(secondBulletins, bulletin -> bulletin);
        assertEquals(first, unchanged);

    }

    private AlertState createModifiedAlertState(final List<Bulletin> secondBulletins, Function<Bulletin, Bulletin> transformer) {
        final int indexToModify = 0;

        List<Bulletin> copyList = new ArrayList<>(secondBulletins);

        final Bulletin copyToModify = new Bulletin(copyList.get(indexToModify));
        Bulletin modified = transformer.apply(copyToModify);
        copyList.set(indexToModify, modified);

        return new AlertState(copyList);
    }

    @Test
    public void testLastModified() throws Exception {
        List<Bulletin> bulletins = readDefaultTestBulletins();
        AlertState state = new AlertState(bulletins);
        Optional<LocalDateTime> maybeDt = state.lastModified();
        assertTrue(maybeDt.isPresent());
        String dt = DAOImplBase.OMM_DT_FORMATTER.format(maybeDt.get());
        assertEquals("2019-04-08 15:21:24", dt);
    }
}
