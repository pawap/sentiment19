package sentiments.domain.repository.tweet;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import sentiments.domain.model.tweet.Tweet;

import java.sql.Timestamp;
import java.util.Date;
import java.util.stream.Stream;

/**
 * @author Paw, 5malfa
 *
 */
public interface TweetRepository extends MongoRepository<Tweet, Integer>, TweetRepositoryCustom {

	//returns an Iterable with every (non-) offensive Tweet
	@Query(value="{}", count = true)
	public int countfindAllTweets();

	//returns an int with the count of every (non-) offensive Tweet
	@Query(value = "{ 'offensive' : ?0 }", count = true)
	public int countByOffensive(Boolean offensive);

	//returns an int with the count of Tweets with a timestamp inbetween startdate and enddate
	@Query(value = "{ 'tmstamp' : { $gte: ?0, $lte: ?1} }", count = true)
	public int countfindAllByDateBetween(Timestamp startdate, Timestamp enddate);

}
