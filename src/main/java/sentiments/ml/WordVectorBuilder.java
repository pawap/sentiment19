package sentiments.ml;

import org.deeplearning4j.models.embeddings.learning.impl.elements.SkipGram;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import sentiments.domain.repository.TweetRepository;

import java.io.IOException;

/**
 * Builds Wordvectors out of the Tweets in a given {@link TweetRepository}.
 *
 * @author 6runge
 */
public class WordVectorBuilder {

    private TweetRepository tweetRepo;

    /**
     * Returns the path where the word vectors are persisted.
     * @return the file path of the model file
     */

    /**
     * Constructor
     * @param tweetRepository the repository where the tweets to be learned from are stored.
     */
    public WordVectorBuilder(TweetRepository tweetRepository){
        this.tweetRepo = tweetRepository;
    }


    /**
     * Builds wordvectors and persists them at a location accessible via {@see sentiments.data.ml.WordVectorService#getWordVectorPath()}.
     * @throws IOException
     */
    public  void train() throws IOException {
        SentenceIterator sentenceIterator = new TweetSentenceIterator(tweetRepo);
        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .layerSize(300)
                .windowSize(4)
                .seed(42)
                .epochs(5)
                .elementsLearningAlgorithm(new SkipGram<VocabWord>())
                .iterate(sentenceIterator)
                .tokenizerFactory(tokenizerFactory)
                .build();
        vec.fit();

        WordVectorSerializer.writeWord2VecModel(vec, WordVectorsService.getWordVectorPath());
    }
}

