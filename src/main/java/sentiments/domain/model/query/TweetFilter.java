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

    /*
     * Private constructor; use builder!
     */
    private TweetFilter(){}

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

    /**
     * Use this to build {@link TweetFilter}s.
     */
    public static class Builder {

        private Boolean offensive;
        private Timestamp start, end, classified;
        private List<String> languages;
        private List<String> hashtags;

        public Builder() {
            reset();
        }

        /**
         * Sets all parameters to null.
         */
        public void reset() {
            this.offensive = null;
            this.start = null;
            this.end = null;
            this.classified = null;
            this.languages = null;
            this.hashtags = null;
        }

        /**
         * @param offensive false/true, if the desired tweets should be (non-)offensive. null for either.
         */
        public Builder setOffensive(boolean offensive) {
            this.offensive =offensive;
            return this;
        }

        /**
         * @param start the creation date of the earliest desired tweet
         */
        public Builder setStart(Timestamp start) {
            this.start = start;
            return this;
        }

        /**
         * @param end the creation date of the latest desired tweet
         */
        public Builder setEnd(Timestamp end) {
            this.end = end;
            return this;
        }

        /**
         * @param classified the time at which the desired tweets were classified as a {@link Timestamp}
         */
        public Builder setClassified(Timestamp classified) {
            this.classified = classified;
            return this;
        }

        /**
         * @param languages a {@link List} of the {@link sentiments.domain.model.Language} of the desired tweets
         */
        public Builder setLanguages(List<String> languages) {
            this.languages = languages;
            return this;
        }

        /**
         * @param hashtags a {@link List} of the hashtags of the desired tweets
         */
        public Builder setHashtags(List<String> hashtags) {
            this.hashtags = hashtags;
            return this;
        }

        /**
         * Create a TweetFilter with the parameters set beforehand (preferably via method chaining).
         * Does NOT reset the parameters.
         * @return the desired {@link TweetFilter}
         */
        public TweetFilter build() {
            TweetFilter tweetFilter = new TweetFilter();
            tweetFilter.setOffensive(this.offensive);
            tweetFilter.setStart(this.start);
            tweetFilter.setEnd(this.end);
            tweetFilter.setClassified(this.classified);
            tweetFilter.setLanguages(this.languages);
            tweetFilter.setHashtags(this.hashtags);

            return tweetFilter;
        }
    }
}
