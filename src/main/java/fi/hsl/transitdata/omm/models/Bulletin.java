package fi.hsl.transitdata.omm.models;

import com.google.transit.realtime.GtfsRealtime;

import java.time.LocalDateTime;
import java.util.LinkedList;
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
        TrafficAccident,
        TrafficJam,
        TechicalFailure;

        public static Category fromString(String str) {
            switch (str) {
                case "EARLIER_DISRUPTION": return EarlierDisruption;
                case "NO_TRAFFIC_DISRUPTION": return NoTrafficDisruption;
                case "ROAD_MAINTENANCE": return RoadMaintenance;
                case "ROAD_CLOSED": return RoadClosed;
                case "ROAD_TRENCH": return RoadTrench;
                case "TRACK_BLOCKED": return TrackBlocked;
                case "TRAFFIC_ACCIDENT": return TrafficAccident;
                case "TRAFFIC_JAM": return TrafficJam;
                case "TECHNICAL_FAILURE": return TechicalFailure;
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
    public List<Long> affectedLineGids;
    public List<Long> affectedStopGids;
    public GtfsRealtime.TranslatedString descriptions;
    public GtfsRealtime.TranslatedString headers;

    public Bulletin() {}

    public Bulletin(Bulletin other) {
        id = other.id;
        category = other.category;
        impact = other.impact;
        lastModified = other.lastModified;
        validFrom = other.validFrom;
        validTo = other.validTo;
        affectsAllRoutes = other.affectsAllRoutes;
        affectsAllStops = other.affectsAllStops;
        if (other.affectedLineGids != null)
            affectedLineGids = new LinkedList<>(other.affectedLineGids);
        if (other.affectedStopGids != null)
            affectedStopGids = new LinkedList<>(other.affectedStopGids);
        descriptions = other.descriptions;
        headers = other.headers;
    }


    @Override
    public boolean equals(Object other) {
        if (other instanceof Bulletin) {
            return equals((Bulletin) other);
        }
        return false;
    }

    public boolean equals(Bulletin other) {
        if (other == this)
            return true;

        if (other == null)
            return false;

        boolean same = true;
        same &= this.id == other.id;
        same &= this.category == other.category;
        same &= this.impact == other.impact;
        same &= equalsWithNullCheck(this.lastModified, other.lastModified);
        same &= equalsWithNullCheck(this.validFrom, other.validFrom);
        same &= equalsWithNullCheck(this.validTo, other.validTo);
        same &= this.affectsAllRoutes == other.affectsAllRoutes;
        same &= this.affectsAllStops == other.affectsAllStops;
        same &= equalsWithNullCheck(this.affectedLineGids, other.affectedLineGids);
        same &= equalsWithNullCheck(this.affectedStopGids, other.affectedStopGids);
        same &= equalsWithNullCheck(this.descriptions, other.descriptions);
        same &= equalsWithNullCheck(this.headers, other.headers);

        return same;
    }

    static boolean equalsWithNullCheck(Object o1, Object o2) {
        if (o1 == null && o2 == null)
            return true;
        if (o1 != null && o2 != null)
            return o1.equals(o2);
        return false;
    }

}
