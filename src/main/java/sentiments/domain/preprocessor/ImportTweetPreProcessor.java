package sentiments.domain.preprocessor;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import sentiments.domain.model.tweet.AbstractTweet;
import sentiments.domain.model.tweet.Tweet;

import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Paw
 */
public class ImportTweetPreProcessor implements TweetPreProcessor {

    private static LanguageDetector detector = LanguageDetectorBuilder.fromAllBuiltInSpokenLanguages().build();
    final private Pattern pattern = Pattern.compile("#[\\w_-]+[:]?");

    @Override
    public void preProcess(AbstractTweet tweet) {
        String text = tweet.getText();
        if (tweet instanceof Tweet) {
            Set<String> hashtags = pattern
                    .matcher(text)
                    .results()
                    .map(MatchResult::group)
                    .collect(Collectors.toSet());
            ((Tweet) tweet).setHashtags(hashtags);
        }
        Language detectedLanguage = detector.detectLanguageOf(text.replaceAll("(((RT )?@[\\w_-]+[:]?)|((https?:\\/\\/)[\\w\\d.-\\/]*))", ""));
        if (detectedLanguage != null) {
            tweet.setLanguage(detectedLanguage.getIsoCode());
        } else {
            tweet.setLanguage(Language.UNKNOWN.getIsoCode());
        }
    }

    @Override
    public void destroy() {
        detector = null;
    }
}
