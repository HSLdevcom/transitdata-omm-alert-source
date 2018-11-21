package fi.hsl.transitdata.omm.models;

public class StopPoint {
    public long gid;
    public String stopId;

    public StopPoint() {}

    public StopPoint(long gid, String id) {
        this.gid = gid;
        this.stopId = id;
    }

}
