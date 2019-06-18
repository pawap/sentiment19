package sentiments.domain.preprocessor;

import sentiments.domain.model.AbstractTweet;
import sentiments.domain.model.Tweet;

import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ImportTweetPreProcessor implements TweetPreProcessor {

    @Override
    public void preProcess(AbstractTweet tweet) {
        String text = tweet.getText();
        Set<String> hashtags = Pattern.compile("#[\\w_-]+[:]?")
                .matcher(text)
                .results()
                .map(MatchResult::group)
                .collect(Collectors.toSet());
        ((Tweet) tweet).setHashtags(hashtags);

    }
}
