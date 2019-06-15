package sentiments;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrainingTweetRepository extends TweetRepository {
	
	@Query("from Tweet where test=:test and offensive=:offensive")
	public Iterable<Tweet> findAllByTestAndOffensive(@Param("test") Boolean test, @Param("offensive") Boolean offensive);

	@Query("select count(*) from Tweet where test=:test")
	public int count(@Param("test") boolean test);
}
