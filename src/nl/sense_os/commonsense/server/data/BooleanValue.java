package nl.sense_os.commonsense.server.data;

import java.sql.Timestamp;

public class BooleanValue extends SensorValue {
    
    private boolean value;
    
    public BooleanValue() {
        
    }
    
    public BooleanValue(Timestamp timestamp, int type, boolean value) {
        super(timestamp, type);
        setValue(value);
    }
    
    public void setValue(boolean value) {
        this.value = value;
    }
    
    public boolean getValue() {
        return this.value;
    }
}
