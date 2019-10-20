package sentiments.ml.classifier;

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
import org.nd4j.linalg.api.iter.FirstAxisIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import sentiments.controller.web.BackendController;
import sentiments.domain.model.Language;
import sentiments.domain.model.tweet.Tweet;
import sentiments.domain.repository.tweet.TrainingTweetRepository;
import sentiments.service.ExceptionService;
import sentiments.ml.iterator.TweetIterator;
import sentiments.ml.iterator.TweetListIterator;
import sentiments.ml.service.WordVectorsService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The standard classifier
 * @author Paw
 */
public class W2VTweetClassifier implements Classifier {

	@Autowired
	ExceptionService exceptionService;

	private static final Logger log = LoggerFactory.getLogger(BackendController.class);

	private MultiLayerNetwork net;
	private Language language;
	private DefaultTokenizerFactory tokenizerFactory;

	public W2VTweetClassifier(Language language) {
		tokenizerFactory = new DefaultTokenizerFactory();
		tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
		File netFile;
		this.language = language;
		this.net = null;
		if ((netFile = new File(language.getClassifierFilename())).exists()) {
			try {
				net = ModelSerializer.restoreMultiLayerNetwork(netFile);
			} catch (IOException e) {
				String eString = exceptionService.exceptionToString(e);
				log.warn(eString);
				e.printStackTrace();
			}
		}
	}


	public void train(TrainingTweetRepository tweetRepository) {
		int batchSize = 64;     //Number of examples in each minibatch
	    int vectorSize = 300;   //Size of the word vectors. 300 in the Google News model
	    int nEpochs = 4;        //Number of epochs (full passes of training data) to train on
	    int truncateReviewsToLength = 256;  //Truncate reviews with length (# words) greater than this
	    final int seed = 0;     //Seed for reproducibility
		MultiLayerNetwork net;
	    Nd4j.getMemoryManager().setAutoGcWindow(10000);  //https://deeplearning4j.org/workspaces

	    //Set up network configuration
	    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
	        .seed(seed)
	        .updater(new Adam(5e-4))
	        .l2(1e-5)
	        .weightInit(WeightInit.XAVIER)
	        .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue).gradientNormalizationThreshold(1.0)
	        .list()
	        .layer(0, new LSTM.Builder().nIn(vectorSize).nOut(256)
	            .activation(Activation.RELU).build())
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
	    WordVectors wordVectors = WordVectorsService.getWordVectors(language);
	    TweetIterator train = new TweetIterator(tweetRepository, wordVectors, batchSize, truncateReviewsToLength, false, language);
	    TweetIterator test = new TweetIterator(tweetRepository, wordVectors, batchSize, truncateReviewsToLength, true, language);
	    System.out.println("Starting training");
	    for (int i = 0; i < nEpochs; i++) {
	        net.fit(train);
	        train.reset();
	        System.out.println("Epoch " + i + " complete. Starting evaluation:");

	        Evaluation evaluation = net.evaluate(test);
	        System.out.println(evaluation.stats());
	    }
	    this.net = net;
	    try {
			ModelSerializer.writeModel(net, ResourceUtils.getFile(language.getClassifierFilename()), true);
		} catch (IOException e) {
			String eString = exceptionService.exceptionToString(e);
			log.warn(eString);
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

	@Override
	public boolean isTrained() {
		return net != null;
	}

	public void classifyTweets(List<Tweet> tweets, Date date) {
		if (this.net == null) {
			System.out.println("No model.");
			return;
		}

		TweetListIterator tli = new TweetListIterator(tweets, language);
		if (tli.totalExamples() == 0) {
			return;
		}
		INDArray networkOutput = net.output(tli);

		FirstAxisIterator faxi = new FirstAxisIterator(networkOutput);
		for ( Tweet tweet: tli.getOutputTweets()) {
			if (!faxi.hasNext()) {
				System.out.println("NO nex faxi");
				break;
			}
			INDArray arr = (INDArray) faxi.next();
			long timeSeriesLength = arr.size(1);

			INDArray probabilitiesAtLastWord = arr.get(NDArrayIndex.point(0), NDArrayIndex.point(timeSeriesLength - 1));
			double offProb = probabilitiesAtLastWord.getDouble(0);

			tweet.setOffensive(offProb >= 0.5);
			tweet.setClassified(date);

		}
	}

	public Classification classifyTweet(String tweet) {
		if (this.net == null) {
			System.out.println("No model.");
			return null;
		}

		INDArray features = loadFeaturesFromString(tweet, 300);
		Classification classification = new Classification();
		if (features == null) {
			classification.setOffensive(false);
			classification.setProbability(0);
			return classification;
		}
		INDArray networkOutput = net.output(features);
	    long timeSeriesLength = networkOutput.size(2);
	    INDArray probabilitiesAtLastWord = networkOutput.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(timeSeriesLength - 1));
	    double offProb = probabilitiesAtLastWord.getDouble(0);

	    classification.setOffensive(offProb >= 0.5);
	    classification.setProbability((offProb >= 0.5)? offProb : 1 - offProb);

		return classification;
	}
    /*
     * Used post training to convert a String to a features INDArray that can be passed to the network output method
     *
     * @param maxLength Maximum length (if review is longer than this: truncate to maxLength). Use Integer.MAX_VALUE to not nruncate
     * @return Features array for the given input String
     */
    private INDArray loadFeaturesFromString(String tweetContents, int maxLength){
	    WordVectors wordVectors = WordVectorsService.getWordVectors(language);
	    int vectorSize = wordVectors.getWordVector(wordVectors.vocab().wordAtIndex(0)).length;
	    List<String> tokens = tokenizerFactory.create(tweetContents).getTokens();
        List<String> tokensFiltered = new ArrayList<>();
        for(String t : tokens ){
            if(wordVectors.hasWord(t)) tokensFiltered.add(t);
        }
        if (tokensFiltered.isEmpty()) {
        	return null;
		}

        int outputLength = Math.min(maxLength,tokensFiltered.size());

        INDArray features = Nd4j.create(1, vectorSize, Math.max(outputLength, 1));

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
