package sentiments;

import net.sf.json.JSONObject;

public interface BasicDataHandlerInterface {

	/**
	 * raw entry point for ML-Team. 
	 * 
	 * @return
	 */
	public Object getDBConnection();
	
	/**
	 * raw entry point for FE-Team.
	 * 
	 * @return Structure is subject to discussion.
	 */
	public JSONObject getStatistics();
	
}
