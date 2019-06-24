package sentiments.domain.preprocessor;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.util.Span;
import sentiments.domain.model.AbstractTweet;
import sentiments.domain.model.Tweet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.Language;

import opennlp.tools.namefind.TokenNameFinderModel;

public class ImportTweetPreProcessor implements TweetPreProcessor {

    private LanguageDetector detector = LanguageDetectorBuilder.fromAllBuiltInSpokenLanguages().build();
    final private Pattern pattern = Pattern.compile("#[\\w_-]+[:]?");

    @Override
    public void preProcess(AbstractTweet tweet) {
        String text = tweet.getText();
        Set<String> hashtags = pattern
                .matcher(text)
                .results()
                .map(MatchResult::group)
                .collect(Collectors.toSet());
        ((Tweet) tweet).setHashtags(hashtags);
        // find person name
        try {
            System.out.println("-------Finding entities belonging to category : person name------");
            findName();
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Language detectedLanguage = detector.detectLanguageOf(text.replaceAll("(((RT )?@[\\w_-]+[:]?)|((https?:\\/\\/)[\\w\\d.-\\/]*))",""));
        tweet.setLanguage(detectedLanguage.getIsoCode());
    }

    @Override
    public void destroy() {
        detector = null;
    }

    /**
     * method to find locations in the sentence
     * @throws IOException
     */
    public void findName() throws IOException {
        InputStream is = new FileInputStream("C:\\Users\\Erkan\\IdeaProjects\\sentiment19\\src\\main\\resources\\en-ner-person.bin");

        // load the model from file
        TokenNameFinderModel model = new TokenNameFinderModel(is);
        is.close();

        // feed the model to name finder class
        NameFinderME nameFinder = new NameFinderME(model);

        // input string array
        String[] sentence = new String[]{
                "John",
                "Smith",
                "is",
                "standing",
                "next",
                "to",
                "bus",
                "stop",
                "and",
                "waiting",
                "for",
                "Mike",
                "."
        };

        Span nameSpans[] = nameFinder.find(sentence);

        // nameSpans contain all the possible entities detected
        for(Span s: nameSpans){
            System.out.print(s.toString());
            System.out.print("  :  ");
            // s.getStart() : contains the start index of possible name in the input string array
            // s.getEnd() : contains the end index of the possible name in the input string array
            for(int index=s.getStart();index<s.getEnd();index++){
                System.out.print(sentence[index]+" ");
            }
            System.out.println();
        }
    }
}
