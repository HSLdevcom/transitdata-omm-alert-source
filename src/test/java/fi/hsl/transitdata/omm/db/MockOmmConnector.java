package fi.hsl.transitdata.omm.db;

import fi.hsl.transitdata.omm.models.Bulletin;
import fi.hsl.transitdata.omm.models.Line;
import fi.hsl.transitdata.omm.models.StopPoint;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MockOmmConnector {

    private BulletinDAO bulletinDAO;
    private LineDAO lineDAO;
    private StopPointDAO stopPointDAO;

    private String filename;

    private MockOmmConnector(String filename) {
        this.filename = filename;
    }

    public static MockOmmConnector newInstance(String filename) throws Exception {
        MockOmmConnector connector = new MockOmmConnector(filename);
        connector.initialize();
        return connector;
    }

    void initialize() throws Exception {
        List<String> content = readFileContentFromResources(filename);

        List<Bulletin> bulletins = parseBulletinsFromTsvContent(content);
        bulletinDAO = new BulletinDAOMock(bulletins);

        lineDAO = new LineDAOMock(bulletins);
        stopPointDAO = new StopPointDAOMock(bulletins);
    }


    private List<Bulletin> parseBulletinsFromTsvContent(List<String> lines) throws Exception {
        //We've used tabs as separators, because content includes commas.
        return lines.stream().map(BulletinDAOMock::parseBulletinFromTsv).collect(Collectors.toList());
    }

    private List<String> readFileContentFromResources(String filename) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource(filename);

        boolean first = true;
        List<String> out = new LinkedList<>();
        try (InputStream in = url.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                if (first) {
                    //Ignore title lines
                    first = false;
                    continue;
                }
                out.add(line);
            }
        }
        return out;
    }

    public BulletinDAO getBulletinDAO() {
        return bulletinDAO;
    }

    public static String lineGidToLineId(long gid) {
        return "line-" + Long.toString(gid);
    }

    public static String stopGidtoStopPointId(long gid) {
        return "stop-" + Long.toString(gid);
    }

    static class LineDAOMock implements LineDAO {
        Map<Long, Line> lines;
        LineDAOMock(List<Bulletin> bulletins) {
            lines = bulletins.stream()
                    .flatMap(bulletin -> bulletin.affectedLineGids.stream())
                    .collect(Collectors.toMap(
                            gid -> gid,
                            gid -> new Line(gid, lineGidToLineId(gid)),
                            (oldId, newId) -> oldId) //Merge by just throwing away duplicates
                    );
        }

        @Override
        public Map<Long, Line> getAllLines() throws SQLException {
            return lines;
        }
    }

    public LineDAO getLineDAO() {
        return lineDAO;
    }

    static class StopPointDAOMock implements StopPointDAO {
        Map<Long, StopPoint> stops;

        StopPointDAOMock(List<Bulletin> bulletins) {
            stops = bulletins.stream()
                    .flatMap(bulletin -> bulletin.affectedStopGids.stream())
                    .collect(Collectors.toMap(
                            gid -> gid,
                            gid -> new StopPoint(gid, stopGidtoStopPointId(gid)),
                            (oldId, newId) -> oldId) //Merge by just throwing away duplicates
                    );
        }

        @Override
        public Map<Long, StopPoint> getAllStopPoints() throws SQLException {
            return stops;
        }
    }

    public StopPointDAO getStopPointDAO() {
        return stopPointDAO;
    }

}
