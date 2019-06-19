package sentiments.domain.preprocessor;

import sentiments.domain.model.AbstractTweet;

public class BasicTweetPreProcessor implements TweetPreProcessor {

    @Override
    public void preProcess(AbstractTweet tweet) {

        String oldText = tweet.getText();
        String newText = oldText.replaceAll("(((RT )?@[\\w_-]+[:]?)|((https?:\\/\\/)[\\w\\d.-\\/]*))","");
        
        tweet.setText(newText);
    }
}
