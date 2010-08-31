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
    Map<String, String> fields;
    
    public JsonValue(int deviceId, int sensorType, Date timestamp, String fields) throws JSONException {
        super(deviceId, sensorType, timestamp, SensorValue.JSON);
        
        setFields(fields);
    }
    
    public JsonValue setFields(String fields) throws JSONException {
        this.fields = new HashMap<String, String>();
        JSONObject json = new JSONObject(fields);
        
        JSONArray names = json.names(); 
        for (int i = 0; i < names.length(); i++) {
            String name = names.getString(i);
            this.fields.put(name, json.getString(name));
        }
        
        return this;
    }
    
    public Map<String, String> getFields() {
        return this.fields;
    }
}
