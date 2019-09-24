package sentiments.domain.model;

import javax.persistence.Entity;
import java.util.Date;
import java.util.Set;


/**
 * @author Paw
 *
 */
@Entity
public class Tweet extends AbstractTweet {

    private String twitterId;

    private Set<String> hashtags;

    private Date classified;

    public String getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(String twitterId) {
        this.twitterId = twitterId;
    }

    public Set<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(Set<String> hashtags) {
        this.hashtags = hashtags;
    }

    public Date getClassified() {
        return classified;
    }

    public void setClassified(Date classified) {
        this.classified = classified;
    }
}
