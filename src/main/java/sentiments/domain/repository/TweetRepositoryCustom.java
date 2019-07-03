package sentiments.domain.repository;

import sentiments.domain.model.TweetQuery;

import java.util.List;

public interface TweetRepositoryCustom {
    List<Integer> countByOffensiveAndDayInInterval(TweetQuery tweetQuery);

    String getRandomTwitterId(TweetQuery tweetQuery);
}
