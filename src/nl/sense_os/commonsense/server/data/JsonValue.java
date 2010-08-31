package nl.sense_os.commonsense.server.data;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class JsonValue extends SensorValue {

    @Persistent
    Map<String, Object> fields;
    
    public JsonValue(int deviceId, int sensorType, Date timestamp, String fields) throws JSONException {
        super(deviceId, sensorType, timestamp, SensorValue.JSON);
        
        setFields(fields);
    }
    
    public JsonValue setFields(String fields) throws JSONException {
        this.fields = new HashMap<String, Object>();
        JSONObject json = new JSONObject(fields);
        
        JSONArray names = json.names(); 
        for (int i = 0; i < names.length(); i++) {
            String name = names.getString(i);
            
            Object property = json.get(name);
            if (property instanceof JSONObject) {
                JSONObject subJson = (JSONObject) property;
                JSONArray subNames = subJson.names();
                for (int j = 0; j < subNames.length(); j++) {
                    String subName = subNames.getString(j);
                    
                    Object subProperty = subJson.get(subName);
                    this.fields.put(name + "." + subName, subProperty);
                }
            } else {
                this.fields.put(name, property);
            }
        }
        
        return this;
    }
    
    public Map<String, Object> getFields() {
        return this.fields;
    }
}
