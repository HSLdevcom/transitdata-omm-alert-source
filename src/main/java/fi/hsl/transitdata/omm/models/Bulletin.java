package fi.hsl.transitdata.omm.models;

import java.time.LocalDateTime;
import java.util.List;

public class Bulletin {
    public enum Category {
        EarlierDisruption,
        NoTrafficDisruption,
        RoadMaintenance,
        RoadClosed,
        RoadTrench,
        TrackBlocked,
        TrafficAccident;

        public static Category fromString(String str) {
            switch (str) {
                case "EARLIER_DISRUPTION": return EarlierDisruption;
                case "NO_TRAFFIC_DISRUPTION": return NoTrafficDisruption;
                case "ROAD_MAINTENANCE": return RoadMaintenance;
                case "ROAD_CLOSED": return RoadClosed;
                case "ROAD_TRENCH": return RoadTrench;
                case "TRACK_BLOCKED": return TrackBlocked;
                case "TRAFFIC_ACCIDENT": return TrafficAccident;
                default: throw new IllegalArgumentException("Could not parse category from String: " + str);
            }
        }
    }

    public enum Language {
        FI, EN, SV
    }
    public static class LocalizedText {
        public Language language;
        public String title;
        public String text;
    }

    public long id;
    public Category category;
    public LocalDateTime lastModified;
    public LocalDateTime validFrom;
    public LocalDateTime validTo;
    public boolean affectsAllRoutes;
    public boolean affectsAllStops;
    public List<Long> affectedRouteIds;
    public List<Long> affectedStopIds;
    public LocalizedText textFi;
    public LocalizedText textEn;
    public LocalizedText textSv;

}
