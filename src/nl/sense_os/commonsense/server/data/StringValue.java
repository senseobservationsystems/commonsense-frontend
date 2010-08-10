package nl.sense_os.commonsense.server.data;

import java.sql.Timestamp;

public class StringValue extends SensorValue {
    
    private String value;
    
    public StringValue() {
        
    }
    
    public StringValue(Timestamp timestamp, String name, String value) {
        super(timestamp, name, SensorValue.STRING);
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
