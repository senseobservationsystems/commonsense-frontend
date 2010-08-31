package nl.sense_os.commonsense.server;

import java.util.List;

import javax.jdo.PersistenceManager;
import nl.sense_os.commonsense.server.data.PMF;
import nl.sense_os.commonsense.server.data.SensorType;
import nl.sense_os.commonsense.server.data.SensorValue;
import nl.sense_os.commonsense.server.utility.SensorValueConverter;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;


@SuppressWarnings("serial")
public class IVOSensorValueServlet extends IVOServlet {
	
	@SuppressWarnings("unchecked")
	protected String create(JSONObject change) throws JSONException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			String query = "select from " + SensorType.class.getName() + " WHERE id == " + change.getString("sensor_type");
			List<SensorType> sensorTypes = (List<SensorType>) pm.newQuery(query).execute();
			if (sensorTypes.size() > 0) {
	    	SensorType sensorType = sensorTypes.get(0);
			SensorValue sensorValue = SensorValueConverter.jsonToEntity(change, sensorType.getType());
				pm.makePersistent(sensorValue);
			} else
				log.warning("Could not find corresponding sensor type in datastore.");
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
