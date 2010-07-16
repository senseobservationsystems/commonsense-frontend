package nl.sense_os.commonsense.dto;

import java.sql.Timestamp;

import nl.sense_os.commonsense.server.data.SensorValue;

import com.extjs.gxt.ui.client.data.BaseModel;

public class SensorValueModel extends BaseModel {

    private static final long serialVersionUID = 1L;

    public SensorValueModel() {
        // empty constructor necessary for serializing
    }

    @SuppressWarnings("deprecation")
    public SensorValueModel(Timestamp timestamp, int type, Object value) {
        setTimestamp(timestamp);
        setType(type);

        // different value types for different sensor types (types with JSON values have separate
        // SensorValueModel implementations.)
        switch (type) {
        case SensorValue.UNREAD_MSG:
            setBooleanValue((Boolean) value);
            break;
        case SensorValue.NOISE:
            setFloatValue((Float) value);
            break;
        case SensorValue.AUDIOSTREAM:
        case SensorValue.BLUETOOTH_ADDR:
        case SensorValue.DATA_CONNECTION:
        case SensorValue.IP:
        case SensorValue.MIC:
            setStringValue((String) value);
            break;
        }
    }

    public boolean getBooleanValue() {
        return get("value", false);
    }

    public float getFloatValue() {
        return get("value", -1f);
    }

    public int getIntValue() {
        return get("value", -1);
    }

    public String getStringValue() {
        return get("value");
    }

    public Timestamp getTimestamp() {
        return get("timestamp");
    }

    public int getType() {
        return get("type", -1);
    }

    public void setBooleanValue(boolean value) {
        set("value", value);
    }

    public void setFloatValue(float value) {
        set("value", value);
    }

    public void setIntValue(int value) {
        set("value", value);
    }

    public void setStringValue(String value) {
        set("value", value);
    }

    public void setTimestamp(Timestamp timestamp) {
        set("timestamp", timestamp);
    }

    public void setType(int type) {
        set("type", type);
    }
}
