package fi.hsl.transitdata.omm.models.cancellations;

import fi.hsl.common.transitdata.proto.InternalMessages;

public class Journey {

    private final String tripId;
    private final String operatingDay;
    private final String routeName;
    private final int direction;
    private final String startTime;
    public final String journeyPatternId;

    public Journey(String tripid, String operatingDay, String routeName, int direction, String startTime, String journeyPatternId) {
        this.tripId = tripid;
        this.operatingDay = operatingDay;
        this.routeName = routeName;
        this.direction = direction;
        this.startTime = startTime;
        this.journeyPatternId = journeyPatternId;
    }

    public InternalMessages.TripInfo getAsProtoBuf() {
        InternalMessages.TripInfo.Builder builder = InternalMessages.TripInfo.newBuilder();
        builder.setTripId(tripId);
        builder.setOperatingDay(operatingDay);
        builder.setRouteId(routeName);
        builder.setDirectionId(direction);
        builder.setStartTime(startTime);
        builder.setScheduleType(InternalMessages.TripInfo.ScheduleType.SCHEDULED);
        return builder.build();
    }

}
