package nl.sense_os.commonsense.server.data;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class BooleanValue extends SensorValue {
    
    @Persistent
    private boolean value;
    
    public BooleanValue() {
        
    }
    
    public BooleanValue(int deviceId, int sensorType, Date timestamp, boolean value) {
        super(deviceId, sensorType, timestamp, SensorValue.BOOL);
        setValue(value);
    }
    
    public BooleanValue setValue(boolean value) {
        this.value = value;
        return this;
    }
    
    public boolean getValue() {
        return this.value;
    }
}
