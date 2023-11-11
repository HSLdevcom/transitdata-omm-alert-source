package fi.hsl.transitdata.omm.models.cancellations;

import fi.hsl.common.transitdata.proto.InternalMessages;

public class JourneyPatternStop {

    private final String stopGid;
    public final String stopId;
    private final Integer sequence;
    private final String name;

    public JourneyPatternStop(String stopGid, String stopId, String name, Integer sequence)  {
        this.stopGid = stopGid;
        this.stopId = stopId;
        this.name = name;
        this.sequence = sequence;
    }

    public InternalMessages.JourneyPattern.Stop getAsProtoBuf() {
        InternalMessages.JourneyPattern.Stop.Builder builder = InternalMessages.JourneyPattern.Stop.newBuilder();
        builder.setStopId(stopId);
        builder.setStopSequence(sequence);
        return builder.build();
    }

    public Integer getSequence() {
        return this.sequence;
    }

}
