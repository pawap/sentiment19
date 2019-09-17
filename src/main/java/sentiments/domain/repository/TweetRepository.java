package sentiments.domain.repository;

import java.sql.Timestamp;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import sentiments.domain.model.Tweet;

/**
 * @author Paw
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

    Iterable<Tweet> findAllByLanguage(String language);
}
