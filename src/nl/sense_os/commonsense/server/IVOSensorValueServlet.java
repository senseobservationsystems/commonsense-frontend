package nl.sense_os.commonsense.server;

import java.util.HashMap;
import java.util.List;

import javax.jdo.PersistenceManager;

import nl.sense_os.commonsense.server.data.PMF;
import nl.sense_os.commonsense.server.data.SensorType;
import nl.sense_os.commonsense.server.utility.BooleanValueConverter;
import nl.sense_os.commonsense.server.utility.FloatValueConverter;
import nl.sense_os.commonsense.server.utility.JsonValueConverter;
import nl.sense_os.commonsense.server.utility.StringValueConverter;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;


@SuppressWarnings("serial")
public class IVOSensorValueServlet extends IVOServlet {
	
	private HashMap<Integer, SensorType> sensorTypes;
	private PersistenceManager pm;
	
	@SuppressWarnings("unchecked")
	protected void initialize() {
		sensorTypes = new HashMap<Integer, SensorType>();
		pm = PMF.get().getPersistenceManager();
			String query = "select from " + SensorType.class.getName() + " order by id";
			List<SensorType> rawList = (List<SensorType>) pm.newQuery(query).execute();
			SensorType s;
			for (int i = 0; i < rawList.size(); i++) {
				s = rawList.get(i);
				sensorTypes.put(s.getId(), s);
			}
	}
	
	protected String create(JSONObject change) throws JSONException {
			if (sensorTypes.size() > 0) {
				SensorType sensorType = sensorTypes.get(change.getInt("sensor_type"));
				switch (sensorType.getType()) {
					case SensorType.BOOL: 
						pm.makePersistent(BooleanValueConverter.jsonToEntity(change));
						break;
					case SensorType.FLOAT: 
						pm.makePersistent(FloatValueConverter.jsonToEntity(change));
						break;
					case SensorType.JSON: 
						pm.makePersistent(JsonValueConverter.jsonToEntity(change));
						break;
					case SensorType.STRING: 
						pm.makePersistent(StringValueConverter.jsonToEntity(change));
						break;
				}
			} else
				log.warning("Could not find corresponding sensor type in datastore.");
        return change.getString("id");
	}
		
	protected String update(JSONObject change) throws JSONException {
		return Integer.toString(((JSONObject) change.get("old")).getInt("id"));
	}

	protected String delete(JSONObject change) throws JSONException {
        return Integer.toString(change.getInt("id"));
	}
	
	protected void finalize() {
		pm.close();
	}

}
