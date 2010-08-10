package nl.sense_os.commonsense.dto;

import java.sql.Timestamp;

public class FloatValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;

    public FloatValueModel() {
        
    }
    
    public FloatValueModel(Timestamp timestamp, String name, double value) {
        super(timestamp, name, SensorValueModel.FLOAT);
        setValue(value);
    }
    
    public double getValue() {
        return get("value", Double.MIN_VALUE);
    }
    
    public FloatValueModel setValue(double value) {
        set("value", value);
        return this;
    }
}
