package sentiments.domain.repository;

import sentiments.domain.model.TweetQuery;

import java.util.List;

/**
 * @author paw, 6runge
 */
public interface TweetRepositoryCustom {
    List<Integer> countByOffensiveAndDayInInterval(TweetQuery tweetQuery);

    String getRandomTwitterId(TweetQuery tweetQuery);
}
