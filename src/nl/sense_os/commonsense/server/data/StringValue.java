package nl.sense_os.commonsense.server.data;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class StringValue extends SensorValue {
    
    @Persistent
    private String value;
    
    public StringValue() {
        
    }
    
    public StringValue(int deviceId, int sensorType, Date timestamp, String value) {
        super(deviceId, sensorType, timestamp, SensorValue.STRING);
        setValue(value);
    }
    
    public StringValue setValue(String value) {
        this.value = value;
        return this;
    }
    
    public String getValue() {
        return this.value;
    }
}
