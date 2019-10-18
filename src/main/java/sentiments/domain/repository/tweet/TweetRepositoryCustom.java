package sentiments.domain.repository.tweet;

import org.springframework.data.mongodb.core.BulkOperations;
import sentiments.domain.model.query.HashtagCount;
import sentiments.domain.model.query.Timeline;
import sentiments.domain.model.query.TweetFilter;
import sentiments.domain.model.tweet.Tweet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

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

    Stream<Tweet> find100kByLanguageStartingFrom(String language, LocalDateTime date);

    Stream<Tweet> find100kByClassifiedAndLanguage(Date classified, String language);


    BulkOperations getBulkOps();
}
