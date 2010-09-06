package nl.sense_os.commonsense.dto;

import java.util.Date;
import java.util.Map;

public class JsonValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;
    
    public JsonValueModel() {
        // empty constructor for serializing
    }    

    public JsonValueModel(Date timestamp, Map<String, Object> fields) {
        super(timestamp, SensorValueModel.JSON);
        
        setFields(fields);
    }
    
    public Map<String, Object> getFields() {        
        return get("fields");
    }
    
    public JsonValueModel setFields(Map<String, Object> fields) {
        set("fields", fields);
        return this;
    }
}
