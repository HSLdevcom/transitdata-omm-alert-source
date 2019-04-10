package fi.hsl.transitdata.omm.db;

import com.google.transit.realtime.GtfsRealtime;
import fi.hsl.transitdata.omm.models.Bulletin;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BulletinDAOMock implements BulletinDAO {
    List<Bulletin> mockData;

    BulletinDAOMock(List<Bulletin> data) {
        mockData = data;
    }

    @Override
    public List<Bulletin> getActiveBulletins() throws SQLException {
        return mockData;
    }


    public static Bulletin parseBulletinFromTsv(String line) {
        String[] split = line.split("\t");
        int index = 0;
        Bulletin b = new Bulletin();
        b.id = Long.parseLong(split[index++]);
        b.impact = Bulletin.Impact.fromString(split[index++]);
        b.category = Bulletin.Category.fromString(split[index++]);
        b.lastModified = DAOImplBase.parseOmmLocalDateTime(split[index++]);
        b.validFrom = DAOImplBase.parseNullableOmmLocalDateTime(split[index++]);
        b.validTo = DAOImplBase.parseNullableOmmLocalDateTime(split[index++]);
        b.affectsAllRoutes = Boolean.parseBoolean(split[index++]);
        b.affectsAllStops = Boolean.parseBoolean(split[index++]);
        b.affectedLineGids = BulletinDAOImpl.parseListFromCommaSeparatedString(split[index++]);
        b.affectedStopGids = BulletinDAOImpl.parseListFromCommaSeparatedString(split[index++]);
        Optional<Bulletin.Priority> maybePriority = Bulletin.Priority.fromInt(Integer.parseInt(split[index++]));
        if (maybePriority.isPresent()) {
            b.priority = maybePriority.get();
        }

        String titleFi = split[index++];
        String textFi = split[index++];

        String titleSv =  null;
        String textSv = null;
        if (index < split.length) {
            titleSv = split[index++];
            textSv = split[index++];
        }
        String titleEn = null;
        String textEn = null;
        if (index < split.length) {
            titleEn = split[index++];
            textEn = split[index++];
        }

        b.descriptions = createTranslatedString(textFi, textSv, textEn);
        b.headers = createTranslatedString(titleFi, titleSv, titleEn);
        return b;
    }



    private static GtfsRealtime.TranslatedString createTranslatedString(String fi, String sv, String en) {
        GtfsRealtime.TranslatedString.Builder builder = GtfsRealtime.TranslatedString.newBuilder();

        GtfsRealtime.TranslatedString.Translation translationFi = GtfsRealtime.TranslatedString.Translation.newBuilder()
                .setText(fi)
                .setLanguage(Bulletin.Language.fi.toString()).build();
        builder.addTranslation(translationFi);

        if (sv != null) {
            GtfsRealtime.TranslatedString.Translation translationSv = GtfsRealtime.TranslatedString.Translation.newBuilder()
                    .setText(sv)
                    .setLanguage(Bulletin.Language.sv.toString()).build();
            builder.addTranslation(translationSv);
        }
        if (en != null) {
            GtfsRealtime.TranslatedString.Translation translationEn = GtfsRealtime.TranslatedString.Translation.newBuilder()
                    .setText(en)
                    .setLanguage(Bulletin.Language.en.toString()).build();
            builder.addTranslation(translationEn);
        }

        return builder.build();
    }

    public static Bulletin newMockBulletin(long id) {
        Bulletin b = new Bulletin();
        b.id = id;
        b.category = Bulletin.Category.TRAFFIC_ACCIDENT;
        b.impact = Bulletin.Impact.DELAYED;
        return b;
    }

    public static BulletinDAOMock newMockDAO(List<Long> ids) {
        List<Bulletin> mocks = ids.stream()
                .map(BulletinDAOMock::newMockBulletin)
                .collect(Collectors.toList());
        return new BulletinDAOMock(mocks);
    }

}
