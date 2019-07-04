package sentiments.domain.service;

import sentiments.domain.model.TweetQuery;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author paw, 6runge
 */
public class TweetQueryBuilder {

    private TweetQuery tweetQuery;

    public TweetQueryBuilder() {
        this.tweetQuery = new TweetQuery();
    }

    public TweetQuery build() {
        return this.tweetQuery;
    }

    public TweetQueryBuilder setOffensive(boolean offensive) {
        this.tweetQuery.setOffensive(offensive);
        return this;
    }

    public TweetQueryBuilder setStart(Timestamp start) {
        this.tweetQuery.setStart(start);
        return this;
    }

    public TweetQueryBuilder setEnd(Timestamp end) {
        this.tweetQuery.setEnd(end);
        return this;
    }

    public TweetQueryBuilder setLanguages(List<String> languages) {
        this.tweetQuery.setLanguages(languages);
        return this;
    }

    public TweetQueryBuilder setHashtags(List<String> hashtags) {
        this.tweetQuery.setHashtags(hashtags);
        return this;
    }

}
