package test;

import org.junit.Test;
import sentiments.domain.model.Tweet;
import sentiments.domain.preprocessor.StanfordLemmatizer;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FunctionTest {

    StanfordLemmatizer sfl = new StanfordLemmatizer();

    @Test
    public void lemmaTest(){

        String text = "This is a sentence for test purposes.";

        Tweet tweet = new Tweet();
        tweet.setText(text);
        tweet.setLemma(sfl.lemmatize(tweet.getText()));

        String joined = String.join(" ", tweet.getLemma());

        assertEquals("","this be a sentence for test purpose .",joined);
    }
}
