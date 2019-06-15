package sentiments;

import java.sql.Timestamp;

import javax.persistence.Entity;


/**
 * @author Paw
 *
 */
@Entity
public class Tweet extends AbstractTweet {

    private String twitterId;

    public String getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(String twitterId) {
        this.twitterId = twitterId;
    }

}
