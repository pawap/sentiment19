package sentiments.ml.iterator;

import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import sentiments.domain.model.Language;
import sentiments.domain.model.tweet.Tweet;
import sentiments.domain.repository.tweet.TweetRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Implementation of the dl4j sentenceIterator interface. used to iterate over the texts of all available tweets
 * in a given {@link TweetRepository}.
 *
 * @author 6runge
 */
public class TweetSentenceIterator implements SentenceIterator {
    private String language;
    private SentencePreProcessor preProcessor;
    private TweetRepository tweetRepository;
    private Iterator<Tweet> tweets;
    private long count;

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
        reset();
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
        if (count++ % (1024*64) == 0) System.out.println(count);
        Tweet t = tweets.next();
        if (count % 499998 == 0) {
            LocalDateTime time = LocalDateTime.ofInstant(t.getCrdate().toInstant(), ZoneId.of("UTC"));
            tweets = tweetRepository.find500kByLanguageStartingFrom(language, time).iterator();
        }
        String text = t.getText();
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
        LocalDateTime start = LocalDateTime.of(2000,1,1,1,1,1,1);
        this.tweets = tweetRepository.find500kByLanguageStartingFrom(this.language, start).iterator();
        count = 0;
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
