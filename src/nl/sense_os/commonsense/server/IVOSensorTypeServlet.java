package nl.sense_os.commonsense.server;

import javax.jdo.PersistenceManager;
import nl.sense_os.commonsense.server.data.PMF;
import nl.sense_os.commonsense.server.data.SensorType;
import nl.sense_os.commonsense.server.utility.SensorTypeConverter;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;


@SuppressWarnings("serial")
public class IVOSensorTypeServlet extends IVOServlet {
	
	protected String create(JSONObject change) throws JSONException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		SensorType sensorType = SensorTypeConverter.jsonToEntity(change);
   	    try {
            pm.makePersistent(sensorType);
        } finally {
            pm.close();
        }
        return Integer.toString(change.getInt("id"));
	}
		
	protected String update(JSONObject change) throws JSONException {
		return Integer.toString(((JSONObject) change.get("old")).getInt("id"));
	}

	protected String delete(JSONObject change) throws JSONException {
        return Integer.toString(change.getInt("id"));
	}

}
