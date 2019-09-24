package sentiments.controller.web;

import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sentiments.domain.repository.TweetRepository;

import java.sql.Timestamp;
import java.util.Date;

@RestController
public class TestController extends BasicWebController{

    @Autowired
    TweetRepository tweetRepository;

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
}
