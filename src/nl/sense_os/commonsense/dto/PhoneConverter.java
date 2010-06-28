package nl.sense_os.commonsense.dto;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

import nl.sense_os.commonsense.server.data.Phone;

public class PhoneConverter {

	public static PhoneModel entityToModel(Phone phone) {  
	   PhoneModel phoneModel = new PhoneModel(
			   phone.getId(),
			   phone.getBrand(),
			   phone.getType(),
			   phone.getImei(),
			   phone.getIp(),
			   phone.getNumber(),
			   phone.getDate());  
	   return phoneModel;  
	}
	
	public static Phone jsonToEntity(JSONObject jsonPhone) {
		Phone p = new Phone();
		try {
			p.setId((String) jsonPhone.get("id"));
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
