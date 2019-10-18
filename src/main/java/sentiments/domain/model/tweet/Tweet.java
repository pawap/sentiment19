package sentiments.domain.model.tweet;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;
import java.util.Set;


/**
 * @author Paw
 *
 */
@Entity
public class Tweet extends AbstractTweet {


    @Indexed(unique = true)
    private String twitterId;

    @Id
    private ObjectId _id;

    private Set<String> hashtags;

    private Date classified;

    public String getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(String twitterId) {
        this.twitterId = twitterId;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
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
