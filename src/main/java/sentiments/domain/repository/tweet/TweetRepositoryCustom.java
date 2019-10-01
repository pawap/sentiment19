package sentiments.domain.repository.tweet;

import org.springframework.data.mongodb.core.BulkOperations;
import sentiments.domain.model.query.HashtagCount;
import sentiments.domain.model.query.Timeline;
import sentiments.domain.model.query.TweetFilter;

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

    BulkOperations getBulkOps();
}
