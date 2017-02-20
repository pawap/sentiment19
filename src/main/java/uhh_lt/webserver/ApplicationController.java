package uhh_lt.webserver;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jobimtext.api.struct.Order2;
import org.jobimtext.api.struct.WebThesaurusDatastructure;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

@RestController
@EnableAutoConfiguration
public class ApplicationController {


    private static WebThesaurusDatastructure dt;


    @RequestMapping("/expansions")
    String home(@RequestParam(value = "word", defaultValue = "") String word, @RequestParam(value = "format", defaultValue = "text") String format) {

        word = word.replace("\r", " ").replace("\n", " ").trim();
        format = format.replace("\r", " ").replace("\n", " ").trim();

        if (format.compareTo("json") == 0) {
            return generateJSONResponse(word);
        } else {
            return generateTextResponse(word);
        }
    }

    private String generateJSONResponse(String input) {
        JSONObject out = new JSONObject();
        out.put("input", input);

        JSONArray expansions = new JSONArray();
        for (Order2 exp : dt.getSimilarTerms(input)) {
            expansions.add(exp.key);
        }
        out.put("expansions", expansions);

        return out.toString();
    }

    private String generateTextResponse(String input) {
        StringBuilder output = new StringBuilder();

        output.append("input: " + input);
        output.append("\nexpansions:");
        for (Order2 exp : dt.getSimilarTerms(input)) {
            output.append("\n  - " + exp.key);
        }
        return output.toString();
    }

    /**
     * Runs the RESTful server.
     *
     * @param args execution arguments
     */
    public static void main(String[] args) {
        dt = new WebThesaurusDatastructure("resources/conf_web_deNews_trigram.xml");
        dt.connect();
        SpringApplication.run(ApplicationController.class, args);
    }

}