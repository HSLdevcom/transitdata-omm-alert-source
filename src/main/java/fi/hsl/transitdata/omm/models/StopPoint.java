package fi.hsl.transitdata.omm.models;

import fi.hsl.transitdata.omm.db.DAOImplBase;

import java.time.LocalDateTime;
import java.util.Optional;

public class StopPoint {
    public long gid;
    public String stopId;
    public Optional<LocalDateTime> existsFromDate;
    public Optional<LocalDateTime> existsUptoDate;

    public StopPoint() {}

    public StopPoint(long gid, String id) {
        this.gid = gid;
        this.stopId = id;
        this.existsFromDate = Optional.empty();
        this.existsUptoDate = Optional.empty();
    }

    public StopPoint(long gid, String id, String existsFromDate, String existsUptoDate)  {
        this.gid = gid;
        this.stopId = id;
        this.existsFromDate = getDateOrEmpty(existsFromDate);
        this.existsUptoDate = getDateOrEmpty(existsUptoDate);
    }

    public Optional<LocalDateTime> getDateOrEmpty(String dateStr) {
        try {
            return Optional.of(DAOImplBase.parseOmmLocalDateTime(dateStr));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
