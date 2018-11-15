package fi.hsl.transitdata.omm.models;

import com.google.transit.realtime.GtfsRealtime;

import java.util.List;

public class AlertState {
    List<Bulletin> alerts;

    public AlertState(List<Bulletin> alerts) {
        this.alerts = alerts;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof AlertState) {
            return equals((AlertState)other);
        }
        return false;
    }

    public boolean equals(AlertState other) {
        if (other == null)
            return false;

        if (other.alerts.size() != this.alerts.size())
            return false;

        //TODO use Set. not list.
        return false;
    }

    @Override
    public int hashCode() {
        return 0;//TODO create hash from the entire set.... difficult. yes.
    }

}
