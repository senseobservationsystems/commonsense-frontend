package nl.sense_os.commonsense.dto;

import nl.sense_os.commonsense.pojo.Sensor;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public class SensorConverter {

	public static Sensor jsonToEntity(JSONObject jsonSensor) {
		Sensor s = new Sensor();
		try {
			s.setId((String) jsonSensor.get("id"));
			s.setName((String) jsonSensor.get("name"));
		} catch (JSONException e) {
		}
		return s;
	}

}
