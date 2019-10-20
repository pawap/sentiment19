package sentiments.ml.service;

import org.deeplearning4j.models.embeddings.learning.impl.elements.SkipGram;
import org.deeplearning4j.models.sequencevectors.interfaces.VectorsListener;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import sentiments.domain.model.Language;
import sentiments.domain.repository.tweet.TweetRepository;
import sentiments.ml.iterator.TweetSentenceIterator;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Builds Wordvectors out of the Tweets in a given {@link Language} in a given {@link TweetRepository}.
 *
 * @author 6runge
 */
public class WordVectorBuilder {

    private TweetRepository tweetRepo;

    /**
     * Constructor
     * @param tweetRepository the repository where the tweets to be learned from are stored.
     */
    public WordVectorBuilder(TweetRepository tweetRepository){
        this.tweetRepo = tweetRepository;
    }


    /**
     * Builds wordvectors and persists them at a location accessible via {@see sentiments.data.ml.WordVectorService#getWordVectorPath()}.
     * @param language the desired language of the word vectors
     * @throws IOException
     */
    public  void train(Language language) throws IOException {
        SentenceIterator sentenceIterator = new TweetSentenceIterator(tweetRepo, language);
        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .layerSize(300)
                .windowSize(4)
                .seed(42)
                .epochs(5)
                .elementsLearningAlgorithm(new SkipGram<>())
                .iterate(sentenceIterator)
                .tokenizerFactory(tokenizerFactory)
                .build();
        Set<VectorsListener<VocabWord>> set = new HashSet<>();
        //set.add(new ScoreListener<>(ListenerEvent.ITERATION, 1));
        vec.setEventListeners(set);
        vec.fit();
        WordVectorsService.saveWordVectors(vec, language);
    }
}

