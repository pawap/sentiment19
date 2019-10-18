package sentiments.domain.repository.tweet;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import sentiments.domain.model.tweet.TrainingTweet;
import sentiments.domain.model.tweet.Tweet;

/**
 * @author Paw, 5malfa
 */
public interface TrainingTweetRepository extends MongoRepository<TrainingTweet,Integer> {

	//returns an Iterable of all tweets that are labled as test/training and (non-) offensive
	@Query("{ 'test' : ?0 , 'offensive' : ?1 , 'language': ?2}")
	public Iterable<Tweet> findAllByTestAndOffensiveAndLanguage(Boolean test, Boolean offensive, String language);

	//returns the count of test/training samples
	@Query(value="{ 'test' : ?0 }", count = true)
	public int countByTestAndLanguage(boolean test);
}
