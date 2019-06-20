package sentiments.data;

import sentiments.domain.model.AbstractTweet;

import java.util.LinkedList;
import java.util.List;

abstract public class TweetProvider<T extends AbstractTweet> {

    public abstract T createTweet();

    public List<T> getNewTweetList() {
        return new LinkedList<T>();
    }
}
