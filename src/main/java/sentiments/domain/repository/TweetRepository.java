package sentiments.domain.repository;

import java.sql.Timestamp;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import sentiments.domain.model.Tweet;

/**
 * @author Paw
 *
 */
public interface TweetRepository extends MongoRepository<Tweet, Integer> {
	
	@Query("{ 'offensive' : ?0 }")
	public Iterable<Tweet> findAllByOffensive(Boolean offensive);
	
	@Query("{ 'offensive' : ?0 }")
	public Iterable<Tweet> findAllByOffensiveAndDate( Boolean offensive, Timestamp startdate, Timestamp enddate);

//	@Query("select count(*) from Tweet where offensive=:offensive")
	public int countByOffensive(@Param("offensive") Boolean offensive);
	
	@Query(value = "{ 'offensive' : ?0 }", count = true)
	public int countByOffensiveAndDate(@Param("offensive") Boolean offensive, @Param("startdate") Timestamp startdate, @Param("enddate") Timestamp enddate);
}
