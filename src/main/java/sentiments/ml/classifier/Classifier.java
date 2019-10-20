package sentiments.ml.classifier;

import sentiments.domain.model.tweet.Tweet;
import sentiments.domain.repository.tweet.TrainingTweetRepository;

import java.util.Date;
import java.util.List;

/**
 * @author Paw
 */
public interface Classifier {

    /**
     * Classify a {@link Tweet}.
     * @param tweet the {@link Tweet} to be classified
     * @return a classification for the input
     */
    Classification classifyTweet(String tweet);

    /**
     * Prepare the classifier for classification.
     * @param tweetRepository the repository containing the training and test data
     */
    void train(TrainingTweetRepository tweetRepository);

    /**
     * @return true, if the classifier is ready for classification
     */
    boolean isTrained();

    /**
     * Classify a list of {@link Tweet}s
     * @param tweetList a lit of {@link Tweet}s to be classified
     * @param runDate the date associated with the classification run
     */
    void classifyTweets(List<Tweet> tweetList, Date runDate);
}
