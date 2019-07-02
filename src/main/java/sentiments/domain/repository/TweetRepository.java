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
	
	@Query("{ 'offensive' : ?0 }")
	public Iterable<Tweet> findAllByOffensive(Boolean offensive);
	
	@Query("{ 'offensive' : ?0 }")
	public Iterable<Tweet> findAllByOffensiveAndDate( Boolean offensive, Timestamp startdate, Timestamp enddate);

	@Query(value = "{ 'offensive' : ?0 }", count = true)
	public int countByOffensive(@Param("offensive") Boolean offensive);
	
	@Query(value = "{ 'offensive' : ?0, 'crdate': { $gte: ?1, $lte: ?2} }", count = true)
	public int countByOffensiveAndDate(@Param("offensive") Boolean offensive, Timestamp startdate, Timestamp enddate);

}
