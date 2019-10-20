package sentiments.domain.preprocessor;

import sentiments.domain.model.tweet.AbstractTweet;

/**
 * Removes unwanted substrings (e.g. URLs) from the {@link AbstractTweet}'s text.
 * @author 6koch, Paw
 */
public class BasicTweetPreProcessor implements TweetPreProcessor {

    @Override
    public void preProcess(AbstractTweet tweet) {

        String oldText = tweet.getText();
        String newText = oldText.replaceAll("(((RT )?@[\\w_-]+[:]?)|((https?:\\/\\/)[\\w\\d.-\\/]*))","");
        
        tweet.setText(newText);
    }

    @Override
    public void destroy() {

    }
}
