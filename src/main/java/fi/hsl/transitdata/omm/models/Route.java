package fi.hsl.transitdata.omm.models;

public class Route {
    public long lineGid;
    public String routeId;

    public Route() {}

    public Route(long lineGid, String id) {
        this.lineGid = lineGid;
        this.routeId = id;
    }
}
