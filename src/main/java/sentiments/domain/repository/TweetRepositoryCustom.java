package sentiments.domain.repository;

import java.sql.Timestamp;
import java.util.List;

public interface TweetRepositoryCustom {
    List<Integer> countByOffensiveAndDayInInterval(Boolean offensive, Timestamp startdate, Timestamp enddate);

    String getRandomTwitterId(boolean offensive);
}
