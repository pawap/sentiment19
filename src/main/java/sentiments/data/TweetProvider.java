package sentiments.data;

import sentiments.domain.model.AbstractTweet;

public interface TweetProvider {

    public AbstractTweet createTweet();
}
