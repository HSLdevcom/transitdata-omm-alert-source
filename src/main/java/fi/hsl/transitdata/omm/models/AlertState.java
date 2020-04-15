package fi.hsl.transitdata.omm.models;

import java.util.*;

public class AlertState {
    Set<Bulletin> alerts;

    public AlertState(Collection<Bulletin> alerts) {
        this.alerts = alerts == null ? Collections.emptySet() : new HashSet<>(alerts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlertState that = (AlertState) o;
        return Objects.equals(alerts, that.alerts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alerts);
    }
}
