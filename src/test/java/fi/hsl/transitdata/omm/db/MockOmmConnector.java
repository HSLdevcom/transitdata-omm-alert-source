package fi.hsl.transitdata.omm.db;

import fi.hsl.transitdata.omm.models.Bulletin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
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

    public LineDAO getLineDAO() {
        return lineDAO;
    }

    public StopPointDAO getStopPointDAO() {
        return stopPointDAO;
    }

}
