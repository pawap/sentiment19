package sentiments.ml.classifier;

import sentiments.domain.model.tweet.Tweet;
import sentiments.domain.repository.tweet.TrainingTweetRepository;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author 6runge
 * 
 * Dummy Classifier for determining the initial project architecture
 *
 */
public class TweetClassifier implements Classifier{
	HashMap<String, Integer> sentiWords;

	public TweetClassifier() {
		sentiWords = new HashMap<String, Integer>();
		readWordList();
	}
	
	public Classification classifyTweet(String tweet) {
		double classifiedWords = 0;
		double	sentiment = 0;
		String classification = "The Tweet is ";
		String[] tweetWords = tweet.split("\\s+"); 
		
		for (String word : tweetWords) {
			if (sentiWords.containsKey(word.toUpperCase())) {					
					classifiedWords++;
					sentiment += sentiWords.get(word.toUpperCase());
//					System.out.println(word + "is a word I know.");
//					System.out.println("Sentiment at " + sentiment);
			}
		}
		Classification cl = new Classification();
		if (sentiment == 0) {
			cl.setOffensive(false);
			cl.setProbability(sentiment/classifiedWords);
			classification += "neutral.";
		}
		else if (sentiment < 0) {
			cl.setOffensive(true);
			cl.setProbability(-sentiment/classifiedWords);
		    classification += sentiment/classifiedWords*-100 + "% negative.";
		}
		else {
			cl.setOffensive(false);
			cl.setProbability(sentiment/classifiedWords);
			classification += + sentiment/classifiedWords*100 + "% positive.";
		}

		return cl;
	}

	@Override
	public void train(TrainingTweetRepository tweetRepository) {

	}

	@Override
	public boolean isTrained() {
		return true;
	}

	@Override
	public void classifyTweets(List<Tweet> tweetList, Date runDate) {

	}

	private void readWordList() {
        String wordListFile = "resources/LoughranMcDonald_SentimentWordLists_2018.csv";
        BufferedReader br = null;
        String line = "";
        String separator = ",";

        try {

            br = new BufferedReader(new FileReader(wordListFile));
            while ((line = br.readLine()) != null) {
            	//System.out.println(line);
            	String[] splitLine = line.split(separator);
                sentiWords.put(splitLine[0], Integer.valueOf(splitLine[1]));

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}
}
