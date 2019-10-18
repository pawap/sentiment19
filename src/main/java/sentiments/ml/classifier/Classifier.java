package sentiments.ml.classifier;

import sentiments.domain.model.tweet.Tweet;
import sentiments.domain.repository.tweet.TrainingTweetRepository;

import java.util.Date;
import java.util.List;

/**
 * @author Paw
 */
public interface Classifier {

    Classification classifyTweet(String tweet);

    void train(TrainingTweetRepository tweetRepository);

    boolean isTrained();

    void classifyTweets(List<Tweet> tweetList, Date runDate);
}
