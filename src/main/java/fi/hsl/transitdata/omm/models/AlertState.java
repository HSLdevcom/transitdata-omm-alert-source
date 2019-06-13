package fi.hsl.transitdata.omm.models;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AlertState {
    List<Bulletin> alerts;

    public AlertState(List<Bulletin> alerts) {
        this.alerts = alerts;
        if (this.alerts == null)
            this.alerts = new LinkedList<>();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof AlertState) {
            return equals((AlertState)other);
        }
        return false;
    }

    public boolean equals(AlertState other) {
        if (other == this)
            return true;

        if (other == null || other.alerts == null)
            return false;

        if (other.alerts.size() != this.alerts.size())
            return false;

        //Let's not modify the original lists, just to avoid side effects.
        List<Bulletin> oursSorted = asSorted(alerts);
        ListIterator<Bulletin> ourItr = oursSorted.listIterator();

        List<Bulletin> theirsSorted = asSorted(other.alerts);
        ListIterator<Bulletin> theirItr = theirsSorted.listIterator();

        while (ourItr.hasNext()) {
            Bulletin ours = ourItr.next();
            Bulletin theirs = theirItr.next();

            if (!ours.equals(theirs)) {
                return false;
            }
        }
        return true;
    }

    public LocalDateTime lastModified() {
        return LocalDateTime.now();
    }

    static List<Bulletin> asSorted(List<Bulletin> list) {
        return list.stream().sorted(Comparator.comparingLong(bulletin -> bulletin.id)).collect(Collectors.toList());
    }

}
