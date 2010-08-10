package nl.sense_os.commonsense.server.data;

import java.sql.Timestamp;

public class FloatValue extends SensorValue {

    private double value; 
    
    public FloatValue() {
        
    }
    
    public FloatValue(Timestamp timestamp, String name, double value) {
        super(timestamp, name, SensorValue.FLOAT);
        setValue(value);
    }
    
    public FloatValue setValue(double value) {
        this.value = value;
        return this;
    }
    
    public double getValue() {
        return this.value;
    }
}
