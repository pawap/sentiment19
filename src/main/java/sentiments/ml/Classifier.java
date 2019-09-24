package sentiments.ml;

import sentiments.domain.model.Classification;
import sentiments.domain.model.Language;
import sentiments.domain.repository.TrainingTweetRepository;

public interface Classifier {

    Classification classifyTweet(String tweet);

    void train(TrainingTweetRepository tweetRepository);

    boolean isTrained();
}
