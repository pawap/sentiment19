package sentiments;

import java.sql.Timestamp;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * @author Paw
 *
 */
public interface TweetRepository extends CrudRepository<Tweet, Integer> {
	
	@Query("from Tweet where offensive=:offensive")
	public Iterable<Tweet> findAllByOffensive(@Param("offensive") Boolean offensive);
	
	@Query("from Tweet where offensive=:offensive and crdate>=:startdate and crdate<=:enddate")
	public Iterable<Tweet> findAllByOffensiveAndDate(@Param("offensive") Boolean offensive, @Param("startdate") Timestamp startdate, @Param("enddate") Timestamp enddate);

	@Query("select count(*) from Tweet where offensive=:offensive")
	public int countByOffensive(@Param("offensive") Boolean offensive);
	
	@Query("select count(*) from Tweet where offensive=:offensive and crdate>=:startdate and crdate<=:enddate")
	public int countByOffensiveAndDate(@Param("offensive") Boolean offensive, @Param("startdate") Timestamp startdate, @Param("enddate") Timestamp enddate);
}
