package sentiments.domain.preprocessor;

import sentiments.domain.model.AbstractTweet;

import java.io.IOException;

/**
 * @author Paw
 */
public interface TweetPreProcessor {

    /**
     * @param tweet Tweet
     */
    public void preProcess(AbstractTweet tweet) throws IOException;

    void destroy();
}
