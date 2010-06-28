package nl.sense_os.commonsense.server.utility;


import nl.sense_os.commonsense.dto.SensorModel;
import nl.sense_os.commonsense.server.data.Sensor;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public class SensorConverter {

	public static SensorModel entityToModel(Sensor sensor) {  
		   SensorModel sensorModel = new SensorModel(
				   sensor.getId(),
				   sensor.getName());
		   return sensorModel;  
		}

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
