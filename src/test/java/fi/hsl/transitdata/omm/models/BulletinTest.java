package fi.hsl.transitdata.omm.models;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class BulletinTest {
    @Test
    public void testCategoryToEnum() {
        List<Bulletin.Category> all = Arrays.asList(Bulletin.Category.values());
        assertEquals(34, all.size());
        for (Bulletin.Category c: all) {
            assertNotNull(c.toCategory());
        }
    }

    @Test
    public void testImpactToEnum() {
        List<Bulletin.Impact> all = Arrays.asList(Bulletin.Impact.values());
        assertEquals(14, all.size());
        for (Bulletin.Impact i: all) {
            assertNotNull(i.toImpact());
        }
    }

    @Test
    public void testPriorityToEnum() {
        List<Bulletin.Priority> all = Arrays.asList(Bulletin.Priority.values());
        assertEquals(3, all.size());
        for (Bulletin.Priority i: all) {
            assertNotNull(i.toPriority());
        }
    }

    @Test
    public void testLanguageCodes() {
        Set<String> languageCodes = Arrays.stream(Bulletin.Language.values()).map(Bulletin.Language::toString).collect(Collectors.toSet());
        assertEquals(3, languageCodes.size());
        assertTrue(languageCodes.contains("fi"));
        assertTrue(languageCodes.contains("en"));
        assertTrue(languageCodes.contains("sv"));

    }
}
