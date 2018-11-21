package fi.hsl.transitdata.omm.models;

public class Line {
    public long gid;
    public String lineId;

    public Line() {}

    public Line(long gid, String id) {
        this.gid = gid;
        this.lineId = id;
    }
}
