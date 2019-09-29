package sentiments.domain.service;

import sentiments.domain.model.TweetFilter;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author paw, 6runge
 */
public class TweetFilterBuilder {

    private TweetFilter tweetFilter;

    public TweetFilterBuilder() {
        this.tweetFilter = new TweetFilter();
    }

    public TweetFilter build() {
        return this.tweetFilter;
    }

    public TweetFilterBuilder setOffensive(boolean offensive) {
        this.tweetFilter.setOffensive(offensive);
        return this;
    }

    public TweetFilterBuilder setStart(Timestamp start) {
        this.tweetFilter.setStart(start);
        return this;
    }

    public TweetFilterBuilder setEnd(Timestamp end) {
        this.tweetFilter.setEnd(end);
        return this;
    }

    public TweetFilterBuilder setLanguages(List<String> languages) {
        this.tweetFilter.setLanguages(languages);
        return this;
    }

    public TweetFilterBuilder setHashtags(List<String> hashtags) {
        this.tweetFilter.setHashtags(hashtags);
        return this;
    }

}
