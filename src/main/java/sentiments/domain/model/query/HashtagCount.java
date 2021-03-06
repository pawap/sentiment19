package sentiments.domain.model.query;

import net.sf.json.JSONObject;

/**
 * @author Paw
 */
public class HashtagCount extends Count{

    public String hashtag;

    @Override
    public String toString() {
        return hashtag + ": " + count + "; ";
    }

    public JSONObject toJSONObject() {
        JSONObject o = new JSONObject();
        o.put("count", count);
        o.put("hashtag", hashtag);
        return o;
    }
}
