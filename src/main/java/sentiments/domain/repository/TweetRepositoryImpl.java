package sentiments.domain.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import sentiments.domain.model.DayCount;
import sentiments.domain.model.Tweet;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TweetRepositoryImpl implements TweetRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public TweetRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Integer> countByOffensiveAndDayInInterval(Boolean offensive, Timestamp startdate, Timestamp enddate) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("offensive").is(offensive)),
                Aggregation.match(Criteria.where("crdate").gte(startdate)),
                Aggregation.match(Criteria.where("crdate").lte(enddate)),
                Aggregation.project()
                        .andExpression("year(crdate)").as("year")
                        .andExpression("month(crdate)").as("month")
                        .andExpression("dayOfMonth(crdate)").as("day"),
                Aggregation.group(Aggregation.fields().and("year").and("month").and("day"))
                        .count().as("count"),
                Aggregation.project("count")
        );
        System.out.print(agg);
        List<Integer> result = new ArrayList<Integer>();
        for (DayCount i: mongoTemplate.aggregate(agg, Tweet.class, DayCount.class)) {
            result.add(i.count);
        }

        return result;
    }
}
