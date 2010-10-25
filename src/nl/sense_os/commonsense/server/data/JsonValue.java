package nl.sense_os.commonsense.server.data;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class JsonValue extends SensorValue {

    @Persistent(serialized = "true")
    String fields;
    
    public JsonValue(int deviceId, int sensorType, Date timestamp, String fields) throws JSONException {
        super(deviceId, sensorType, timestamp);
        setFields(fields);
    }
    
    public JsonValue setFields(String fields) {
    	this.fields = fields;
    	// System.out.println("Fields set: " + fields);
    	return this;
    }
    
    public String getFields() {
    	return this.fields;
    }
    
    public Map<String, Object> getFieldMap() throws JSONException {
    	HashMap<String, Object> map = new HashMap<String, Object>();

    	JSONObject json = new JSONObject(fields);
        
        JSONArray names = json.names(); 
        for (int i = 0; i < names.length(); i++) {
            String name = names.getString(i);
            
            Serializable property = (Serializable) json.get(name);
            if (property instanceof JSONObject) {
                JSONObject subJson = (JSONObject) property;
                JSONArray subNames = subJson.names();
                for (int j = 0; j < subNames.length(); j++) {
                    String subName = subNames.getString(j);
                    Serializable subProperty = (Serializable) subJson.get(subName);
                    map.put(name + "." + subName, subProperty);
                }
            } else {
                map.put(name, property);
            }
        }
        return map;
    }
}
