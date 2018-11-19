package fi.hsl.transitdata.omm.models;

import com.google.transit.realtime.GtfsRealtime;

import java.time.LocalDateTime;
import java.util.List;

public class Bulletin {
    public enum Category {
        //TODO check if contains all
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

    public enum Impact {
        //TODO check if contains all
        Cancelled,
        Delayed,
        DeviatingSchedule,
        DisruptionRoute,
        PossibleDeviations,
        PossiblyDelayed,
        ReducedTransport,
        ReturningToNormal;

        public static Impact fromString(String str) {
            switch (str) {
                case "CANCELLED": return Cancelled;
                case "DELAYED": return Delayed;
                case "DEVIATING_SCHEDULE": return DeviatingSchedule;
                case "DISRUPTION_ROUTE": return DisruptionRoute;
                case "POSSIBLE_DEVIATIONS": return PossibleDeviations;
                case "POSSIBLY_DELAYED": return PossiblyDelayed;
                case "REDUCED_TRANSPORT": return ReducedTransport;
                case "RETURNING_TO_NORMAL": return ReturningToNormal;
                default: throw new IllegalArgumentException("Could not parse Impact from String: " + str);
            }
        }

        /**
         * @return possible GTFS-RT Effects:
         * NO_SERVICE,
         * REDUCED_SERVICE,
         * SIGNIFICANT_DELAYS,
         * DETOUR,
         * ADDITIONAL_SERVICE,
         * MODIFIED_SERVICE,
         * OTHER_EFFECT,
         * UNKNOWN_EFFECT,
         * STOP_MOVED
         */
        public GtfsRealtime.Alert.Effect toGtfsEffect() {
            //TODO Check all these!
            switch (this) {
                case Cancelled: return GtfsRealtime.Alert.Effect.NO_SERVICE;
                case Delayed: return GtfsRealtime.Alert.Effect.SIGNIFICANT_DELAYS;
                case DeviatingSchedule: return GtfsRealtime.Alert.Effect.SIGNIFICANT_DELAYS;
                case DisruptionRoute: return GtfsRealtime.Alert.Effect.DETOUR;
                case PossibleDeviations: return GtfsRealtime.Alert.Effect.SIGNIFICANT_DELAYS;
                case PossiblyDelayed: return GtfsRealtime.Alert.Effect.SIGNIFICANT_DELAYS;
                case ReducedTransport: return GtfsRealtime.Alert.Effect.REDUCED_SERVICE;
                case ReturningToNormal: return GtfsRealtime.Alert.Effect.OTHER_EFFECT;
                default: return GtfsRealtime.Alert.Effect.UNKNOWN_EFFECT;
            }
        }
    }


    public enum Language {
        //Let's define these already in BCP-47 format, so .toString() works
        fi, en, sv
    }

    public long id;
    public Category category;
    public Impact impact;
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
