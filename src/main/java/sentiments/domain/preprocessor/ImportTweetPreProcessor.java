package sentiments.domain.preprocessor;

import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.RuleMatch;
import sentiments.domain.model.AbstractTweet;
import sentiments.domain.model.Tweet;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.Language;


public class ImportTweetPreProcessor implements TweetPreProcessor {

    private LanguageDetector detector = LanguageDetectorBuilder.fromAllBuiltInSpokenLanguages().build();
    final private Pattern pattern = Pattern.compile("#[\\w_-]+[:]?");

    JLanguageTool langToolEng = new JLanguageTool(new BritishEnglish());
    List<RuleMatch> matchesEng;
    JLanguageTool langToolGer = new JLanguageTool(new GermanyGerman());
    List<RuleMatch> matchesGer;

    @Override
    public void preProcess(AbstractTweet tweet) throws IOException {
        String text = tweet.getText();
        Set<String> hashtags = pattern
                .matcher(text)
                .results()
                .map(MatchResult::group)
                .collect(Collectors.toSet());
        ((Tweet) tweet).setHashtags(hashtags);

        Language detectedLanguage = detector.detectLanguageOf(text.replaceAll("(((RT )?@[\\w_-]+[:]?)|((https?:\\/\\/)[\\w\\d.-\\/]*))",""));
        tweet.setLanguage(detectedLanguage.getIsoCode());


        //gathering german and english tweets and their suggested corrections
        if(tweet.getLanguage() == "en" & (text.length() >= 70)) {
            matchesEng = langToolEng.check(text);
            for (RuleMatch match : matchesEng) {
                System.out.println("------" + text);
                System.out.println("Potential error at characters " +
                        match.getFromPos() + "-" + match.getToPos() + ": " +
                        match.getMessage());
                System.out.println("Suggested correction(s): " +
                        match.getSuggestedReplacements());
            }
        }else if (tweet.getLanguage() == "de" & (text.length() >= 70)){
            matchesGer = langToolGer.check(text);
            for (RuleMatch match : matchesGer) {
                System.out.println("------" + text);
                System.out.println("Potential error at characters " +
                        match.getFromPos() + "-" + match.getToPos() + ": " +
                        match.getMessage());
                System.out.println("Suggested correction(s): " +
                        match.getSuggestedReplacements());
            }
        }


        System.out.println(getMentionedUsers(tweet));

        //toLowerCase
        if(tweet.getLanguage()== ("en" ) || tweet.getLanguage()==("de")) {
            tweet.setText(text.toLowerCase());
            System.out.println(tweet.getText());
       }
    }

    /**
     *
     * @param tweet
     * @return tweet with updated text, replacing @... consistently with @Username
     */
    public String getMentionedUsers(AbstractTweet tweet) {
       String text = tweet.getText();
        String newText;
        if (text.contains("@")) {
            newText = text.replaceAll("@[\\w_-]+[:]?", "@Username");
        }else{
            newText = text;
        }

        return newText;
    }

    @Override
    public void destroy() {
        detector = null;
    }
}
