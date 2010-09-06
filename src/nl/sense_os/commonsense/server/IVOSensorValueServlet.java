package nl.sense_os.commonsense.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.jdo.PersistenceManager;

import nl.sense_os.commonsense.server.data.BooleanValue;
import nl.sense_os.commonsense.server.data.FloatValue;
import nl.sense_os.commonsense.server.data.JsonValue;
import nl.sense_os.commonsense.server.data.StringValue;
import nl.sense_os.commonsense.server.data.PMF;
import nl.sense_os.commonsense.server.data.SensorType;
import nl.sense_os.commonsense.server.data.SensorValue;
import nl.sense_os.commonsense.server.utility.SensorValueConverter;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;


@SuppressWarnings("serial")
public class IVOSensorValueServlet extends IVOServlet {
	
	private HashMap<Integer, SensorType> sensorTypes;
	private List<BooleanValue> booleanValues;
	private List<FloatValue> floatValues;
	private List<JsonValue> jsonValues;
	private List<StringValue> stringValues;
	
	public IVOSensorValueServlet() {
		super();
		booleanValues = new ArrayList<BooleanValue>();
		floatValues = new ArrayList<FloatValue>();
		jsonValues = new ArrayList<JsonValue>();
		stringValues = new ArrayList<StringValue>();
	}
	
	@SuppressWarnings("unchecked")
	protected void initialize() {
		sensorTypes = new HashMap<Integer, SensorType>();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			String query = "select from " + SensorType.class.getName() + " order by id";
			List<SensorType> rawList = (List<SensorType>) pm.newQuery(query).execute();
			SensorType s;
			for (int i = 0; i < rawList.size(); i++) {
				s = rawList.get(i);
				sensorTypes.put(s.getId(), s);
			}
		} finally {
			pm.close();
		}
		
	}
	
	protected String create(JSONObject change) throws JSONException {
			if (sensorTypes.size() > 0) {
				SensorType sensorType = sensorTypes.get(change.getInt("sensor_type"));
				SensorValue sensorValue = SensorValueConverter.jsonToEntity(change, sensorType.getType());
				// for storage, we need to explicitly differentiate between sensor value types... 
				switch (sensorType.getType()) {
					case SensorType.BOOL: 
						booleanValues.add((BooleanValue) sensorValue);
						break;
					case SensorType.FLOAT: 
						floatValues.add((FloatValue) sensorValue);
						break;
					case SensorType.JSON: 
						jsonValues.add((JsonValue) sensorValue);
						break;
					case SensorType.STRING: 
						stringValues.add((StringValue) sensorValue);
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
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistentAll(booleanValues);
			pm.makePersistentAll(floatValues);
			pm.makePersistentAll(jsonValues);
			pm.makePersistentAll(stringValues);
		} finally {
			pm.close();	
		}
	}

}
