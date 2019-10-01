package sentiments.domain.preprocessor;

import sentiments.domain.model.tweet.AbstractTweet;

/**
 * @author Paw
 */
public interface TweetPreProcessor {

    /**
     * @param tweet Tweet
     */
    public void preProcess(AbstractTweet tweet);

    void destroy();
}
