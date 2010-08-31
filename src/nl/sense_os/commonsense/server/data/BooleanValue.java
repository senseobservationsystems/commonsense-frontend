package nl.sense_os.commonsense.server.data;

import java.sql.Timestamp;

public class BooleanValue extends SensorValue {
    
    private boolean value;
    
    public BooleanValue() {
        
    }
    
    public BooleanValue(int deviceId, int sensorType, Timestamp timestamp, String name, boolean value) {
        super(deviceId, sensorType, timestamp, name, SensorValue.BOOL);
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
