package sentiments.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sentiments.domain.model.DayStats;

import java.time.LocalDate;

/**
 * MongoRepository for {@link DayStats}
 * @author 6runge
 */
public interface DayStatsRepository extends MongoRepository<DayStats, Integer>, DayStatsRepositoryCustom {

    int findOffensiveByDateEquals(LocalDate date);

    int findNonffensiveByDateEquals(LocalDate date);

    int findOffensiveByDateEqualsAndLanguageEquals(LocalDate date, String language);

    int findNonffensiveByDateEqualsAndLanguageEquals(LocalDate date, String language);

    DayStats findAllByDateEquals(LocalDate date);

}
