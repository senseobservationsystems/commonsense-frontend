package nl.sense_os.commonsense.dto;

import java.sql.Timestamp;

public class BooleanValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;

    public BooleanValueModel() {
        
    }
    
    public BooleanValueModel(Timestamp timestamp, int type, boolean value) {
        super(timestamp, type);
        setValue(value);
    }
    
    public void setValue(boolean value) {
        set("value", value);
    }
    
    public boolean getValue() {
        return get("value", false);
    }
}
