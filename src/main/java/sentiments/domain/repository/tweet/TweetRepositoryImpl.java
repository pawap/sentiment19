package sentiments.domain.repository.tweet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.SampleOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import sentiments.domain.model.query.*;
import sentiments.domain.model.tweet.Tweet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * @author paw, 6runge
 */
public class TweetRepositoryImpl implements TweetRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public TweetRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Timeline countByOffensiveAndDayInInterval(TweetFilter tweetFilter) {
        List<AggregationOperation> list = getWhereOperations(tweetFilter);
        list.add(Aggregation.project()
                .andExpression("year(crdate)").as("year")
                .andExpression("month(crdate)").as("month")
                .andExpression("dayOfMonth(crdate)").as("day1"));
        list.add(Aggregation.group(Aggregation.fields().and("year").and("month").and("day1"))
                .count().as("count"));
        list.add(Aggregation.sort(Sort.Direction.ASC, "year", "month", "day1"));
        list.add(Aggregation.project("count").andExpression("concat(substr(year,0,-1),'-',substr(month,0,-1),'-',substr(day1,0,-1))").as("day"));

        Aggregation agg = Aggregation.newAggregation(list);

        List<Integer> result = new LinkedList<>();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-M-d", Locale.US);

        // init StartDate
        LocalDate start;

        if (tweetFilter.getStart() != null) {
            start = tweetFilter.getStart().toLocalDateTime().toLocalDate();
        } else {
            start = getFirstDate();
        }

        // init EndDate
        LocalDate end;

        if (tweetFilter.getEnd() != null) {
            end = tweetFilter.getEnd().toLocalDateTime().toLocalDate();
        } else {
            end = getLastDate();
        }

        //Loop over Aggregation-Result and fill in 0 for missing days.
        LocalDate current = start;

        for (DayCount i : mongoTemplate.aggregate(agg, Tweet.class, DayCount.class)) {
            LocalDate currentDate = LocalDate.parse(i.day, f);
            while (current.compareTo(currentDate) < 0) {
                result.add(0);
                current = current.plusDays(1);
            }
            current = current.plusDays(1);
            result.add(i.count);
        }
        while (current.compareTo(end) < 0) {
            result.add(0);
            current = current.plusDays(1);
        }
        Timeline timeline = new Timeline();
        timeline.start = start;
        timeline.end = end;
        timeline.timeline = result;
        return timeline;
    }

    @Override
    public LocalDate getFirstDate() {
        Tweet first = mongoTemplate.aggregate(Aggregation.newAggregation(
                Aggregation.sort(Sort.Direction.ASC, "crdate"),
                Aggregation.limit(1)
        ), Tweet.class, Tweet.class).getUniqueMappedResult();
        if (first == null){
            return null;
        }
        return first.getCrdate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    @Override
    public LocalDate getLastDate() {
        Tweet last = mongoTemplate.aggregate(Aggregation.newAggregation(
                Aggregation.sort(Sort.Direction.DESC, "crdate"),
                Aggregation.limit(1)
        ), Tweet.class, Tweet.class).getUniqueMappedResult();
        if (last == null){
            return null;
        }
        return last.getCrdate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    @Override
    public String getRandomTwitterId(TweetFilter tweetFilter) {
        SampleOperation sampleStage = Aggregation.sample(1);
        List<AggregationOperation> list = getWhereOperations(tweetFilter);
        list.add(Aggregation.match(Criteria.where("twitterId").exists(true)));
        list.add(sampleStage);
        list.add(Aggregation.project("twitterId").andExclude("_id"));
        Aggregation aggregation = Aggregation.newAggregation(list);
        AggregationResults<Tweet> output = mongoTemplate.aggregate(aggregation, Tweet.class, Tweet.class);
        List<Tweet> mappedOutput = output.getMappedResults();
        String result = (mappedOutput.size() > 0) ? mappedOutput.get(0).getTwitterId() : null;
        return result;
    }

    @Override
    public int countByOffensiveAndDate(TweetFilter tweetFilter){

        List<AggregationOperation> list = getWhereOperations(tweetFilter);
        list.add(Aggregation.group().count().as("count"));
        list.add(Aggregation.project("count").andExclude("_id"));
        Aggregation aggregation = Aggregation.newAggregation(list);

        AggregationResults<Count> output = mongoTemplate.aggregate(aggregation, Tweet.class, Count.class);
        List<Count> mappedOutput = output.getMappedResults();
        int result = (mappedOutput.size() > 0) ? mappedOutput.get(0).count : 0;
        return result;
    }

    @Override
    public List<HashtagCount> getMostPopularHashtags(TweetFilter tweetFilter, int limit) {

        List<AggregationOperation> list = getWhereOperations(tweetFilter);

        list.add(Aggregation.unwind("hashtags"));
        list.add(Aggregation.group(Aggregation.fields().and("hashtags")).count().as("count"));
        list.add(Aggregation.sort(Sort.Direction.DESC, "count"));
        list.add(Aggregation.project("count").andExpression("_id").as( "hashtag"));
        list.add(Aggregation.limit(limit));

        Aggregation aggregation = Aggregation.newAggregation(list);
        AggregationResults<HashtagCount> output = mongoTemplate.aggregate(aggregation, Tweet.class, HashtagCount.class);

        return output.getMappedResults();

    }

    @Override
    public BulkOperations getBulkOps() {
        return mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Tweet.class);
    }

    private List<AggregationOperation> getWhereOperations(TweetFilter tweetFilter) {
        List<AggregationOperation> list = new LinkedList<>();

        //offensive?
        //TODO what if null?
        list.add(Aggregation.match(Criteria.where("offensive").is(tweetFilter.isOffensive())));
        //timeframe
        Criteria c = Criteria.where("crdate");
        boolean addTimeQuery = false;
        if (tweetFilter.getStart() != null) {
            LocalDateTime start = LocalDateTime.ofInstant(tweetFilter.getStart().toInstant(), ZoneId.of("UTC")).withHour(0).withMinute(0).withSecond(0);
            c = c.gte(start.toInstant(ZoneOffset.UTC));
            addTimeQuery = true;
        }
        if (tweetFilter.getEnd() != null) {
            LocalDateTime end = LocalDateTime.ofInstant(tweetFilter.getEnd().toInstant(), ZoneId.of("UTC")).withHour(23).withMinute(59).withSecond(59);
            c = c.lte(end.toInstant(ZoneOffset.UTC));
            addTimeQuery = true;
        }
        if (addTimeQuery) {
            list.add(Aggregation.match(c));
        }
        //hashtags
        if (tweetFilter.getHashtags() != null && !tweetFilter.getHashtags().isEmpty()) {
            list.add(Aggregation.match(Criteria.where("hashtags").all(tweetFilter.getHashtags())));
        }

        //languages
        if (tweetFilter.getLanguages() != null && !tweetFilter.getLanguages().isEmpty() ) {
            list.add(Aggregation.match(Criteria.where("language").in(tweetFilter.getLanguages())));
        }
        if (tweetFilter.getClassified() != null) {
            list.add(Aggregation.match(Criteria.where("classified").is(tweetFilter.isOffensive())));
        }

        return list;
    }

}
