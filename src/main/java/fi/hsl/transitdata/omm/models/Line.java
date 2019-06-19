package fi.hsl.transitdata.omm.models;

import java.util.ArrayList;

public class Line {
    public long gid;
    public String lineId;
    public ArrayList<Route> routes;

    public Line() {}

    public Line(long gid, String id) {
        this.gid = gid;
        this.lineId = id;
        this.routes = new ArrayList<Route>();
    }

    public void addRouteToLine(Route route) {
        routes.add(route);
    }
}
