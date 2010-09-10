package nl.sense_os.commonsense.server.data;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class FloatValue extends SensorValue {

    @Persistent
    private double value; 
    
    public FloatValue() {
        
    }
    
    public FloatValue(int deviceId, int sensorType, Date timestamp, double value) {
        super(deviceId, sensorType, timestamp);
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
