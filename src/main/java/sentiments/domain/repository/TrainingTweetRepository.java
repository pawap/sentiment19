package sentiments.domain.repository;


import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import sentiments.domain.model.Tweet;

public interface TrainingTweetRepository extends TweetRepository {
	
//	@Query("from Tweet where test=:test and offensive=:offensive")
//	public Iterable<Tweet> findAllByTestAndOffensive(Boolean test, Boolean offensive);

	@Query(value="{}", count = true)
	public int count(boolean test);
}
