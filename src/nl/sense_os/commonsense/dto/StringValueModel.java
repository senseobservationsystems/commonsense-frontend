package nl.sense_os.commonsense.dto;

import java.sql.Timestamp;

public class StringValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;

    public StringValueModel() {
        // empty constructor for serializing
    }
    
    public StringValueModel(Timestamp timestamp, String name, String value) {
        super(timestamp, name, SensorValueModel.STRING);
        setValue(value);
    }
    
    public String getValue() {
        return get("value");
    }
    
    public StringValueModel setValue(String value) {
        set("value", value);
        return this;
    }
}
