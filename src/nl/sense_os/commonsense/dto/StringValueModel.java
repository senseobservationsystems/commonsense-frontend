package nl.sense_os.commonsense.dto;

import java.sql.Timestamp;

public class StringValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;

    public StringValueModel() {
        
    }
    
    public StringValueModel(Timestamp timestamp, int type, String value) {
        super(timestamp, type);
        setValue(value);
    }
    
    public void setValue(String value) {
        set("value", value);
    }
    
    public String getValue() {
        return get("value");
    }
}
