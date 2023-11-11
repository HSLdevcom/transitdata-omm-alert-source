package fi.hsl.transitdata.omm.models;

public class DisruptionRouteLink {
    public final String id;
    public final String deviationId;
    public final String startStopId;
    public final String endStopId;
    public final String sequenceNumber;
    public final String latitude;
    public final String longitude;

    public DisruptionRouteLink(String id, String deviationId, String startStopId, String endStopId, String sequenceNumber, String latitude, String longitude) {
        this.id = id;
        this.deviationId = deviationId;
        this.startStopId = startStopId;
        this.endStopId = endStopId;
        this.sequenceNumber = sequenceNumber;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
