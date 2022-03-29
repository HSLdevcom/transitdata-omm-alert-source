package fi.hsl.transitdata.omm.db;


import fi.hsl.common.files.FileUtils;
import fi.hsl.common.transitdata.proto.InternalMessages;
import fi.hsl.transitdata.omm.models.DisruptionRoute;
import fi.hsl.transitdata.omm.models.DisruptionRouteLink;
import fi.hsl.transitdata.omm.models.cancellations.Stop;

import java.sql.Connection;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DisruptionDAOImpl extends DAOImplBase implements DisruptionDAO {

    String queryString;
    String queryLinksString;
    String timezone;
    int pollIntervalInSeconds;
    boolean queryAllModifiedAlerts;

    public DisruptionDAOImpl(Connection connection, String timezone) {
        super(connection);
        this.timezone = timezone;
        this.pollIntervalInSeconds = pollIntervalInSeconds;
        this.queryAllModifiedAlerts = queryAllModifiedAlerts;
        queryString = createQuery("/disruption_routes.sql");
        queryLinksString = createQuery("/disruption_route_links.sql");
    }

    private List<DisruptionRoute> parseDisruptionRoutes(ResultSet resultSet, Map<String, Stop> stopsByGid) throws SQLException {
        HashMap<String, ArrayList<DisruptionRouteLink>> disruptionLinks = getActiveDisruptionLinks();
        List<DisruptionRoute> disruptionRoutes = new ArrayList<>();
        log.info("Processing disruptionRoutes resultset");
        while (resultSet.next()) {
            try {
                String disruptionRouteId = resultSet.getString("DISRUPTION_ROUTES_ID");
                ArrayList<DisruptionRouteLink> disruptionRouteLinks = new ArrayList<DisruptionRouteLink>();
                ArrayList<DisruptionRouteLink> disruptionRouteLinksByRouteId = disruptionLinks.get(disruptionRouteId);
                if (disruptionRouteLinksByRouteId != null) {
                    disruptionRouteLinks = disruptionRouteLinksByRouteId;
                }
                String startStopGid = resultSet.getString("START_STOP_ID");
                String startStopId = stopsByGid.containsKey(startStopGid) ? stopsByGid.get(startStopGid).stopId : "";
                String endStopGid = resultSet.getString("END_STOP_ID");
                String endStopId = stopsByGid.containsKey(startStopGid) ? stopsByGid.get(endStopGid).stopId : "";

                String affectedRoutes = resultSet.getString("AFFECTED_ROUTE_IDS");
                List<String> affectedRoutesList = Arrays.stream(affectedRoutes.split(",")).map(String::trim).collect(Collectors.toList());

                String validFrom = resultSet.getString("DC_VALID_FROM");
                String validTo = resultSet.getString("DC_VALID_TO");

                disruptionRoutes.add(new DisruptionRoute(disruptionRouteId, startStopId, endStopId, affectedRoutesList, validFrom, validTo, timezone, disruptionRouteLinks));

                String name = resultSet.getString("NAME");
                String description = resultSet.getString("DESCRIPTION");
                String type = resultSet.getString("DC_TYPE");
                log.info("Found disruption route with name: {}, description: {} and type: {}", name, description, type);
            } catch (IllegalArgumentException iae) {
                log.error("Error while parsing the disruptionRoutes resultset", iae);
            }
        }
        log.info("Found total {} disruption routes", disruptionRoutes.size());
        return disruptionRoutes;
    }

    private String createQuery(String query) {
        InputStream stream = getClass().getResourceAsStream(query);
        try {
            return FileUtils.readFileFromStreamOrThrow(stream);
        } catch (Exception e) {
            log.error("Error in reading sql from file:", e);
            return null;
        }
    }

    public static String localDateAsString(Instant instant, String zoneId) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(instant.atZone(ZoneId.of(zoneId)));
    }

    public HashMap<String, ArrayList<DisruptionRouteLink>> getActiveDisruptionLinks() throws SQLException {
        log.info("Querying disruption routes from database");
        List<DisruptionRouteLink> links = new ArrayList<>();
        HashMap<String, ArrayList<DisruptionRouteLink>> linksByRouteId = new HashMap<String, ArrayList<DisruptionRouteLink>>();
        String preparedString = queryLinksString;
        try (PreparedStatement statement = connection.prepareStatement(preparedString)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                    String id = resultSet.getString("disruption_routes_id");
                    String deviationId = resultSet.getString("deviation_case_id");
                    String startStopId = resultSet.getString("start_stop_id");
                    String endStopId = resultSet.getString("end_stop_id");
                    String sequenceNumber = resultSet.getString("link_location_sequence_number");
                    String latitude = resultSet.getString("latitude");
                    String longitude = resultSet.getString("longitude");
                    DisruptionRouteLink disruptionLink = new DisruptionRouteLink(id, deviationId, startStopId, endStopId, sequenceNumber, latitude, longitude);

                    ArrayList<DisruptionRouteLink> list = linksByRouteId.get(id);
                    if (list != null) {
                        list.add(disruptionLink);
                        linksByRouteId.replace(id, list);
                    } else {
                        ArrayList<DisruptionRouteLink> newList = new ArrayList<DisruptionRouteLink>();
                        newList.add(disruptionLink);
                        linksByRouteId.put(id, newList);
                    }
            }
        }
        catch (Exception e) {
            log.error("Error while  querying and processing messages", e);
            throw e;
        }
        return linksByRouteId;
    }

    @Override
    public List<DisruptionRoute> getActiveDisruptions() throws SQLException {
        log.info("Querying disruption route links from database");
        String dateFrom = localDateAsString(Instant.now(), timezone);
        String preparedString = queryString.replace("VAR_DATE_FROM", "1970-03-07");
        try (PreparedStatement statement = connection.prepareStatement(preparedString)) {
            ResultSet resultSet = statement.executeQuery();
            HashMap<String, Stop> stopsByGid = new HashMap<String, Stop>();
            return parseDisruptionRoutes(resultSet, stopsByGid);
        }
        catch (Exception e) {
            log.error("Error while  querying and processing messages", e);
            throw e;
        }
    }
}
