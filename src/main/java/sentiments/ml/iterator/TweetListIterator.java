package sentiments.ml.iterator;

import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import sentiments.domain.model.Language;
import sentiments.domain.model.tweet.Tweet;
import sentiments.ml.service.WordVectorsService;

import java.util.*;

/**
 * @author Paw
 *
 */
public class TweetListIterator implements DataSetIterator{

    private final WordVectors wordVectors;
    private final int batchSize;
    private final int vectorSize;
    private int maxLength;
    private List<List<String>> tokenized;

    private int cursor = 0;
    private final TokenizerFactory tokenizerFactory;
	private int totalExamples;

    private List<Tweet> outputTweets;

    /**
     * @param language
     */
    public TweetListIterator(List<Tweet> tweets, Language language) {
        this.outputTweets = new LinkedList<>();
        this.batchSize = 512;
        this.wordVectors = WordVectorsService.getWordVectors(language);
        this.vectorSize = wordVectors.getWordVector(wordVectors.vocab().wordAtIndex(0)).length;
        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
        tokenizeTweets(tweets);
        this.reset();
       // System.out.println("totalExamples: " + totalExamples + "/ maxLength: " + maxLength + "/ Thread: " + Thread.currentThread().getName());
    }

    private void tokenizeTweets(List<Tweet> tweets) {
        tokenized = new ArrayList<>();
        this.maxLength = 0;
        for (Tweet t: tweets) {
            List<String> tokens = tokenizeTweet(t.getText());
            if (!tokens.isEmpty()) {
                tokenized.add(tokens);
                outputTweets.add(t);
                this.maxLength = Math.max(tokens.size(), this.maxLength);
            }
        }
        this.totalExamples = tokenized.size();
    }


    @Override
    public DataSet next(int num) {
        if (cursor >= totalExamples) throw new NoSuchElementException();
        return nextDataSet(num);

    }

    private DataSet nextDataSet(int num){
        //Create data for classifying
        //Here: we have reviews.size() examples of varying lengths
        INDArray features = Nd4j.create(new int[]{tokenized.size(), vectorSize, maxLength}, 'f');
        //Because we are dealing with reviews of different lengths and only one output at the final time step: use padding arrays
        //Mask arrays contain 1 if data is present at that time step for that example, or 0 if data is just padding
        INDArray featuresMask = Nd4j.zeros(tokenized.size(), maxLength);
        long time = System.currentTimeMillis();
        for( int i=0; i < batchSize && this.hasNext(); i++ ){

            List<String> tokens = tokenized.get(cursor);
            cursor++;

            // Get the truncated sequence length of document (i)
            int seqLength = Math.min(tokens.size(), maxLength);

            // Get all wordvectors for the current document and transpose them to fit the 2nd and 3rd feature shape
            final INDArray vectors = wordVectors.getWordVectors(tokens.subList(0, seqLength)).transpose();

            // Put wordvectors into features array at the following indices:
            // 1) Document (i)
            // 2) All vector elements which is equal to NDArrayIndex.interval(0, vectorSize)
            // 3) All elements between 0 and the length of the current sequence
            features.put(
                new INDArrayIndex[] {
                    NDArrayIndex.point(i), NDArrayIndex.all(), NDArrayIndex.interval(0, seqLength)
                },
                vectors);

            // Assign "1" to each position where a feature is present, that is, in the interval of [0, seqLength)
            featuresMask.get(new INDArrayIndex[] {NDArrayIndex.point(i), NDArrayIndex.interval(0, seqLength)}).assign(1);
        }
        return new DataSet(features, null, featuresMask,null);
    }

	private List<String> tokenizeTweet(String tweet) {
        List<String> tokens = tokenizerFactory.create(tweet).getTokens();
        List<String> tokensFiltered = new ArrayList<>();
        for(String t : tokens ){
            if(wordVectors.hasWord(t)) tokensFiltered.add(t);
        }
		return tokensFiltered;
	}


	public int totalExamples() {
        return totalExamples;
    }

    @Override
    public int inputColumns() {
        return vectorSize;
    }

    @Override
    public int totalOutcomes() {
        return 2;
    }

    @Override
    public void reset() {
        cursor = 0;
    }

    public boolean resetSupported() {
        return true;
    }

    @Override
    public boolean asyncSupported() {
        return true;
    }

    @Override
    public int batch() {
        return batchSize;
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor preProcessor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getLabels() {
        return Arrays.asList("offensive","nonoffensive");
    }

    @Override
    public boolean hasNext() {
        return cursor < totalExamples;
    }

    @Override
    public DataSet next() {
        return next(batchSize);
    }

    @Override
    public void remove() {

    }
    @Override
    public  DataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<Tweet> getOutputTweets() {
        return outputTweets;
    }

    public void setOutputTweets(List<Tweet> outputTweets) {
        this.outputTweets = outputTweets;
    }


}
