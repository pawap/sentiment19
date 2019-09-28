package sentiments.ml;

import sentiments.domain.model.Classification;
import sentiments.domain.model.Language;
import sentiments.domain.model.Tweet;
import sentiments.domain.repository.TrainingTweetRepository;

import java.util.Date;
import java.util.List;

public interface Classifier {

    Classification classifyTweet(String tweet);

    void train(TrainingTweetRepository tweetRepository);

    boolean isTrained();

    void classifyTweets(List<Tweet> tweetList, Date runDate);
}
