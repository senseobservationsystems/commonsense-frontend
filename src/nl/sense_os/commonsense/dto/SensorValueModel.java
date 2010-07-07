package nl.sense_os.commonsense.dto;

import java.sql.Timestamp;

import com.extjs.gxt.ui.client.data.BaseModel;

public class SensorValueModel extends BaseModel {

    private static final long serialVersionUID = 1L;

    public SensorValueModel() {
    }

    public SensorValueModel(Timestamp timestamp, String value) {
        setTimestamp(timestamp);
        setValue(value);
    }

    public Timestamp getTimestamp() {
        return get("timestamp");
    }

    public String getValue() {
        return get("value");
    }

    public void setTimestamp(Timestamp timestamp) {
        set("timestamp", timestamp);
    }

    public void setValue(String value) {
        set("value", value);
    }
}
