package sentiments.domain.preprocessor;

import sentiments.domain.model.AbstractTweet;
import sentiments.domain.model.Tweet;

import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.Language;


public class ImportTweetPreProcessor implements TweetPreProcessor {

    private LanguageDetector detector = LanguageDetectorBuilder.fromAllBuiltInSpokenLanguages().build();
    final private Pattern pattern = Pattern.compile("#[\\w_-]+[:]?");

    StanfordLemmatizer sfl = new StanfordLemmatizer();

    @Override
    public void preProcess(AbstractTweet tweet) {
        String text = tweet.getText();
        Set<String> hashtags = pattern
                .matcher(text)
                .results()
                .map(MatchResult::group)
                .collect(Collectors.toSet());
        ((Tweet) tweet).setHashtags(hashtags);
        Language detectedLanguage = detector.detectLanguageOf(text.replaceAll("(((RT )?@[\\w_-]+[:]?)|((https?:\\/\\/)[\\w\\d.-\\/]*))",""));
        tweet.setLanguage(detectedLanguage.getIsoCode());

        //test of the functionality
        if(tweet.getLanguage()== "en" || tweet.getLanguage()== "de") {
//            System.out.println(text);
//            System.out.println(sfl.lemmatize(text));

            //persist lemmtized tokens (german, english) into MongoDB
            tweet.setLemma(sfl.lemmatize(text));
        }
    }

    @Override
    public void destroy() {
        detector = null;
    }
}
