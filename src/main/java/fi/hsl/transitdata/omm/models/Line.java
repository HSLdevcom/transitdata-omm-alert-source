package fi.hsl.transitdata.omm.models;

import java.util.ArrayList;

public class Line {
    public long gid;
    public ArrayList<Route> routes;

    public Line() {}

    public Line(long gid) {
        this.gid = gid;
        this.routes = new ArrayList<>();
    }

    public void addRouteToLine(Route route) {
        routes.add(route);
    }
}
