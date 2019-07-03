package sentiments.domain.repository;

import java.sql.Timestamp;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import sentiments.domain.model.Tweet;

/**
 * @author Paw
 *
 */
public interface TweetRepository extends MongoRepository<Tweet, Integer>, TweetRepositoryCustom {

	//returns an Iterable with every (non-) offensive Tweet
	@Query(value="{}", count = true)
	public int countfindAllTweets();


	//returns an Iterable with every (non-) offensive Tweet
	@Query("{ 'offensive' : ?0 }")
	public Iterable<Tweet> findAllByOffensive(Boolean offensive);

	//returns an int with the count of every (non-) offensive Tweet
	@Query(value = "{ 'offensive' : ?0 }", count = true)
	public int countByOffensive(Boolean offensive);


	//returns an Iterable with every (non-) offensive Tweet within a timerange
	@Query(value = "{ '$and' : [{'offensive' : ?0},{ 'tmstamp' : {$gte: ?0, $lte: ?1}}]}")
	public Iterable<Tweet> findAllByOffensiveAndDate( Boolean offensive, Timestamp startdate, Timestamp enddate);

	//returns an Iterable with every (non-) offensive Tweet within a timerange
	//@Query(value = "{ '$and' : [{'offensive' : ?0},{ 'tmstamp' : {$gte: ?0, $lte: ?1}}]}", count = true)
	@Query(value = "{ 'offensive' : ?0, 'crdate': { $gte: ?1, $lte: ?2} }", count = true)
	public int countByOffensiveAndDate( Boolean offensive, Timestamp startdate, Timestamp enddate);


	//returns an Iterable with every Tweet with a timestamp inbetween startdate and enddate
	@Query(value = "{ 'tmstamp' : { $gte: ?0, $lte: ?1} }")
	public Iterable<Tweet> findAllByDateBetween(Timestamp startdate, Timestamp enddate);

	//returns an int with the count of Tweets with a timestamp inbetween startdate and enddate
	@Query(value = "{ 'tmstamp' : { $gte: ?0, $lte: ?1} }", count = true)
	public int countfindAllByDateBetween(Timestamp startdate, Timestamp enddate);
}
