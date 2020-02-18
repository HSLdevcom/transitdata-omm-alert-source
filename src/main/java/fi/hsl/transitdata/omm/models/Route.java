package fi.hsl.transitdata.omm.models;

import fi.hsl.transitdata.omm.db.DAOImplBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

public class Route {
    static final Logger log = LoggerFactory.getLogger(Route.class);
    public long lineGid;
    public String routeId;
    public Optional<LocalDateTime> existsFromDate;
    public Optional<LocalDateTime> existsUptoDate;

    public Route() {}

    public Route (long lineGid, String id) {
        this.lineGid = lineGid;
        this.routeId = id;
        this.existsFromDate = Optional.empty();
        this.existsUptoDate = Optional.empty();
    }

    public Route(long lineGid, String id, String existsFromDate, String existsUptoDate) {
        this.lineGid = lineGid;
        this.routeId = id;
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
