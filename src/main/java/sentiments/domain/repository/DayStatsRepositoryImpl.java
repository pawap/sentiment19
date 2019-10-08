package sentiments.domain.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import sentiments.domain.model.DayStats;

import java.time.LocalDate;
import java.util.Collection;

public class DayStatsRepositoryImpl implements DayStatsRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public DayStatsRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    public Iterable<DayStats> findByDateBetweenAndLanguageInOrderBy(LocalDate start, LocalDate end, Collection<String> language) {
        Query query =Query.query(Criteria.where("date").gte(start).lte(end).and("language").in(language));
        return mongoTemplate.find(query, DayStats.class);
    }

}
