package fi.hsl.transitdata.omm.db;

import static com.google.transit.realtime.GtfsRealtime.*;
import fi.hsl.transitdata.omm.models.Bulletin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class BulletinDAOImpl extends DAOImplBase implements BulletinDAO {

    String queryString;
    String timezone;

    public BulletinDAOImpl(Connection connection, String timezone) {
        super(connection);
        this.timezone = timezone;
        queryString = createQuery();
    }

    @Override
    public List<Bulletin> getActiveBulletins() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(queryString)) {
            String now = localDatetimeAsString(timezone);
            statement.setString(1, now);

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
            bulletin.descriptions = parseDescriptions(resultSet);
            bulletin.headers = parseTitles(resultSet);

            bulletins.add(bulletin);
        }
        return bulletins;
    }

    static TranslatedString parseTitles(ResultSet resultSet) throws SQLException {
        return parseText(resultSet,"title_");
    }

    static TranslatedString parseDescriptions(ResultSet resultSet) throws SQLException {
        return parseText(resultSet,"text_");
    }

    private static TranslatedString parseText(final ResultSet resultSet, final String columnPrefix) throws SQLException {
        TranslatedString.Builder builder = TranslatedString.newBuilder();

        String[] suffixes = {Bulletin.Language.en.toString(), Bulletin.Language.fi.toString(), Bulletin.Language.sv.toString()};
        for (String language: suffixes) {
            String text = resultSet.getString(columnPrefix + language);
            if (text != null) {
                TranslatedString.Translation translation = TranslatedString.Translation.newBuilder()
                        .setText(text)
                        .setLanguage(language).build();

                builder.addTranslation(translation);
            }
        }
        return builder.build();
    }

    static List<Long> parseListFromCommaSeparatedString(String value) {
        if (value != null && !value.isEmpty()) {
            return Arrays.stream(value.split(","))
                    .map(subString -> Long.parseLong(subString.trim()))
                    .collect(Collectors.toList());
        } else {
            return new LinkedList<>();
        }
    }

    private String createQuery() {
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
                "    ,MAX(CASE WHEN BLM.language_code = 'fi' THEN BLM.title END) AS title_fi" +
                "    ,MAX(CASE WHEN BLM.language_code = 'fi' THEN BLM.description END) AS text_fi" +
                "    ,MAX(CASE WHEN BLM.language_code = 'sv' THEN BLM.title END) AS title_sv" +
                "    ,MAX(CASE WHEN BLM.language_code = 'sv' THEN BLM.description END) AS text_sv" +
                "    ,MAX(CASE WHEN BLM.language_code = 'en' THEN BLM.title END) AS title_en" +
                "    ,MAX(CASE WHEN BLM.language_code = 'en' THEN BLM.description END) AS text_en" +
                "  FROM [OMM_Community].[dbo].[bulletins] AS B" +
                "    LEFT JOIN [OMM_Community].[dbo].bulletin_localized_messages AS BLM ON BLM.bulletins_id = B.bulletins_id" +
                "    LEFT JOIN [OMM_Community].[dbo].passenger_bulletin_meta_data AS PBMD ON PBMD.bulletins_id = B.bulletins_id" +
                "  WHERE B.[type] = 'PASSENGER_INFORMATION' " +
                "    AND B.valid_to > ?" +
                "  GROUP BY B.bulletins_id" +
                "    ,B.category" +
                "    ,PBMD.impact" +
                "    ,B.last_modified" +
                "    ,B.valid_from" +
                "    ,B.valid_to" +
                "    ,B.affects_all_routes" +
                "    ,B.affects_all_stops" +
                "    ,B.affected_route_ids" +
                "    ,B.affected_stop_ids";
    }
}
