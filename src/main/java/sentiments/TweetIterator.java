package sentiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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

/**
 * @author Paw
 *
 */
public class TweetIterator implements DataSetIterator{

    private final WordVectors wordVectors;
    private final int batchSize;
    private final int vectorSize;
    private final int truncateLength;

    private int cursor = 0;
    private Iterator<Tweet> offensiveTweets;
    private Iterator<Tweet> nonoffensiveTweets;
    private final TokenizerFactory tokenizerFactory;
	private int totalExamples;
	private TweetRepository tweetRepository;
	private boolean train;

    /**
     * @param dataDirectory the directory of the IMDB review data set
     * @param wordVectors WordVectors object
     * @param batchSize Size of each minibatch for training
     * @param truncateLength If reviews exceed
     * @param train If true: return the training data. If false: return the testing data.
     */
    public TweetIterator(TrainingTweetRepository tweetRepository, WordVectors wordVectors, int batchSize, int truncateLength, boolean test) {
        this.batchSize = batchSize;
        this.vectorSize = wordVectors.getWordVector(wordVectors.vocab().wordAtIndex(0)).length;
        this.tweetRepository = tweetRepository;
        this.reset();
        this.wordVectors = wordVectors;
        this.truncateLength = truncateLength;
        this.train = train;
        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
        System.out.println("totalExamples: " + totalExamples);
    }


    @Override
    public DataSet next(int num) {
        if (cursor >= totalExamples) throw new NoSuchElementException();
        return nextDataSet(num);

    }

    private DataSet nextDataSet(int num){
        //First: load reviews to String. Alternate positive and negative reviews
        //Second: tokenize reviews and filter out unknown words
        List<List<String>> allTokens = new ArrayList<>();
        int maxLength = 0;
        System.out.println("num: " + num);
        boolean[] offensive = new boolean[num];
        String tweet;
        for (int i = 0; i < num && cursor < totalExamples(); i++ ){
        	if (offensiveTweets.hasNext() && (!nonoffensiveTweets.hasNext() || (cursor % 3 == 0))) {
        		tweet = offensiveTweets.next().getText();
                offensive[i] = true;  
            } else {
        		tweet = nonoffensiveTweets.next().getText();
        		offensive[i] = false;
            }
    		List<String> tokens = tokenizeTweet(tweet);
            if (tokens.isEmpty()) {
            	i--;
            	totalExamples--;
            	System.out.println("No tokens for " + (offensive[i]? "" : "non") + "offensive Tweet: " + tweet);
            } else {
            	allTokens.add(tokens);
                maxLength = Math.max(maxLength,tokens.size());
                cursor++;
            } 
        }

        //If longest review exceeds 'truncateLength': only take the first 'truncateLength' words
        if(maxLength > truncateLength) maxLength = truncateLength;

        //Create data for training
        //Here: we have reviews.size() examples of varying lengths
        INDArray features = Nd4j.create(new int[]{allTokens.size(), vectorSize, maxLength}, 'f');
        INDArray labels = Nd4j.create(new int[]{allTokens.size(), 2, maxLength}, 'f');    //Two labels: positive or negative
        //Because we are dealing with reviews of different lengths and only one output at the final time step: use padding arrays
        //Mask arrays contain 1 if data is present at that time step for that example, or 0 if data is just padding
        INDArray featuresMask = Nd4j.zeros(allTokens.size(), maxLength);
        INDArray labelsMask = Nd4j.zeros(allTokens.size(), maxLength);

        for( int i=0; i < allTokens.size(); i++ ){
            List<String> tokens = allTokens.get(i);
//            System.out.println("Tweet: " + offensive[i]);
//            for (String token: tokens) {
//            	System.out.print(token + ", ");
//            	
//            }
//            System.out.println();
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

            int idx = (offensive[i] ? 0 : 1);
            int lastIdx = Math.min(tokens.size(),maxLength);
            labels.putScalar(new int[]{i,idx,lastIdx-1},1.0);   //Set label: [0,1] for negative, [1,0] for positive
            labelsMask.putScalar(new int[]{i,lastIdx-1},1.0);   //Specify that an output exists at the final time step for this example
        }

        return new DataSet(features,labels,featuresMask,labelsMask);
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
        this.offensiveTweets = tweetRepository.findAllByOffensive(true).iterator();
       	this.nonoffensiveTweets = tweetRepository.findAllByOffensive(false).iterator();
        this.totalExamples = (int) tweetRepository.count();
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
        return offensiveTweets.hasNext() || nonoffensiveTweets.hasNext();
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

}
