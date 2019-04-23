package fi.hsl.transitdata.omm.models;

import com.google.transit.realtime.GtfsRealtime;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Bulletin {

    public enum Category {

        OTHER_DRIVER_ERROR,
        ITS_SYSTEM_ERROR,
        TOO_MANY_PASSENGERS,
        MISPARKED_VEHICLE,
        STRIKE,
        TEST,
        VEHICLE_OFF_THE_ROAD,
        TRAFFIC_ACCIDENT,
        SWITCH_FAILURE,
        SEIZURE,
        WEATHER,
        STATE_VISIT,
        ROAD_MAINTENANCE,
        ROAD_CLOSED,
        TRACK_BLOCKED,
        WEATHER_CONDITIONS,
        ASSAULT,
        TRACK_MAINTENANCE,
        MEDICAL_INCIDENT,
        EARLIER_DISRUPTION,
        TECHNICAL_FAILURE,
        TRAFFIC_JAM,
        OTHER,
        NO_TRAFFIC_DISRUPTION,
        ACCIDENT,
        PUBLIC_EVENT,
        ROAD_TRENCH,
        VEHICLE_BREAKDOWN,
        POWER_FAILURE,
        STAFF_DEFICIT,
        DISTURBANCE,
        VEHICLE_DEFICIT;

        public static Category fromString(String str) {
            switch (str) {
                case "OTHER_DRIVER_ERROR": return OTHER_DRIVER_ERROR;
                case "ITS_SYSTEM_ERROR": return ITS_SYSTEM_ERROR;
                case "TOO_MANY_PASSENGERS": return TOO_MANY_PASSENGERS;
                case "MISPARKED_VEHICLE": return MISPARKED_VEHICLE;
                case "STRIKE": return STRIKE;
                case "TEST": return TEST;
                case "VEHICLE_OFF_THE_ROAD": return VEHICLE_OFF_THE_ROAD;
                case "TRAFFIC_ACCIDENT": return TRAFFIC_ACCIDENT;
                case "SWITCH_FAILURE": return SWITCH_FAILURE;
                case "SEIZURE": return SEIZURE;
                case "WEATHER": return WEATHER;
                case "STATE_VISIT": return STATE_VISIT;
                case "ROAD_MAINTENANCE": return ROAD_MAINTENANCE;
                case "ROAD_CLOSED": return ROAD_CLOSED;
                case "TRACK_BLOCKED": return TRACK_BLOCKED;
                case "WEATHER_CONDITIONS": return WEATHER_CONDITIONS;
                case "ASSAULT": return ASSAULT;
                case "TRACK_MAINTENANCE": return TRACK_MAINTENANCE;
                case "MEDICAL_INCIDENT": return MEDICAL_INCIDENT;
                case "EARLIER_DISRUPTION": return EARLIER_DISRUPTION;
                case "TECHNICAL_FAILURE": return TECHNICAL_FAILURE;
                case "TRAFFIC_JAM": return TRAFFIC_JAM;
                case "OTHER": return OTHER;
                case "NO_TRAFFIC_DISRUPTION": return NO_TRAFFIC_DISRUPTION;
                case "ACCIDENT": return ACCIDENT;
                case "PUBLIC_EVENT": return PUBLIC_EVENT;
                case "ROAD_TRENCH": return ROAD_TRENCH;
                case "VEHICLE_BREAKDOWN": return VEHICLE_BREAKDOWN;
                case "POWER_FAILURE": return POWER_FAILURE;
                case "STAFF_DEFICIT": return STAFF_DEFICIT;
                case "DISTURBANCE": return DISTURBANCE;
                case "VEHICLE_DEFICIT": return VEHICLE_DEFICIT;
                default: throw new IllegalArgumentException("Could not parse category from String: " + str);
            }
        }

        public GtfsRealtime.Alert.Cause toGtfsCause() {
            switch (this) {
                case OTHER_DRIVER_ERROR: return GtfsRealtime.Alert.Cause.OTHER_CAUSE;
                case ITS_SYSTEM_ERROR: return GtfsRealtime.Alert.Cause.TECHNICAL_PROBLEM;
                case TOO_MANY_PASSENGERS: return GtfsRealtime.Alert.Cause.OTHER_CAUSE;
                case MISPARKED_VEHICLE: return GtfsRealtime.Alert.Cause.OTHER_CAUSE;
                case STRIKE: return GtfsRealtime.Alert.Cause.STRIKE;
                case TEST: return GtfsRealtime.Alert.Cause.OTHER_CAUSE;
                case VEHICLE_OFF_THE_ROAD: return GtfsRealtime.Alert.Cause.ACCIDENT;
                case TRAFFIC_ACCIDENT: return GtfsRealtime.Alert.Cause.ACCIDENT;
                case SWITCH_FAILURE: return GtfsRealtime.Alert.Cause.TECHNICAL_PROBLEM;
                case SEIZURE: return GtfsRealtime.Alert.Cause.MEDICAL_EMERGENCY;
                case WEATHER: return GtfsRealtime.Alert.Cause.WEATHER;
                case STATE_VISIT: return GtfsRealtime.Alert.Cause.OTHER_CAUSE;
                case ROAD_MAINTENANCE: return GtfsRealtime.Alert.Cause.MAINTENANCE;
                case ROAD_CLOSED: return GtfsRealtime.Alert.Cause.CONSTRUCTION;
                case TRACK_BLOCKED: return GtfsRealtime.Alert.Cause.OTHER_CAUSE;
                case WEATHER_CONDITIONS: return GtfsRealtime.Alert.Cause.WEATHER;
                case ASSAULT: return GtfsRealtime.Alert.Cause.POLICE_ACTIVITY;
                case TRACK_MAINTENANCE: return GtfsRealtime.Alert.Cause.MAINTENANCE;
                case MEDICAL_INCIDENT: return GtfsRealtime.Alert.Cause.MEDICAL_EMERGENCY;
                case EARLIER_DISRUPTION: return GtfsRealtime.Alert.Cause.OTHER_CAUSE;
                case TECHNICAL_FAILURE: return GtfsRealtime.Alert.Cause.TECHNICAL_PROBLEM;
                case TRAFFIC_JAM: return GtfsRealtime.Alert.Cause.OTHER_CAUSE;
                case OTHER: return GtfsRealtime.Alert.Cause.OTHER_CAUSE;
                case NO_TRAFFIC_DISRUPTION: return GtfsRealtime.Alert.Cause.OTHER_CAUSE;
                case ACCIDENT: return GtfsRealtime.Alert.Cause.ACCIDENT;
                case PUBLIC_EVENT: return GtfsRealtime.Alert.Cause.OTHER_CAUSE;
                case ROAD_TRENCH: return GtfsRealtime.Alert.Cause.CONSTRUCTION;
                case VEHICLE_BREAKDOWN: return GtfsRealtime.Alert.Cause.TECHNICAL_PROBLEM;
                case POWER_FAILURE: return GtfsRealtime.Alert.Cause.TECHNICAL_PROBLEM;
                case STAFF_DEFICIT: return GtfsRealtime.Alert.Cause.OTHER_CAUSE;
                case DISTURBANCE: return GtfsRealtime.Alert.Cause.OTHER_CAUSE;
                case VEHICLE_DEFICIT: return GtfsRealtime.Alert.Cause.TECHNICAL_PROBLEM;
                default: return GtfsRealtime.Alert.Cause.UNKNOWN_CAUSE;
            }
        }
    }

    public enum Impact {
        CANCELLED,
        DELAYED,
        DEVIATING_SCHEDULE,
        DISRUPTION_ROUTE,
        IRREGULAR_DEPARTURES,
        POSSIBLE_DEVIATIONS,
        POSSIBLY_DELAYED,
        REDUCED_TRANSPORT,
        RETURNING_TO_NORMAL,
        VENDING_MACHINE_OUT_OF_ORDER,
        NULL,
        OTHER,
        NO_TRAFFIC_IMPACT,
        UNKNOWN;

        public static Impact fromString(String str) {
            if (str == null) {
                return NULL; //This can be null in the database schema.
            }
            switch (str) {
                case "CANCELLED": return CANCELLED;
                case "DELAYED": return DELAYED;
                case "DEVIATING_SCHEDULE": return DEVIATING_SCHEDULE;
                case "DISRUPTION_ROUTE": return DISRUPTION_ROUTE;
                case "IRREGULAR_DEPARTURES": return IRREGULAR_DEPARTURES;
                case "POSSIBLE_DEVIATIONS": return POSSIBLE_DEVIATIONS;
                case "POSSIBLY_DELAYED": return POSSIBLY_DELAYED;
                case "REDUCED_TRANSPORT": return REDUCED_TRANSPORT;
                case "RETURNING_TO_NORMAL": return RETURNING_TO_NORMAL;
                case "VENDING_MACHINE_OUT_OF_ORDER": return VENDING_MACHINE_OUT_OF_ORDER;
                case "OTHER": return OTHER;
                case "NO_TRAFFIC_IMPACT": return NO_TRAFFIC_IMPACT;
                case "UNKNOWN": return UNKNOWN;
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
            switch (this) {
                case CANCELLED: return GtfsRealtime.Alert.Effect.NO_SERVICE;
                case DELAYED: return GtfsRealtime.Alert.Effect.SIGNIFICANT_DELAYS;
                case DEVIATING_SCHEDULE: return GtfsRealtime.Alert.Effect.MODIFIED_SERVICE;
                case DISRUPTION_ROUTE: return GtfsRealtime.Alert.Effect.DETOUR;
                case IRREGULAR_DEPARTURES: return GtfsRealtime.Alert.Effect.SIGNIFICANT_DELAYS;
                case POSSIBLE_DEVIATIONS: return GtfsRealtime.Alert.Effect.MODIFIED_SERVICE;
                case POSSIBLY_DELAYED: return GtfsRealtime.Alert.Effect.OTHER_EFFECT;
                case REDUCED_TRANSPORT: return GtfsRealtime.Alert.Effect.REDUCED_SERVICE;
                case RETURNING_TO_NORMAL: return GtfsRealtime.Alert.Effect.OTHER_EFFECT;
                case VENDING_MACHINE_OUT_OF_ORDER: return GtfsRealtime.Alert.Effect.OTHER_EFFECT;
                case NULL: return GtfsRealtime.Alert.Effect.UNKNOWN_EFFECT;
                case OTHER: return GtfsRealtime.Alert.Effect.OTHER_EFFECT;
                case NO_TRAFFIC_IMPACT: return GtfsRealtime.Alert.Effect.NO_EFFECT;
                case UNKNOWN: return GtfsRealtime.Alert.Effect.UNKNOWN_EFFECT;
                default: return GtfsRealtime.Alert.Effect.UNKNOWN_EFFECT;
            }
        }
    }


    public enum Language {
        //Let's define these already in BCP-47 format, so .toString() works
        fi, en, sv
    }

    public enum Priority {
        INFO,
        WARNING,
        SEVERE;

        public static Optional<Priority> fromInt(final Integer priority) {
            switch (priority) {
                case 1: return Optional.of(INFO);
                case 2: return Optional.of(WARNING);
                case 3: return Optional.of(SEVERE);
                default: return Optional.empty();
            }
        }

        /**
         * @return possible GTFS-RT Effects:
         * UNKNOWN_SEVERITY,
         * INFO,
         * WARNING,
         * SEVERE
         */
        public Optional<GtfsRealtime.Alert.SeverityLevel> toGtfsSeverityLevel() {
            switch (this) {
                case INFO: return Optional.of(GtfsRealtime.Alert.SeverityLevel.INFO);
                case WARNING: return Optional.of(GtfsRealtime.Alert.SeverityLevel.WARNING);
                case SEVERE: return Optional.of(GtfsRealtime.Alert.SeverityLevel.SEVERE);
                default: return Optional.empty();
            }
        }
    }

    public long id;
    public Category category;
    public Impact impact;
    public LocalDateTime lastModified;
    public Optional<LocalDateTime> validFrom;
    public Optional<LocalDateTime> validTo;
    public boolean affectsAllRoutes;
    public boolean affectsAllStops;
    public List<Long> affectedLineGids;
    public List<Long> affectedStopGids;
    public GtfsRealtime.TranslatedString descriptions;
    public GtfsRealtime.TranslatedString headers;
    public Priority priority;

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
        priority = other.priority;
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
        same &= this.priority == other.priority;

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
