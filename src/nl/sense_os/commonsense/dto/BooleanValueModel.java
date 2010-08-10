package nl.sense_os.commonsense.dto;

import java.sql.Timestamp;

public class BooleanValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;

    public BooleanValueModel() {
        
    }
    
    public BooleanValueModel(Timestamp timestamp, String name, boolean value) {
        super(timestamp, name, SensorValueModel.BOOL);
        setValue(value);
    }
    
    public boolean getValue() {
        return get("value", false);
    }
    
    public BooleanValueModel setValue(boolean value) {
        set("value", value);
        return this;
    }
}
