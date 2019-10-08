package sentiments.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sentiments.domain.model.DayStats;

import java.time.LocalDate;

/**
 * @author 6runge
 */
public interface DayStatsRepository extends MongoRepository<DayStats, Integer> {

    int findOffensiveByDateEquals(LocalDate date);

    int findNonffensiveByDateEquals(LocalDate date);

    int findOffensiveByDateEqualsAndLanguageEquals(LocalDate date, String language);

    int findNonffensiveByDateEqualsAndLanguageEquals(LocalDate date, String language);

    DayStats findAllByDateEquals(LocalDate date);

    Iterable<DayStats> findByDateBetweenAndLanguage(LocalDate start, LocalDate end, String language);
}
