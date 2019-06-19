package sentiments.ml;

import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import sentiments.domain.model.Tweet;
import sentiments.domain.repository.TweetRepository;

import java.util.Iterator;

/**
 * Implementation of the dl4j sentenceIterator interface. used to iterate over the texts of all available tweets.
 *
 * @author 6runge
 */
public class TweetSentenceIterator implements SentenceIterator {
    private SentencePreProcessor preProcessor;
    private TweetRepository tweetRepository;
    private Iterator<Tweet> tweets;

    /*
     * Constructor
     * @param sentencePreProcessor The PreProcessor used on all the tweets.
     * @param tweetRepository the repository containing the tweets to iterate over
     */
    public TweetSentenceIterator(SentencePreProcessor sentencePreProcessor, TweetRepository tweetRepository) {
        this.preProcessor = sentencePreProcessor;
        this.tweetRepository = tweetRepository;
        this.tweets = tweetRepository.findAll().iterator();
    }

    public TweetSentenceIterator(TweetRepository tweetRepository) {
        this(new SentencePreProcessor() {
            @Override
            public String preProcess(String sentence) {
                return sentence.replaceAll("[^a-zA-Z ]|^(https?|ftp)://.*$", "").toLowerCase();
            }
        }, tweetRepository);
    }

    @Override
    public String nextSentence() {
        String text = tweets.next().getText();
        if (preProcessor != null) {
            text = preProcessor.preProcess(text);
        }
        return text;
    }

    @Override
    public boolean hasNext() {
        return tweets.hasNext();
    }

    @Override
    public void reset() {
        this.tweets = tweetRepository.findAll().iterator();
    }

    @Override
    public void finish() {

    }

    @Override
    public SentencePreProcessor getPreProcessor() {
        return preProcessor;
    }

    @Override
    public void setPreProcessor(SentencePreProcessor preProcessor) {
        this.preProcessor = preProcessor;
    }
}
