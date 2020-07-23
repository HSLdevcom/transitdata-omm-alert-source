package fi.hsl.transitdata.omm.models;

import fi.hsl.common.transitdata.proto.InternalMessages;

import java.time.LocalDateTime;
import java.util.*;

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

        public InternalMessages.Category toCategory() {
            return InternalMessages.Category.valueOf(this.toString());
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
                case "IRREGULAR_DEPARTURES_MAX_15":
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

        public InternalMessages.Bulletin.Impact toImpact() {
            return InternalMessages.Bulletin.Impact.valueOf(this.toString());
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

        public InternalMessages.Bulletin.Priority toPriority() {
            return InternalMessages.Bulletin.Priority.valueOf(this.toString());
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
    public Priority priority;
    public List<InternalMessages.Bulletin.Translation> titles;
    public List<InternalMessages.Bulletin.Translation> descriptions;
    public List<InternalMessages.Bulletin.Translation> urls;
    public boolean displayOnly;

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
        priority = other.priority;
        if (other.titles != null)
            titles = new ArrayList<>(other.titles);
        if (other.descriptions != null)
            descriptions = new ArrayList<>(other.descriptions);
        if (other.urls != null)
            urls = new ArrayList<>(other.urls);
        displayOnly = other.displayOnly;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bulletin bulletin = (Bulletin) o;
        return id == bulletin.id &&
                affectsAllRoutes == bulletin.affectsAllRoutes &&
                affectsAllStops == bulletin.affectsAllStops &&
                displayOnly == bulletin.displayOnly &&
                category == bulletin.category &&
                impact == bulletin.impact &&
                Objects.equals(lastModified, bulletin.lastModified) &&
                Objects.equals(validFrom, bulletin.validFrom) &&
                Objects.equals(validTo, bulletin.validTo) &&
                Objects.equals(affectedLineGids, bulletin.affectedLineGids) &&
                Objects.equals(affectedStopGids, bulletin.affectedStopGids) &&
                priority == bulletin.priority &&
                Objects.equals(titles, bulletin.titles) &&
                Objects.equals(descriptions, bulletin.descriptions) &&
                Objects.equals(urls, bulletin.urls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, category, impact, lastModified, validFrom, validTo, affectsAllRoutes, affectsAllStops, affectedLineGids, affectedStopGids, priority, titles, descriptions, urls, displayOnly);
    }
}
