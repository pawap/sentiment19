package sentiments.domain.repository;

import sentiments.domain.model.TweetFilter;

import java.util.List;

/**
 * @author paw, 6runge
 */
public interface TweetRepositoryCustom {
    List<Integer> countByOffensiveAndDayInInterval(TweetFilter tweetFilter);

    String getRandomTwitterId(TweetFilter tweetFilter);
}
