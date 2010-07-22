package nl.sense_os.commonsense.server.utility;


import nl.sense_os.commonsense.dto.SensorModel;
import nl.sense_os.commonsense.server.data.Sensor;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

import java.util.logging.Logger;

public class SensorConverter {

    private static final Logger log = Logger.getLogger("SensorConverter");
    
	public static SensorModel entityToModel(Sensor sensor) {  
		   SensorModel sensorModel = new SensorModel(
				   sensor.getId(),
				   sensor.getName(),
		           sensor.getPhoneId());
		   return sensorModel;  
		}

	public static Sensor jsonToEntity(JSONObject jsonSensor, int phoneId) {
		Sensor s = new Sensor();
		try {
			s.setId(jsonSensor.getInt("id"));
			s.setName(jsonSensor.getString("name"));
			s.setPhoneId(phoneId);
		} catch (JSONException e) {
		    log.warning("JSONException in jsonToEntity");
		}
		return s;
	}

}
