package sentiments.domain.repository;

import sentiments.domain.model.DayStats;

import java.time.LocalDate;
import java.util.Collection;

public interface DayStatsRepositoryCustom {


    Iterable<DayStats> findByDateBetweenAndLanguageInOrderBy(LocalDate start, LocalDate end, Collection<String> language);

}
