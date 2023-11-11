package fi.hsl.transitdata.omm.models.cancellations;

import fi.hsl.common.transitdata.proto.InternalMessages;

import java.util.*;
import java.util.stream.Collectors;

public class JourneyPattern {

    public final String id;
    private final int stopCount;
    private final Map<String, JourneyPatternStop> stops = new HashMap<>();
    //Stop IDs sorted by stop sequence
    private final NavigableSet<String> stopIds = new TreeSet<>(Comparator.comparingInt(stopId -> stops.get(stopId).getSequence()));
    private final List<Journey> journeys = new ArrayList<>();

    public JourneyPattern(String id, int stopCount) {
        this.id = id;
        this.stopCount = stopCount;
    }

    public Collection<String> getStopIds(){
        return stops.keySet();
    }

    public void addStop(JourneyPatternStop stop) {
        if (!stops.containsKey(stop.stopId)) {
            stops.put(stop.stopId, stop);
            stopIds.add(stop.stopId);
        }
    }

    public JourneyPattern createNewWithSameStops() {
        JourneyPattern copy = new JourneyPattern(id, stopCount);
        copy.stops.putAll(stops);
        copy.stopIds.addAll(stopIds);
        return copy;
    }

    public void addAffectedJourneys(List<Journey> affectedJourneys) {
        this.journeys.addAll(affectedJourneys);
    }

    public Optional<List<JourneyPatternStop>> getStopsBetweenTwoStops(String startStopId, String endStopId) {
        //If the journey pattern does not contain either the start or end stop, return nothing
        if (!stops.containsKey(startStopId) || !stops.containsKey(endStopId)) {
            return Optional.empty();
        }

        Set<String> stopIdsBetweenStops = stopIds.subSet(startStopId, false, endStopId, false);
        if (stopIdsBetweenStops.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(stopIdsBetweenStops.stream().map(stops::get).collect(Collectors.toList()));
    }

    public InternalMessages.JourneyPattern getAsProtoBuf() {
        InternalMessages.JourneyPattern.Builder builder = InternalMessages.JourneyPattern.newBuilder();
        builder.setJourneyPatternId(id);
        builder.addAllStops(stopIds.stream().map(stops::get).map(JourneyPatternStop::getAsProtoBuf).collect(Collectors.toList()));
        if (!journeys.isEmpty()) {
            builder.addAllTrips(journeys.stream().map(Journey::getAsProtoBuf).collect(Collectors.toList()));
        }
        return builder.build();
    }
}
