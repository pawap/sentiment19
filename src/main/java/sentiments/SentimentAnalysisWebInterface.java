package sentiments;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface SentimentAnalysisWebInterface {

	/**
	 * 
	 * Get Basic Tweet analysis.  
	 * 
	 * @param tweet String Tweet text
	 * @param format String Response format ("json" or "text"
	 * @return the Analysis in the given format. Structure: {input: tweet, sentiments: "This Tweet is neutral/ x% negative /x% positive"}
	 */
    @RequestMapping("/sentiments") 
    ResponseEntity<String> home(@RequestParam(value = "tweet", defaultValue = "") String tweet, @RequestParam(value = "format", defaultValue = "text") String format);


    /**
     * 
     * Get the js/html of the FrontendApp
     * 
     * @return html with vue.js App
     */
    @RequestMapping("/html")
    ResponseEntity<String> html();


    /**
     * 
     * 
     * @return the percentage of offensive Tweets. Format: {offensive: x}
     */
    @RequestMapping("/sentiment/offensivityStatistics")
    ResponseEntity<String> offensivityStatistics();
    
}
