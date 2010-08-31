package nl.sense_os.commonsense.dto;

import java.util.Date;

public class FloatValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;

    public FloatValueModel() {
        
    }
    
    public FloatValueModel(Date timestamp, double value) {
        super(timestamp, SensorValueModel.FLOAT);
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
