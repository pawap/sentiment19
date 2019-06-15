package sentiments;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author 6runge
 * 
 * Dummy Classifier for determining the initial project architecture
 *
 */
public class TweetClassifier {
	HashMap<String, Integer> sentiWords;

	public TweetClassifier() {
		sentiWords = new HashMap<String, Integer>();
		readWordList();
	}
	
	public String classifyTweet(String tweet) {
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
		
		if (sentiment == 0) {
			classification += "neutral.";
		}
		else if (sentiment < 0) {
		    classification += sentiment/classifiedWords*-100 + "% negative.";
		}
		else {
			classification += + sentiment/classifiedWords*100 + "% positive.";
		}
		
		return classification;
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
