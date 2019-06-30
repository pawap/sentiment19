package sentiments.domain.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import sentiments.domain.model.Tweet;

public interface TrainingTweetRepository extends MongoRepository<Tweet,Integer> {

	@Query("{ 'offensive' : ?0 }")
	public Iterable<Tweet> findAllByOffensive(Boolean offensive);

	@Query("{ 'test' : ?1 , 'offensive' : ?0 }")
	public Iterable<Tweet> findAllByTestAndOffensive(Boolean test, Boolean offensive);

	@Query(value="{}", count = true)
	public int count(boolean test);
}
