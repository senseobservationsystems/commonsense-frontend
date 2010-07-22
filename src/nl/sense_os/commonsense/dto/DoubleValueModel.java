package nl.sense_os.commonsense.dto;

import java.sql.Timestamp;

public class DoubleValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;

    public DoubleValueModel() {
        
    }
    
    public DoubleValueModel(Timestamp timestamp, int type, double value) {
        super(timestamp, type);
        setValue(value);
    }
    
    public void setValue(double value) {
        set("value", value);
    }
    
    public double getValue() {
        return get("value", Double.MIN_VALUE);
    }
}
