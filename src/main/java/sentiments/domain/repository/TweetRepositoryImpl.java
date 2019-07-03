package sentiments.domain.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.SampleOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import sentiments.domain.model.DayCount;
import sentiments.domain.model.Tweet;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TweetRepositoryImpl implements TweetRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public TweetRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Integer> countByOffensiveAndDayInInterval(Boolean offensive, Timestamp startdate, Timestamp enddate) {
        LocalDateTime start = LocalDateTime.ofInstant(startdate.toInstant(), ZoneId.of("UTC")).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = LocalDateTime.ofInstant(enddate.toInstant(), ZoneId.of("UTC")).withHour(23).withMinute(59).withSecond(59);
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("offensive").is(offensive)),
                Aggregation.match(Criteria.where("crdate").gte(start.toInstant(ZoneOffset.UTC)).lte(end.toInstant(ZoneOffset.UTC))),
                Aggregation.project()
                        .andExpression("year(crdate)").as("year")
                        .andExpression("month(crdate)").as("month")
                        .andExpression("dayOfMonth(crdate)").as("day1"),
                Aggregation.group(Aggregation.fields().and("year").and("month").and("day1"))
                        .count().as("count"),
                Aggregation.sort(Sort.Direction.ASC, "year","month","day1"),
                Aggregation.project("count").andExpression("concat(substr(year,0,-1),'-',substr(month,0,-1),'-',substr(day1,0,-1))").as("day")

        );
        List<Integer> result = new LinkedList<>();
        DateTimeFormatter f = DateTimeFormatter.ofPattern( "yyyy-M-d" , Locale.US );
        LocalDate current = startdate.toLocalDateTime().toLocalDate();

        for (DayCount i: mongoTemplate.aggregate(agg, Tweet.class, DayCount.class)) {
            LocalDate currentDate = LocalDate.parse(i.day, f);
            while (current.compareTo(currentDate) < 0) {
                result.add(0);
                current = current.plusDays(1);
            }
            current = current.plusDays(1);
            result.add(i.count);
        }
        while (current.compareTo(enddate.toLocalDateTime().toLocalDate()) < 0) {
            result.add(0);
            current = current.plusDays(1);
        }

        return result;
    }

    @Override
    public String getRandomTwitterId(boolean offensive) {
        SampleOperation sampleStage = Aggregation.sample(1);
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(Criteria.where("offensive").is(offensive)),
                sampleStage,
                Aggregation.project("twitterId").andExclude("_id"));
        AggregationResults<Tweet> output = mongoTemplate.aggregate(aggregation, Tweet.class, Tweet.class);
        List<Tweet> mappedOutput = output.getMappedResults();
        String result = (mappedOutput.size() > 0)? mappedOutput.get(0).getTwitterId(): null;
        return result;
    }
}
