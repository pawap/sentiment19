package sentiments;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

/**
 * @author Paw, 6runge
 * 
 * Dummy App-Controller for determining the initial project architecture
 *
 */
//@Configuration
@RestController
@EnableAutoConfiguration
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

        String url = base_url + "911789314169823232" + "&align=center";
        StringBuffer response = new StringBuffer();
        JsonObject obj = null;

        int responseCode = 0;
        try {
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
            e.printStackTrace();
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
    
    @RequestMapping("/stats")
	public ResponseEntity<String> stats(@RequestParam(value = "offensive", defaultValue = "1") boolean offensive,
			@RequestParam(value = "startdate", defaultValue = "1990-01-01") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startdate,
			@RequestParam(value = "enddate", defaultValue = "today") @DateTimeFormat(pattern = "yyyy-MM-dd") Date enddate) {

    	int count = tweetRepository.countByOffensiveAndDate(offensive, new Timestamp(startdate.getTime()), new Timestamp(enddate.getTime()));
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        String response;
        JSONObject out = new JSONObject();
        out.put("count", count);
        return new ResponseEntity<String>(out.toString(), responseHeaders,HttpStatus.CREATED);
    }    

    @RequestMapping("/")
	public ResponseEntity<String> html() {
        String response = "";
        try {
            File file = ResourceUtils.getFile(
                    "classpath:html-tester/Sentiments-Frontend.html");
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
}
