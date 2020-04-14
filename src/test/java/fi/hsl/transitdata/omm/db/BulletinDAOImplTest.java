package fi.hsl.transitdata.omm.db;

import fi.hsl.common.transitdata.proto.InternalMessages;
import org.junit.Test;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BulletinDAOImplTest {
    static final String MAX = Long.toString(Long.MAX_VALUE);

    @Test
    public void testSimpleCSVParsing() {
        List<Long> one = BulletinDAOImpl.parseListFromCommaSeparatedString(MAX);
        assertEquals(1, one.size());
        assertEquals((Long)Long.MAX_VALUE, one.get(0));

        List<Long> four = BulletinDAOImpl.parseListFromCommaSeparatedString("0,1,2,3");
        Long[] expectedFour = {0L, 1L, 2L, 3L};
        assertArrayEquals(expectedFour, four.toArray());
    }

    @Test
    public void testNullCSVParsing() {
        List<Long> empty = BulletinDAOImpl.parseListFromCommaSeparatedString("");
        assertEquals(0, empty.size());

        List<Long> nullInput = BulletinDAOImpl.parseListFromCommaSeparatedString(null);
        assertEquals(0, nullInput.size());
    }

    @Test
    public void testExtraCommaParsing() {
        List<Long> four = BulletinDAOImpl.parseListFromCommaSeparatedString("0,1,2,3,");
        Long[] expectedFour = {0L, 1L, 2L, 3L};
        assertArrayEquals(expectedFour, four.toArray());
    }

    @Test
    public void testParseUrlsFiltersEmpty() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("url_fi")).thenReturn("https://hsl.fi");
        when(resultSet.getString("url_sv")).thenReturn("https://hsl.fi/sv");
        when(resultSet.getString("url_en")).thenReturn("");

        List<InternalMessages.Bulletin.Translation> translations = BulletinDAOImpl.parseUrls(resultSet);
        assertEquals(2, translations.size());
        assertFalse(translations.stream().anyMatch(translation -> "en".equals(translation.getLanguage())));
    }
}
