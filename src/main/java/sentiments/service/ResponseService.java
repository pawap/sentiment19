package sentiments.service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

/**
 * @author Paw, 6runge
 */
@Service
public class ResponseService {

    public String generateJSONResponse(String input) {
        JSONObject out = new JSONObject();
        out.put("input", input);

        JSONArray sentiments = new JSONArray();

        sentiments.add(input);

        out.put("sentiments", sentiments);

        return out.toString();
    }

    public String generateTextResponse(String input) {
        StringBuilder output = new StringBuilder();

        output.append("input: " + input);
        output.append("\nsentiments:");
        output.append(input);

        return output.toString();
    }
}
