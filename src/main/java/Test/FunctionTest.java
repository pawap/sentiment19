package Test;

import org.junit.Test;
import sentiments.domain.model.Tweet;
import sentiments.domain.preprocessor.BasicTweetPreProcessor;
import sentiments.domain.preprocessor.ImportTweetPreProcessor;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FunctionTest {

    private String englishTweet = "This is an sample english tweet!";
    private String germanTweet = "Dies is ein Beispiel Tweet!";


    private String regexString = "RT @example2 https://example3";


    @Test
    public void tweetTest(){
        ArrayList<String> hashtag = new ArrayList<>();
        hashtag.add("Test");

        Tweet tweet = new Tweet();
        tweet.setText("TestText");
        tweet.setTwitterId("0L");
        tweet.setOffensive(true);
        tweet.setLanguage("TestLanguage");
        tweet.setCrdate(new Date(1));
        tweet.setTmstamp(new Date(1));
        tweet.setUid(0);

        assertEquals("Tweet not matching",
                0+" "+"0L"+" "+"TestText"+" "+ "TestLanguage"+" "+new Date(1)+" "+new Date(1)+" "+true ,
                tweet.getUid() +" "+ tweet.getTwitterId()+" "+tweet.getText()+" "+ tweet.getLanguage()+" "+tweet.getCrdate()+" "+tweet.getTmstamp()+" "+tweet.isOffensive());

    }

    @Test
    public void regexTest(){

        BasicTweetPreProcessor basicTweetPreProcessor = new BasicTweetPreProcessor();

        Tweet tweet = new Tweet();
        tweet.setText(regexString);
        basicTweetPreProcessor.preProcess(tweet);
        assertEquals(" ",tweet.getText());

    }

    @Test
    public void LanguageTest(){
        ImportTweetPreProcessor importTweetPreProcessor = new ImportTweetPreProcessor();

        Tweet t1= new Tweet();
        t1.setText(englishTweet);

        Tweet t2 = new Tweet();
        t2.setText(germanTweet);

        importTweetPreProcessor.preProcess(t1);
        importTweetPreProcessor.preProcess(t2);

        assertEquals("en", t1.getLanguage());
        assertEquals("de", t2.getLanguage());
    }

}
