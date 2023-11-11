package fi.hsl.transitdata.omm.models;

import fi.hsl.common.transitdata.proto.InternalMessages;
import fi.hsl.transitdata.omm.models.cancellations.Journey;
import fi.hsl.transitdata.omm.models.cancellations.JourneyPattern;
import fi.hsl.transitdata.omm.models.cancellations.JourneyPatternStop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DisruptionRoute {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Logger log = LoggerFactory.getLogger(DisruptionRoute.class);

    public final String disruptionRouteId;
    public final String startStopId;
    public final String endStopId;
    private final Optional<LocalDateTime> validFromDate;
    private final Optional<LocalDateTime> validToDate;
    private final ZoneId timezoneId;
    public final Collection<String> affectedRoutes;
    private final Map<String, List<Journey>> affectedJourneysByJourneyPatternId;
    private final Map<String, List<String>> affectedStopIdsByJourneyPatternId;
    public final ArrayList<DisruptionRouteLink> disruptionRouteLinks;

    public DisruptionRoute(String disruptionRouteId, String startStopId, String endStopId, Collection<String> affectedRoutes, String validFromDate, String validToDate, String timezone, ArrayList<DisruptionRouteLink> disruptionRouteLinks) {
        this.disruptionRouteId = disruptionRouteId;
        this.startStopId = startStopId;
        this.endStopId = endStopId;
        this.validFromDate = getDateOrEmpty(validFromDate);
        this.validToDate = getDateOrEmpty(validToDate);
        this.timezoneId = ZoneId.of(timezone);
        this.affectedRoutes = affectedRoutes;
        this.affectedJourneysByJourneyPatternId = new HashMap<>();
        this.affectedStopIdsByJourneyPatternId = new HashMap<>();
        this.disruptionRouteLinks = disruptionRouteLinks;
    }

    public void addAffectedJourneys(List<Journey> journeys) {
        for (Journey journey : journeys) {
            if (!affectedJourneysByJourneyPatternId.containsKey(journey.journeyPatternId)) {
                affectedJourneysByJourneyPatternId.put(journey.journeyPatternId, new ArrayList<>());
            }
            affectedJourneysByJourneyPatternId.get(journey.journeyPatternId).add(journey);
        }
    }

    public Optional<String> getValidFrom() {
        return validFromDate.map(DATE_TIME_FORMATTER::format);
    }

    public Optional<String> getValidTo() {
        return validToDate.map(DATE_TIME_FORMATTER::format);
    }

    public Collection<String> getAffectedJourneyPatternIds() {
        return affectedJourneysByJourneyPatternId.keySet();
    }

    public void findAddAffectedStops(Map<String, JourneyPattern> journeyPatternsById) {
        for (String jpId : affectedJourneysByJourneyPatternId.keySet()) {
            JourneyPattern journeyPattern = journeyPatternsById.get(jpId);
            Optional<List<JourneyPatternStop>> stopsBetween = journeyPattern.getStopsBetweenTwoStops(startStopId, endStopId);
            stopsBetween.ifPresent(stops -> affectedStopIdsByJourneyPatternId.put(jpId, stops.stream().map(stop -> stop.stopId).collect(Collectors.toList())));
        }
    }

    public Optional<LocalDateTime> getDateOrEmpty(String dateStr) {
        try {
            return Optional.of(LocalDateTime.parse(dateStr.replace(" ", "T")));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List <InternalMessages.StopCancellations.StopCancellation> getAsStopCancellations() {
        // find unique cancelledStopIds from all affected stop ids
        Set<String> cancelledStopIds = affectedStopIdsByJourneyPatternId.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        if (cancelledStopIds.isEmpty()) {
            log.info("No stop cancellations were created by disruption route {}", disruptionRouteId);
            return Collections.emptyList();
        }

        validateStopCancellations(cancelledStopIds);
        return cancelledStopIds.stream().map(stopId -> {
            InternalMessages.StopCancellations.StopCancellation.Builder builder = InternalMessages.StopCancellations.StopCancellation.newBuilder();
            builder.setCause(InternalMessages.StopCancellations.Cause.JOURNEY_PATTERN_DETOUR);
            builder.setStopId(stopId);
            validFromDate.ifPresent(dateTime -> builder.setValidFromUnixS(toUtcEpochSeconds(dateTime)));
            validToDate.ifPresent(dateTime -> builder.setValidToUnixS(toUtcEpochSeconds(dateTime)));
            builder.addAllAffectedJourneyPatternIds(new ArrayList<>(affectedStopIdsByJourneyPatternId.keySet()));
            return builder.build();
        }).collect(Collectors.toList());
    }

    //TODO: what does this do?
    private void validateStopCancellations(Set<String> cancelledStopIds) {
        // check that there's no journey pattern specific stop cancellations
        Set<String> compareStopIds = new HashSet<>(affectedStopIdsByJourneyPatternId.values().iterator().next());
        if (!cancelledStopIds.equals(compareStopIds)) {
            log.warn("Found journey pattern specific stop cancellations by disruption route");
        }
    }

    public List <InternalMessages.JourneyPattern> getAffectedJourneyPatterns(Map<String, JourneyPattern> journeyPatternById) {
        return affectedJourneysByJourneyPatternId.keySet().stream().map(jpId -> {
            JourneyPattern affectedJourneyPattern = journeyPatternById.get(jpId).createNewWithSameStops();
            affectedJourneyPattern.addAffectedJourneys(affectedJourneysByJourneyPatternId.get(jpId));
            return affectedJourneyPattern.getAsProtoBuf();
        }).collect(Collectors.toList());
    }

    private Long toUtcEpochSeconds(LocalDateTime dt) {
        // convert dt to Unix time (i.e. Epoch time in seconds)
        return dt.atZone(timezoneId).toEpochSecond();
    }

}
