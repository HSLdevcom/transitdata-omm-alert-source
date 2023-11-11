package fi.hsl.transitdata.omm.models.cancellations;

public class Stop {

    public final String stopGid;
    public final String stopId;
    public final String name;

    public Stop(String stopGid, String stopId, String name)  {
        this.stopGid = stopGid;
        this.stopId = stopId;
        this.name = name;
    }

}
