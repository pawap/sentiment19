package sentiments.domain.repository;

import sentiments.domain.model.HashtagCount;
import sentiments.domain.model.Timeline;
import sentiments.domain.model.TweetFilter;

import java.time.LocalDate;
import java.util.List;

/**
 * @author paw, 6runge
 */
public interface TweetRepositoryCustom {
    Timeline countByOffensiveAndDayInInterval(TweetFilter tweetFilter);

    LocalDate getFirstDate();

    LocalDate getLastDate();

    String getRandomTwitterId(TweetFilter tweetFilter);

    int countByOffensiveAndDate(TweetFilter tweetFilter);

    List<HashtagCount> getMostPopularHashtags(TweetFilter tweetFilter, int limit);
}
