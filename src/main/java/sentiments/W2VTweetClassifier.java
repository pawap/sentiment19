package sentiments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Paw
 *
 */
@Service
public class W2VTweetClassifier {
	
	@Autowired
	TrainingTweetRepository tweetRepository;
	
	private MultiLayerNetwork net;
	
	public W2VTweetClassifier() {
		File netFile;
		this.net = null;
		if ((netFile = new File("resources/nets/rnnw2v.nn")).exists()) {
			try {
				net = ModelSerializer.restoreMultiLayerNetwork(netFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void train() {
		int batchSize = 64;     //Number of examples in each minibatch
	    int vectorSize = 300;   //Size of the word vectors. 300 in the Google News model
	    int nEpochs = 2;        //Number of epochs (full passes of training data) to train on
	    int truncateReviewsToLength = 256;  //Truncate reviews with length (# words) greater than this
	    final int seed = 0;     //Seed for reproducibility

	    Nd4j.getMemoryManager().setAutoGcWindow(10000);  //https://deeplearning4j.org/workspaces

	    //Set up network configuration
	    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
	        .seed(seed)
	        .updater(new Adam(5e-3))
	        .l2(1e-5)
	        .weightInit(WeightInit.XAVIER)
	        .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue).gradientNormalizationThreshold(1.0)
	        .list()
	        .layer(0, new LSTM.Builder().nIn(vectorSize).nOut(256)
	            .activation(Activation.TANH).build())
	        .layer(1, new RnnOutputLayer.Builder().activation(Activation.SOFTMAX)
	            .lossFunction(LossFunctions.LossFunction.MCXENT).nIn(256).nOut(2).build())
	        .build();

	    net = new MultiLayerNetwork(conf);
	    net.init();
	    
	    //Initialize the user interface backend
	    UIServer uiServer = UIServer.getInstance();
	    //Configure where the network information (gradients, score vs. time etc) is to be stored. Here: store in memory.
	    StatsStorage statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later
	    //Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
	    uiServer.attach(statsStorage);
	    //Then add the StatsListener to collect this information from the network, as it trains
	    net.setListeners(new StatsListener(statsStorage));
	    
	    //DataSetIterators for training and testing respectively
	    WordVectors wordVectors = WordVectorsService.getWordVectors();
	    TweetIterator train = new TweetIterator(tweetRepository, wordVectors, batchSize, truncateReviewsToLength, true);
	    TweetIterator test = new TweetIterator(tweetRepository, wordVectors, batchSize, truncateReviewsToLength, false);
	    System.out.println("Starting training");
	    for (int i = 0; i < nEpochs; i++) {
	        net.fit(train);
	        train.reset();
	        System.out.println("Epoch " + i + " complete. Starting evaluation:");

	        Evaluation evaluation = net.evaluate(test);
	        System.out.println(evaluation.stats());
	    }
	    
	    try {
			ModelSerializer.writeModel(net, new File("resources/nets/rnnw2v.nn"), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    //After training: load a single example and generate predictions
	    String shortOffensiveTweet = "You are all bloody suckers. I hate filthy assholes like u";

	    INDArray features = loadFeaturesFromString(shortOffensiveTweet, truncateReviewsToLength);
	    INDArray networkOutput = net.output(features);
	    long timeSeriesLength = networkOutput.size(2);
	    INDArray probabilitiesAtLastWord = networkOutput.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(timeSeriesLength - 1));

	    System.out.println("\n\n-------------------------------");
	    System.out.println("Short negative tweet: \n" + shortOffensiveTweet);
	    System.out.println("\n\nProbabilities at last time step:");
	    System.out.println("p(offensive): " + probabilitiesAtLastWord.getDouble(0));
	    System.out.println("p(nonoffensive): " + probabilitiesAtLastWord.getDouble(1));
	    System.out.println("----- Example complete -----");
	    
	    //After training: load a single example and generate predictions
	    String shortNonOffensiveTweet = "I love you all. Happiness is evreywhere.";

	    features = loadFeaturesFromString(shortNonOffensiveTweet, truncateReviewsToLength);
	    networkOutput = net.output(features);
	    timeSeriesLength = networkOutput.size(2);
	    probabilitiesAtLastWord = networkOutput.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(timeSeriesLength - 1));

	    System.out.println("\n\n-------------------------------");
	    System.out.println("Short positive tweet: \n" + shortNonOffensiveTweet);
	    System.out.println("\n\nProbabilities at last time step:");
	    System.out.println("p(offensive): " + probabilitiesAtLastWord.getDouble(0));
	    System.out.println("p(nonoffensive): " + probabilitiesAtLastWord.getDouble(1));
	    System.out.println("----- Example complete -----");
		
	}
	
	public String classifyTweet(String tweet) {
		if (this.net == null) {
			System.out.println("No model. Training new one.");
			train();
		}
		INDArray features = loadFeaturesFromString(tweet, 300);
	    INDArray networkOutput = net.output(features);
	    long timeSeriesLength = networkOutput.size(2);
	    INDArray probabilitiesAtLastWord = networkOutput.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(timeSeriesLength - 1));
	    double offProb = probabilitiesAtLastWord.getDouble(0);
	    return ((offProb >= 0.5)? "offensive" : "nonoffensive") + "(" + ((offProb >= 0.5)? offProb : 1 - offProb) + ")";	
	}
    /**
     * Used post training to convert a String to a features INDArray that can be passed to the network output method
     *
     * @param reviewContents Contents of the review to vectorize
     * @param maxLength Maximum length (if review is longer than this: truncate to maxLength). Use Integer.MAX_VALUE to not nruncate
     * @return Features array for the given input String
     */
    private INDArray loadFeaturesFromString(String tweetContents, int maxLength){
        DefaultTokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
	    WordVectors wordVectors = WordVectorsService.getWordVectors();
	    int vectorSize = wordVectors.getWordVector(wordVectors.vocab().wordAtIndex(0)).length;
	    List<String> tokens = tokenizerFactory.create(tweetContents).getTokens();
        List<String> tokensFiltered = new ArrayList<>();
        for(String t : tokens ){
            if(wordVectors.hasWord(t)) tokensFiltered.add(t);
        }
        int outputLength = Math.min(maxLength,tokensFiltered.size());

        INDArray features = Nd4j.create(1, vectorSize, outputLength);

        int count = 0;
        for (int j = 0; j < tokensFiltered.size() && count < maxLength; j++ ){
            String token = tokensFiltered.get(j);
            INDArray vector = wordVectors.getWordVectorMatrix(token);
            if(vector == null){
                continue;   //Word not in word vectors
            }
            features.put(new INDArrayIndex[]{NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(j)}, vector);
            count++;
        }

        return features;
    }

}
