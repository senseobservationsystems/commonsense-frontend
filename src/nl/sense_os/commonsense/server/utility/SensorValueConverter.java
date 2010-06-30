package nl.sense_os.commonsense.server.utility;


import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.server.data.SensorValue;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public class SensorValueConverter {

	public static SensorValueModel entityToModel(SensorValue sensorValue) {  
		   SensorValueModel sensorValueModel = new SensorValueModel(
				   sensorValue.getTimestamp(),
				   sensorValue.getValue());
		   return sensorValueModel;  
		}

	public static SensorValue jsonToEntity(JSONObject jsonSensorValue) {
		SensorValue s = new SensorValue();
		try {
			String ts = (String) jsonSensorValue.get("timestamp");
			s.setTimestamp(TimestampConverter.microEpochToTimestamp(ts));
			s.setValue((String) jsonSensorValue.get("value"));
		} catch (JSONException e) {
		}
		return s;
	}

}
