package nl.sense_os.commonsense.server;

import nl.sense_os.commonsense.data.Phone;
import nl.sense_os.commonsense.data.Sensor;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public class DataFactory {

	public static Phone createPhone(String id, JSONObject jsonPhone) {
		Phone p = new Phone(id);
		try {
			p.setBrand((String) jsonPhone.get("brand"));
			p.setType((String) jsonPhone.get("type"));
			p.setImei((String) jsonPhone.get("imei"));
			p.setIp((String) jsonPhone.get("ip"));
			p.setNumber((String) jsonPhone.get("number"));
			p.setDate((String) jsonPhone.get("date"));	
		} catch (JSONException e) {
		}
		return p;
	}
	
	public static Sensor createSensor(String id, JSONObject jsonSensor) {
		Sensor s = new Sensor(id);
		try {
			s.setName((String) jsonSensor.get("name"));
		} catch (JSONException e) {
		}
		return s;
	}
}
