package sentiments.domain.model.query;

import java.sql.Timestamp;
import java.util.List;

/**
 * Used to filter tweets with specified attributes.
 * @author paw, 6runge
 */
public class TweetFilter {

    private Boolean offensive = null;
    private Timestamp start, end, classified;
    private List<String> languages;
    private List<String> hashtags;

    /**
     * @return false/true, if the desired tweets should be (non-)offensive. null for either.
     */
    public Boolean isOffensive() {
        return offensive;
    }

    /**
     * @param offensive false/true, if the desired tweets should be (non-)offensive. null for either.
     */
    public void setOffensive(Boolean offensive) {
        this.offensive = offensive;
    }

    /**
     * @return the creation date of the earliest tweet
     */
    public Timestamp getStart() {
        return start;
    }

    /**
     * @param start the creation date of the earliest desired tweet
     */
    public void setStart(Timestamp start) {
        this.start = start;
    }

    /**
     * @return the creation date of the latest tweet
     */
    public Timestamp getEnd() {
        return end;
    }

    /**
     * @param end the creation date of the latest desired tweet
     */
    public void setEnd(Timestamp end) {
        this.end = end;
    }

    /**
     * @return a {@link List} of the {@link sentiments.domain.model.Language} of the desired tweets
     */
    public List<String> getLanguages() {
        return languages;
    }

    /**
     * @param languages a {@link List} of the {@link sentiments.domain.model.Language} of the desired tweets
     */
    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    /**
     * @return a {@link List} of the hashtags of the desired tweets
     */
    public List<String> getHashtags() {
        return hashtags;
    }

    /**
     * @param hashtags a {@link List} of the hashtags of the desired tweets
     */
    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    /**
     * @return the time at which the desired tweets were classified as a {@link Timestamp}
     */
    public Timestamp getClassified() {
        return classified;
    }

    /**
     * @param classified the time at which the desired tweets were classified as a {@link Timestamp}
     */
    public void setClassified(Timestamp classified) {
        this.classified = classified;
    }
}
