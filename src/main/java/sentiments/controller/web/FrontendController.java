package sentiments.controller.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import sentiments.domain.model.Language;
import sentiments.domain.model.query.HashtagCount;
import sentiments.domain.model.query.Timeline;
import sentiments.domain.model.query.TweetFilter;
import sentiments.domain.repository.DayStatsRepository;
import sentiments.domain.repository.tweet.TweetRepository;
import sentiments.domain.service.LanguageService;
import sentiments.ml.classifier.Classification;
import sentiments.ml.classifier.Classifier;
import sentiments.ml.service.ClassifierService;
import sentiments.service.ExceptionService;
import sentiments.service.TimelineService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
/**
 * The controller for the frontend.
 * @author 6runge, Paw
 */
@RestController
public class FrontendController extends BasicWebController {

    private static final Logger log = LoggerFactory.getLogger(FrontendController.class);

    @Autowired
    Environment env;

    @Autowired
    ClassifierService classifierService;

    @Autowired
    TweetRepository tweetRepository;

    @Autowired
    LanguageService languageService;

    @Autowired
    ExceptionService exceptionService;

    @Autowired
    DayStatsRepository dayStatsRepository;

    @Autowired
    private TimelineService timelineService;

    @RequestMapping("/")
    public ResponseEntity<String> html() {
        String response = "";
        try {
            File file = ResourceUtils.getFile(
                    "classpath:frontend/sentiment-frontend.html");
            response = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            String eString = exceptionService.exceptionToString(e);
            log.warn(eString);
            e.printStackTrace();
        } catch (IOException e) {
            String eString = exceptionService.exceptionToString(e);
            log.warn(eString);
            e.printStackTrace();
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");

        return new ResponseEntity<String>(response, responseHeaders,HttpStatus.CREATED);
    }

    @RequestMapping(value = "/tweet",  method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<String> tweet(@RequestBody TweetFilter tf) {
        String base_url = "https://publish.twitter.com/oembed?url=https://twitter.com/user/status/";
        String twitterId;
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        JsonObject out = new JsonObject();
        JsonObject obj = null;
        int responseCode = 0;
        int i = 0;
        while (responseCode != 200 && i < 100) {
            i++;
            try {
                twitterId = tweetRepository.getRandomTwitterId(tf);
                if (twitterId == null) {
                    break;
                }
                String url = base_url + twitterId + "&align=center";
                URL urlObj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

                // optional default is GET
                con.setRequestMethod("GET");

                //add request header
                responseCode = con.getResponseCode();

                JsonReader reader = new JsonReader(new InputStreamReader(con.getInputStream()));
                obj = new JsonParser().parse(reader).getAsJsonObject();
                reader.close();

            } catch (IOException e) {

            }
        }
        String str = null;
        if (obj != null && obj.get("html") != null) {
            str = obj.get("html").getAsString();

        } else {
            str = "<h3>Couldn't fetch tweet</h3>";
        }
        log.debug("#calls: " + i + "; Success:" + ((obj != null)? "true" : "false") + ";");
        
        out.addProperty("html", str);
        return new ResponseEntity<>(out.toString(), responseHeaders, HttpStatus.CREATED);
    }

    @RequestMapping("/classify")
    public ResponseEntity<String> classify(@RequestParam(value = "tweet", defaultValue = "") String tweet) {
        String cleanTweet = tweet.replace("\r", " ").replace("\n", " ").trim();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        Classification classification;
        try {
            Classifier tweetClassifier = classifierService.getClassifier(languageService.getLanguage("en"));
            classification = tweetClassifier.classifyTweet(cleanTweet);
        } catch (Exception e) {
            String eString = exceptionService.exceptionToString(e);
            log.warn("Exception during classification: " + eString);
            return new ResponseEntity<>("Internal Error." + eString, responseHeaders,HttpStatus.INTERNAL_SERVER_ERROR);
        }

        JSONObject out = new JSONObject();
        out.put("offensive", classification.isOffensive());
        out.put("probability", classification.getProbability());
        return new ResponseEntity<>(out.toString(), responseHeaders,HttpStatus.CREATED);
    }

    @RequestMapping(value = "/stats",  method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<String> stats(@RequestBody TweetFilter tf) {

        int count = tweetRepository.countByOffensiveAndDate(tf);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        JSONObject out = new JSONObject();
        out.put("count", count);
        return new ResponseEntity<>(out.toString(), responseHeaders,HttpStatus.CREATED);
    }

    @RequestMapping(value = "/popularhashtags",  method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<String> popularhashtags(@RequestBody TweetFilter tf, @RequestParam( value = "limit", defaultValue = "5") int limit ) {

        List<HashtagCount> tags = tweetRepository.getMostPopularHashtags(tf, limit);
        int total = tweetRepository.countByOffensiveAndDate(tf);
        tf.setOffensive(true);
        total += tweetRepository.countByOffensiveAndDate(tf);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        JSONArray hashtags = new JSONArray();
        hashtags.addAll(tags.stream().map(HashtagCount::toJSONObject).collect(Collectors.toList()));
        JSONObject out = new JSONObject();
        out.put("hashtags", hashtags );
        out.put("total", total );
        return new ResponseEntity<>(out.toString(), responseHeaders,HttpStatus.OK);
    }

    @RequestMapping(value="/timeline", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<String> timeline(@RequestBody TweetFilter tf) {
        Timeline timeline = timelineService.getTimeline(tf);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        JSONObject out = new JSONObject();
        JSONArray arr = new JSONArray();
        arr.addAll(timeline.timeline);
        out.put("timeline", arr);
        out.put("start", timeline.start.toString());
        out.put("end", timeline.end.toString());
        return new ResponseEntity<>(out.toString(), responseHeaders,HttpStatus.OK);
    }

    @RequestMapping(value="/availablelanguages")
    public ResponseEntity<String> availablelanguages() {
        HttpHeaders responseHeaders = new HttpHeaders();

        Iterable<Language> langs = languageService.getAvailableLanguages();
        JSONObject out = new JSONObject();
        JSONArray arr = new JSONArray();
        for (Language lang: langs) {
            arr.add(lang.toJSONObject());
        }
        out.put("availableLanguages", arr);
        return new ResponseEntity<>(out.toString(), responseHeaders,HttpStatus.OK);
    }

}
