package fi.hsl.transitdata.omm.db;

import fi.hsl.common.transitdata.proto.InternalMessages;
import fi.hsl.transitdata.omm.models.Bulletin;

import java.sql.SQLException;
import java.util.ArrayList;
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
        String titleSv =  split[index++];
        String titleEn = split[index++];

        String textFi = split[index++];
        String textSv = split[index++];
        String textEn = split[index++];

        String urlFi = split[index++];
        String urlSv = split[index++];
        String urlEn = split[index++];

        b.titles = createTranslatedString(titleFi, titleSv, titleEn);
        b.descriptions = createTranslatedString(textFi, textSv, textEn);
        b.urls = createTranslatedString(urlFi, urlSv, urlEn);
        return b;
    }



    private static List<InternalMessages.Bulletin.Translation> createTranslatedString(String fi, String sv, String en) {
        List<InternalMessages.Bulletin.Translation> translations = new ArrayList<>();

        InternalMessages.Bulletin.Translation translationFi = InternalMessages.Bulletin.Translation.newBuilder()
                .setText(fi)
                .setLanguage(Bulletin.Language.fi.toString()).build();
        translations.add(translationFi);

        if (sv != null) {
            InternalMessages.Bulletin.Translation translationSv = InternalMessages.Bulletin.Translation.newBuilder()
                    .setText(sv)
                    .setLanguage(Bulletin.Language.sv.toString()).build();
            translations.add(translationSv);
        }
        if (en != null) {
            InternalMessages.Bulletin.Translation translationEn = InternalMessages.Bulletin.Translation.newBuilder()
                    .setText(en)
                    .setLanguage(Bulletin.Language.en.toString()).build();
            translations.add(translationEn);
        }

        return translations;
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
