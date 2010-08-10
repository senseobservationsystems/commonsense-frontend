package nl.sense_os.commonsense.dto;

import java.sql.Timestamp;
import java.util.Map;

public class JsonValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;
    
    public JsonValueModel() {
        // empty constructor for serializing
    }
    
    public JsonValueModel(Timestamp timestamp, String name, Map<String, String> fields) {
        super(timestamp, name, SensorValueModel.JSON);
        
        setFields(fields);
    }
    
    public Map<String, String> getFields() {
        // TODO
        return null;
    }
    
    public JsonValueModel setFields(Map<String, String> fields) {
        // TODO
        return this;
    }
}
