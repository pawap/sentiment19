package sentiments.domain.model;

import javax.persistence.Entity;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Paw
 */
@Entity
public class Crawl {

    public final static int IN_PROGRESS = 0;

    public final static int FINISHED = 1;

    private LocalDateTime date;

    private int status;

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
