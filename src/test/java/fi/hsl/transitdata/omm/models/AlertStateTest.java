package fi.hsl.transitdata.omm.models;

import fi.hsl.transitdata.omm.db.BulletinDAOMock;
import fi.hsl.transitdata.omm.db.MockOmmConnector;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

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

    @Test
    public void testEquals() throws Exception {
        MockOmmConnector connector = MockOmmConnector.newInstance("2018_11_alert_dump.tsv");
        List<Bulletin> bulletins = connector.getBulletinDAO().getActiveBulletins();

        AlertState first = new AlertState(bulletins);
        AlertState second = new AlertState(bulletins);

        assertEquals(first, second);

    }
}
