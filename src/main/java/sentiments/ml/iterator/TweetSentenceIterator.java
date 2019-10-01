package sentiments.ml.iterator;

import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import sentiments.domain.model.Language;
import sentiments.domain.model.tweet.Tweet;
import sentiments.domain.repository.tweet.TweetRepository;

import java.util.Iterator;

/**
 * Implementation of the dl4j sentenceIterator interface. used to iterate over the texts of all available tweets
 * in agiven {@link TweetRepository}.
 *
 * @author 6runge
 */
public class TweetSentenceIterator implements SentenceIterator {
    private String language;
    private SentencePreProcessor preProcessor;
    private TweetRepository tweetRepository;
    private Iterator<Tweet> tweets;

    /**
     * Constructor
     * @param sentencePreProcessor The PreProcessor used on all the tweets.
     * @param tweetRepository the repository containing the tweets to iterate over
     * @param language
     */
    public TweetSentenceIterator(SentencePreProcessor sentencePreProcessor, TweetRepository tweetRepository, Language language) {
        this.preProcessor = sentencePreProcessor;
        this.tweetRepository = tweetRepository;
        this.language = language.getIso();
        this.tweets = tweetRepository.findAllByLanguage(this.language).iterator();

    }

    /**
     * Constructor
     * @param tweetRepository the repository containing the tweets to iterate over
     * @param language
     */
    public TweetSentenceIterator(TweetRepository tweetRepository, Language language) {
        this(new SentencePreProcessor() {
            @Override
            public String preProcess(String sentence) {
                return sentence.replaceAll("[^a-zA-Z ]", "").toLowerCase();
            }
        }, tweetRepository, language);
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
