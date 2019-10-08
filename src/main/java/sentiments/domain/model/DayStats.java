package sentiments.domain.model;

import org.springframework.data.mongodb.core.index.Indexed;

import javax.persistence.Entity;
import java.time.LocalDate;

/**
 * @author 6runge
 */
@Entity
public class DayStats {

    private String language;

    private int offensive;

    private int nonoffensive;

    @Indexed
    private LocalDate date;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getOffensive() {
        return offensive;
    }

    public void setOffensive(int offensive) {
        this.offensive = offensive;
    }

    public int getNonoffensive() {
        return nonoffensive;
    }

    public void setNonoffensive(int nonoffensive) {
        this.nonoffensive = nonoffensive;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

}
