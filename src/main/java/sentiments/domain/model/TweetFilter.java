package sentiments.domain.model;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author paw, 6runge
 */
public class TweetFilter {

    private boolean offensive;
    private Timestamp start, end, classified;
    private List<String> languages;
    private List<String> hashtags;

    public boolean isOffensive() {
        return offensive;
    }

    public void setOffensive(boolean offensive) {
        this.offensive = offensive;
    }

    public Timestamp getStart() {
        return start;
    }

    public void setStart(Timestamp start) {
        this.start = start;
    }

    public Timestamp getEnd() {
        return end;
    }

    public void setEnd(Timestamp end) {
        this.end = end;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public Timestamp getClassified() {
        return classified;
    }

    public void setClassified(Timestamp classified) {
        this.classified = classified;
    }
}
