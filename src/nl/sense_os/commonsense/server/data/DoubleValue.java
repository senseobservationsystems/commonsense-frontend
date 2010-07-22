package nl.sense_os.commonsense.server.data;

import java.sql.Timestamp;

public class DoubleValue extends SensorValue {

    private double value; 
    
    public DoubleValue() {
        
    }
    
    public DoubleValue(Timestamp timestamp, int type, double value) {
        super(timestamp, type);
        setValue(value);
    }
    
    public void setValue(double value) {
        this.value = value;
    }
    
    public double getValue() {
        return this.value;
    }
}
