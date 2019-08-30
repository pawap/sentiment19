package sentiments;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sentiments.data.BasicDataImporter;
import sentiments.domain.model.HashtagCount;
import sentiments.domain.model.TweetFilter;
import sentiments.domain.repository.TweetRepository;
import sentiments.domain.service.TweetFilterBuilder;
import sentiments.ml.W2VTweetClassifier;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Paw, 6runge
 * 
 * Dummy App-Controller for determining the initial project architecture
 *
 */
//@Configuration
@RestController
@EnableAutoConfiguration
@EnableScheduling
@ComponentScan
public class ApplicationController implements SentimentAnalysisWebInterface{

	@Autowired
	Environment env;
	
	@Autowired
    BasicDataImporter basicDataImporter;
	
	@Autowired
    W2VTweetClassifier tweetClassifier;
	
	@Autowired
    TweetRepository tweetRepository;


	@RequestMapping("/tweet")
    public ResponseEntity<String> tweet(@RequestParam(value = "offensive", defaultValue = "1") boolean offensive) {
        String base_url = "https://publish.twitter.com/oembed?url=https://twitter.com/user/status/";
        String twitterId;
        StringBuffer response = new StringBuffer();
        JsonObject obj = null;

        int responseCode = 0;
        int i = 0;
        while (responseCode != 200 && i < 100) {
            i++;
            try {
                TweetFilterBuilder tfb = new TweetFilterBuilder();
                twitterId = tweetRepository.getRandomTwitterId(tfb.setOffensive(offensive).build());
                if (twitterId == null) break;
                String url = base_url + twitterId + "&align=center";
                URL urlObj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

                // optional default is GET
                con.setRequestMethod("GET");

                //add request header
                responseCode = con.getResponseCode();
                System.out.println("\nSending 'GET' request to URL : " + url);
                System.out.println("Response Code : " + responseCode);

                JsonReader reader = new JsonReader(new InputStreamReader(con.getInputStream()));
                obj = new JsonParser().parse(reader).getAsJsonObject();
                reader.close();

            } catch (IOException e) {

            }
        }
        String str = null;
        if (obj != null) {
            str = obj.get("html").getAsString();
        } else {
            str = "<h3>Couldn't fetch tweet</h3>";
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        JSONObject out = new JSONObject();
        out.put("html", str);
        return new ResponseEntity<String>(out.toString(), responseHeaders, HttpStatus.CREATED);
    }

    @RequestMapping("/sentiments")
	public ResponseEntity<String> home(@RequestParam(value = "tweet", defaultValue = "") String tweet, @RequestParam(value = "format", defaultValue = "text") String format) {
        String cleanTweet = tweet.replace("\r", " ").replace("\n", " ").trim();
        System.out.println("tweet:" + cleanTweet);
        String cleanFormat = format.replace("\r", " ").replace("\n", " ").trim();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        String response;
        if (cleanFormat.compareTo("json") == 0) {
        	response = generateJSONResponse(cleanTweet);
        } else {
        	response = generateTextResponse(cleanTweet);
        }
       
        return new ResponseEntity<String>(response, responseHeaders,HttpStatus.CREATED);
    }
    
    @RequestMapping(value = "/stats",  method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity<String> stats(@RequestBody TweetFilter tq) {

    	int count = tweetRepository.countByOffensiveAndDate(tq);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        JSONObject out = new JSONObject();
        out.put("count", count);
        return new ResponseEntity<String>(out.toString(), responseHeaders,HttpStatus.CREATED);
    }

    @RequestMapping(value = "/popularhashtags",  method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<String> popularhashtags(@RequestBody TweetFilter tq, @RequestParam( value = "limit", defaultValue = "5") int limit ) {

        List<HashtagCount> tags = tweetRepository.getMostPopularHashtags(tq, limit);
        int total = tweetRepository.countByOffensiveAndDate(tq);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        JSONArray hashtags = new JSONArray();
        hashtags.addAll(tags.stream().map(HashtagCount::toJSONObject).collect(Collectors.toList()));
        JSONObject out = new JSONObject();
        out.put("hashtags", hashtags );
        out.put("total", total );
        return new ResponseEntity<String>(out.toString(), responseHeaders,HttpStatus.CREATED);
    }

    @RequestMapping("/count")
    public ResponseEntity<String> count() {

        int count = tweetRepository.countfindAllTweets();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        JSONObject out = new JSONObject();
        out.put("count", count);
        return new ResponseEntity<String>(out.toString(), responseHeaders, HttpStatus.CREATED);
    }

    @RequestMapping("/byDateBetween")
    public ResponseEntity<String> byDateBetween(@RequestParam(value = "startdate", defaultValue = "1990-01-01") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startdate,
                                                @RequestParam(value = "enddate", defaultValue = "today") @DateTimeFormat(pattern = "yyyy-MM-dd") Date enddate) {

        int count = tweetRepository.countfindAllByDateBetween(new Timestamp(startdate.getTime()), new Timestamp(enddate.getTime()));
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        JSONObject out = new JSONObject();
        out.put("count", count);
        return new ResponseEntity<String>(out.toString(), responseHeaders,HttpStatus.CREATED);
    }

    @RequestMapping("/countOffensive")
    public ResponseEntity<String> cOffensive(@RequestParam(value = "offensive", defaultValue = "1") boolean offensive) {

        int count = tweetRepository.countByOffensive(offensive);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        JSONObject out = new JSONObject();
        out.put("count", count);
        return new ResponseEntity<String>(out.toString(), responseHeaders,HttpStatus.CREATED);
    }

    @RequestMapping("/timeline")
    public ResponseEntity<String> timeline(@RequestParam(value = "offensive", defaultValue = "1") boolean offensive,
                                        @RequestParam(value = "startdate", defaultValue = "1990-01-01") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startdate,
                                        @RequestParam(value = "enddate", defaultValue = "today") @DateTimeFormat(pattern = "yyyy-MM-dd") Date enddate) {

	    TweetFilterBuilder filterBuilder = new TweetFilterBuilder();
	    filterBuilder.setOffensive(offensive)
                .setStart(new Timestamp(startdate.getTime()))
                .setEnd(new Timestamp(enddate.getTime()));

	    List<Integer> count = tweetRepository.countByOffensiveAndDayInInterval(filterBuilder.build());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        JSONObject out = new JSONObject();
        JSONArray timeline = new JSONArray();
        timeline.addAll(count);
        out.put("timeline", timeline);
        return new ResponseEntity<String>(out.toString(), responseHeaders,HttpStatus.CREATED);
    }

    @RequestMapping("/")
	public ResponseEntity<String> html() {
        String response = "";
        try {
            File file = ResourceUtils.getFile(
                    "classpath:frontend/sentiment-frontend.html");
            response = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
       
        return new ResponseEntity<String>(response, responseHeaders,HttpStatus.CREATED);
    }

    @RequestMapping("/backend")
    public ResponseEntity<String> backend() {
        String response = "";
        try {
            File file = ResourceUtils.getFile(
                    "classpath:frontend/sentiment-backend.html");
            response = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");

        return new ResponseEntity<String>(response, responseHeaders,HttpStatus.CREATED);
    }
    
    @RequestMapping("/backend/import")
	public ResponseEntity<String> tweetimport() {
    	
    	this.basicDataImporter.importExampleJson();
    	
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
       
        return new ResponseEntity<String>("finished", responseHeaders,HttpStatus.CREATED);
    }
    
    @RequestMapping("/backend/import/testandtrain")
	public ResponseEntity<String> testAndTrainimport() {
    	
    	this.basicDataImporter.importTsvTestAndTrain();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
       
        return new ResponseEntity<String>("finished", responseHeaders,HttpStatus.CREATED);
    }    
    
    private String generateJSONResponse(String input) {
        JSONObject out = new JSONObject();
        out.put("input", input);

        JSONArray sentiments = new JSONArray();

        sentiments.add(tweetClassifier.classifyTweet(input));
        
        out.put("sentiments", sentiments);

        return out.toString();
    }

    private String generateTextResponse(String input) {
        StringBuilder output = new StringBuilder();

        output.append("input: " + input);
        output.append("\nsentiments:");
        output.append(tweetClassifier.classifyTweet(input));
 
        return output.toString();
    }

    /**
     * Runs the RESTful server.
     *
     * @param args execution arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ApplicationController.class, args);
    }

	@Override
	public ResponseEntity<String> offensivityStatistics() {
        JSONObject response = new JSONObject();
        response.put("offensive", Math.random() * 100);
		
        return new ResponseEntity<String>(response.toString(), HttpStatus.CREATED);
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) throws Exception {
	    final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	    final CustomDateEditor dateEditor = new CustomDateEditor(df, true) {
	        @Override
	        public void setAsText(String text) throws IllegalArgumentException {
	            if ("today".equals(text)) {
	                setValue(new Date(System.currentTimeMillis()));
	            } else {
	                super.setAsText(text);
	            }
	        }
	    };
	    binder.registerCustomEditor(Date.class, dateEditor);
	}

    @PostMapping("backend/upload")
    public ResponseEntity<String> singleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        HttpHeaders responseHeaders = new HttpHeaders();
        System.out.println("upload");
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            System.out.println("empty");
            return new ResponseEntity<String>("uploadStatus: empty", responseHeaders,HttpStatus.CREATED);
        }

        try {
            InputStream stream = file.getInputStream();
            basicDataImporter.importFromStream(stream);

            redirectAttributes.addFlashAttribute("message",
                    "You successfully uploaded '" + file.getOriginalFilename() + "'");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<String>("uploadStatus: postitve", responseHeaders,HttpStatus.CREATED);
    }


}
