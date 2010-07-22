package nl.sense_os.commonsense.server.data;

import java.sql.Timestamp;

public class StringValue extends SensorValue {
    
    private String value;
    
    public StringValue() {
        
    }
    
    public StringValue(Timestamp timestamp, int type, String value) {
        super(timestamp, type);
        setValue(value);
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
}
