package nl.sense_os.commonsense.dto.sensorvalues;

import java.util.Date;

public class BooleanValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;

    public BooleanValueModel() {
        
    }
    
    public BooleanValueModel(Date timestamp, boolean value) {
        super(timestamp, SensorValueModel.BOOL);
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
