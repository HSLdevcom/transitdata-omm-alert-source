package fi.hsl.transitdata.omm.db;

import fi.hsl.common.transitdata.proto.InternalMessages;
import fi.hsl.transitdata.omm.models.Bulletin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class BulletinDAOImpl extends DAOImplBase implements BulletinDAO {

    private static final int fiveMinutesInSeconds = 60 * 5; //Added to give query some tolerance to work with.

    String queryString;
    String timezone;
    int pollIntervalInSeconds;
    boolean queryAllModifiedAlerts;

    public BulletinDAOImpl(Connection connection, String timezone, int pollIntervalInSeconds,
                           boolean queryAllModifiedAlerts, String databaseSchema) {
        super(connection);
        this.timezone = timezone;
        this.pollIntervalInSeconds = pollIntervalInSeconds;
        this.queryAllModifiedAlerts = queryAllModifiedAlerts;
        queryString = createQuery(queryAllModifiedAlerts, databaseSchema);
    }

    @Override
    public List<Bulletin> getActiveBulletins() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(queryString)) {
            String now = localDatetimeAsString(timezone);
            statement.setString(1, now);
            if (queryAllModifiedAlerts) {
                String pastNow = pastLocalDatetimeAsString(timezone, pollIntervalInSeconds + fiveMinutesInSeconds);
                statement.setString(2, pastNow);
            }

            ResultSet results = performQuery(statement);
            return parseBulletins(results);
        }
        catch (Exception e) {
            log.error("Error while  querying and processing Routes", e);
            throw e;
        }
    }

    private List<Bulletin> parseBulletins(ResultSet resultSet) throws SQLException {
        List<Bulletin> bulletins = new LinkedList<>();
        while (resultSet.next()) {
            try {
                Bulletin bulletin = new Bulletin();

                bulletin.id = resultSet.getLong("bulletins_id");
                log.debug("Handling bulletin id {}", bulletin.id);
                bulletin.category = Bulletin.Category.fromString(resultSet.getString("category"));
                bulletin.impact = Bulletin.Impact.fromString(resultSet.getString("impact"));
                bulletin.lastModified = parseOmmLocalDateTime(resultSet.getString("last_modified"));
                bulletin.validFrom = parseNullableOmmLocalDateTime(resultSet.getString("valid_from"));
                bulletin.validTo = parseNullableOmmLocalDateTime(resultSet.getString("valid_to"));
                bulletin.affectsAllRoutes = resultSet.getInt("affects_all_routes") > 0;
                bulletin.affectsAllStops = resultSet.getInt("affects_all_stops") > 0;
                bulletin.affectedLineGids = parseListFromCommaSeparatedString(resultSet.getString("affected_route_ids"));
                bulletin.affectedStopGids = parseListFromCommaSeparatedString(resultSet.getString("affected_stop_ids"));
                bulletin.titles = parseTitles(resultSet);
                bulletin.descriptions = parseDescriptions(resultSet);
                bulletin.urls = parseUrls(resultSet);
                bulletin.displayOnly = resultSet.getInt("display_only") > 0;

                final Integer priorityInt = resultSet.getInt("priority");
                Optional<Bulletin.Priority> maybePriority = Bulletin.Priority.fromInt(priorityInt);
                if (maybePriority.isPresent()) {
                    bulletin.priority = maybePriority.get();
                    bulletins.add(bulletin);
                } else {
                    // Do not handle bulletin if priority cannot be parsed
                    log.warn("Failed to parse priority {}. Bulletin: {}", priorityInt, bulletinToString(bulletin));
                }
            }
            catch (IllegalArgumentException iae) {
                log.error("Failed to create bulletin because of unexpected data: ", iae);
            }
        }
        return bulletins;
    }
    
    private static String bulletinToString(Bulletin bulletin) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ID=" + bulletin.id);
        stringBuilder.append(", CATEGORY=" + bulletin.category);
        stringBuilder.append(", VALID_FROM=" + bulletin.validFrom);
        stringBuilder.append(", VALID_TO=" + bulletin.validTo);
        
        if (bulletin.titles != null) {
            List<String> titlesList = bulletin.titles.stream().map(t -> t.getLanguage() + ": "
                    + t.getText()).collect(Collectors.toList());
            
            stringBuilder.append(", TITLES=" + titlesList);
        }
        
        if (bulletin.descriptions != null) {
            List<String> descriptionsList = bulletin.descriptions.stream().map(t -> t.getLanguage() + ": "
                    + t.getText()).collect(Collectors.toList());
            
            stringBuilder.append(", DESCRIPTIONS=" + descriptionsList);
        }
        
        return stringBuilder.toString();
    }

    static List<InternalMessages.Bulletin.Translation> parseTitles(ResultSet resultSet) throws SQLException {
        return parseText(resultSet,"title_");
    }

    static List<InternalMessages.Bulletin.Translation> parseDescriptions(ResultSet resultSet) throws SQLException {
        return parseText(resultSet,"text_");
    }

    static List<InternalMessages.Bulletin.Translation> parseUrls(ResultSet resultSet) throws SQLException {
        return parseText(resultSet,"url_").stream()
                .filter(translation -> translation.hasText() && !translation.getText().trim().isEmpty())
                .collect(Collectors.toList());
    }

    private static List<InternalMessages.Bulletin.Translation> parseText(final ResultSet resultSet, final String columnPrefix) throws SQLException {
        List<InternalMessages.Bulletin.Translation> translations = new ArrayList<>();

        String[] suffixes = {Bulletin.Language.en.toString(), Bulletin.Language.fi.toString(), Bulletin.Language.sv.toString()};
        for (String language: suffixes) {
            String text = resultSet.getString(columnPrefix + language);
            if (text != null) {
                InternalMessages.Bulletin.Translation translation = InternalMessages.Bulletin.Translation.newBuilder()
                        .setText(text)
                        .setLanguage(language).build();

                translations.add(translation);
            }
        }
        return translations;
    }

    static List<Long> parseListFromCommaSeparatedString(String value) {
        if (value != null && !value.isEmpty()) {
            return Arrays.stream(value.split(","))
                    .map(subString -> Long.parseLong(subString.trim()))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private static String createQuery(boolean queryAllModifiedAlerts, String databaseSchema) {
        return "SELECT B.bulletins_id" +
                "    ,PBMD.impact" +
                "    ,B.category" +
                "    ,B.last_modified" +
                "    ,B.valid_from" +
                "    ,B.valid_to" +
                "    ,B.affects_all_routes" +
                "    ,B.affects_all_stops" +
                "    ,B.affected_route_ids" +
                "    ,B.affected_stop_ids" +
                "    ,PBMD.priority" +
                "    ,PBMD.display_only" +
                "    ,MAX(CASE WHEN BLM.language_code = 'fi' THEN BLM.title END) AS title_fi" +
                "    ,MAX(CASE WHEN BLM.language_code = 'sv' THEN BLM.title END) AS title_sv" +
                "    ,MAX(CASE WHEN BLM.language_code = 'en' THEN BLM.title END) AS title_en" +
                "    ,MAX(CASE WHEN BLM.language_code = 'fi' THEN BLM.description END) AS text_fi" +
                "    ,MAX(CASE WHEN BLM.language_code = 'sv' THEN BLM.description END) AS text_sv" +
                "    ,MAX(CASE WHEN BLM.language_code = 'en' THEN BLM.description END) AS text_en" +
                "    ,MAX(CASE WHEN BLM.language_code = 'fi' THEN BLM.url END) AS url_fi" +
                "    ,MAX(CASE WHEN BLM.language_code = 'sv' THEN BLM.url END) AS url_sv" +
                "    ,MAX(CASE WHEN BLM.language_code = 'en' THEN BLM.url END) AS url_en" +
                "  FROM [" + databaseSchema + "].[dbo].[bulletins] AS B" +
                "    LEFT JOIN [" + databaseSchema + "].[dbo].bulletin_localized_messages AS BLM ON BLM.bulletins_id = B.bulletins_id" +
                "    LEFT JOIN [" + databaseSchema + "].[dbo].passenger_bulletin_meta_data AS PBMD ON PBMD.bulletins_id = B.bulletins_id" +
                "  WHERE B.[type] = 'PASSENGER_INFORMATION' " +
                "    AND B.valid_to > ?" + (queryAllModifiedAlerts ? " OR B.last_modified > ?" : "") +
                "  GROUP BY B.bulletins_id" +
                "    ,B.category" +
                "    ,PBMD.impact" +
                "    ,B.last_modified" +
                "    ,B.valid_from" +
                "    ,B.valid_to" +
                "    ,B.affects_all_routes" +
                "    ,B.affects_all_stops" +
                "    ,B.affected_route_ids" +
                "    ,B.affected_stop_ids" +
                "    ,PBMD.priority" +
                "    ,PBMD.display_only";
    }
}
