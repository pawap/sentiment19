package sentiments.domain.model.tweet;

import javax.persistence.Entity;

/**
 * @author Paw
 */
@Entity
public class TrainingTweet extends AbstractTweet{
	
	private boolean test;
	
	public boolean isTest() {
		return test;
	}

	public void setTest(boolean test) {
		this.test = test;
	}
}
