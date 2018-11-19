package fi.hsl.transitdata.omm.models;

import com.google.transit.realtime.GtfsRealtime;

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
        //TODO DEFINE
        public GtfsRealtime.Alert.Cause toGtfsCause() {
            switch (this) {
                case EarlierDisruption: return GtfsRealtime.Alert.Cause.OTHER_CAUSE;
                default: return GtfsRealtime.Alert.Cause.OTHER_CAUSE;
            }
        }
    }

    public enum Language {
        //Let's define these already in BCP-47 format, so .toString() works
        fi, en, sv
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
    public GtfsRealtime.TranslatedString descriptions;
    public GtfsRealtime.TranslatedString headers;

}
