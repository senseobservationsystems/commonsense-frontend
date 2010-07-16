package nl.sense_os.commonsense.server.utility;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

import java.util.logging.Logger;

import nl.sense_os.commonsense.dto.PhoneModel;
import nl.sense_os.commonsense.server.data.Phone;

public class PhoneConverter {

    private static final Logger log = Logger.getLogger("PhoneConverter");
    
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
			p.setType((String) jsonPhone.get("type"));
			p.setImei((String) jsonPhone.get("imei"));
			p.setDate((String) jsonPhone.get("date"));	
		} catch (JSONException e) {
		    log.warning("JSONException deserializing phone");
		}
		return p;
	}
}
