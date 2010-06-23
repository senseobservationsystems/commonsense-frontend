package nl.sense_os.commonsense.server;

import nl.sense_os.commonsense.data.Phone;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public class PhoneFactory {

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
	
}
