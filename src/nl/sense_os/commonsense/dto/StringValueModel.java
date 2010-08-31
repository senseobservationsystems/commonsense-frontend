package nl.sense_os.commonsense.dto;

import java.util.Date;

public class StringValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;

    public StringValueModel() {
        // empty constructor for serializing
    }
    
    public StringValueModel(Date timestamp, String value) {
        super(timestamp, SensorValueModel.STRING);
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
