package fi.hsl.transitdata.omm.db;

import com.typesafe.config.Config;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OmmDbConnectorTest {
    private static final String DB_PASSWORD = "Test4321";

    @Rule
    public GenericContainer mssql = new GenericContainer(DockerImageName.parse("mcr.microsoft.com/mssql/server:2017-latest"))
            .withEnv("ACCEPT_EULA", "Y")
            .withEnv("MSSQL_SA_PASSWORD", DB_PASSWORD)
            .withEnv("MSSQL_PID", "Developer")
            .withExposedPorts(1433);

    private String connString;

    @Before
    public void setup() throws SQLException {
        connString = "jdbc:sqlserver://" + mssql.getHost() + ":" + mssql.getFirstMappedPort() + ";user=sa;password=" + DB_PASSWORD + ";";

        try (final Connection connection = DriverManager.getConnection(connString)) {
            connection.prepareStatement("CREATE DATABASE [ptDOI4_Community]").execute();

            connection.prepareStatement("CREATE TABLE [ptDOI4_Community].[dbo].[StopPoint] (Gid INTEGER, ExistsFromDate DATE, ExistsUptoDate DATE, IsJourneyPatternPointGid INTEGER)").execute();
            connection.prepareStatement("INSERT INTO [ptDOI4_Community].[dbo].[StopPoint] (Gid, ExistsFromDate, ExistsUptoDate, IsJourneyPatternPointGid) VALUES (1, '2000-01-01', '2100-01-01', 1)").execute();

            connection.prepareStatement("CREATE TABLE [ptDOI4_Community].[dbo].[JourneyPatternPoint] (Gid INTEGER, Number VARCHAR(255))").execute();
            connection.prepareStatement("INSERT INTO [ptDOI4_Community].[dbo].[JourneyPatternPoint] (Gid, Number) VALUES (1, '1')").execute();
        }
    }
    
    @Test
    public void testOmmDbConnector() throws SQLException {
        Config config = mock(Config.class);
        when(config.getString("omm.timezone")).thenReturn("UTC");
        when(config.getBoolean("omm.queryAllModifiedAlerts")).thenReturn(false);
        when(config.getString("omm.databaseSchema")).thenReturn("OMM_Community");
        when(config.getBoolean("pubtrans.devDatabase")).thenReturn(false);

        OmmDbConnector ommDbConnector = new OmmDbConnector(
                config, 10, connString, "OMM_Community", false);
        ommDbConnector.connect();
        int stopCount = ommDbConnector.getStopPointDAO().getAllStopPoints().size();

        assertEquals(1, stopCount);
    }
}
