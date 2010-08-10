package nl.sense_os.commonsense.dto;

import com.extjs.gxt.ui.client.data.BaseModel;

import java.sql.Timestamp;

public abstract class SensorValueModel extends BaseModel {
    /**
     * Boolean sensor value.
     */
    public static final int BOOL = 1;
    /**
     * Float sensor value.
     */
    public static final int FLOAT = 2;
    /**
     * JSON sensor value.
     */
    public static final int JSON = 3;
    private static final long serialVersionUID = 1L;
    /**
     * String sensor value.
     */
    public static final int STRING = 4;

    public SensorValueModel() {
        // empty constructor necessary for serializing
    }

    public SensorValueModel(Timestamp timestamp, String name, int type) {
        setTimestamp(timestamp);
        setName(name);
        setType(type);
    }
    
    public String getName() {
        return get("name");
    }

    public Timestamp getTimestamp() {
        return get("timestamp");
    }

    public int getType() {
        return get("type", -1);
    }
    
    public SensorValueModel setName(String name) {
        set("name", name);
        return this;
    }

    public SensorValueModel setTimestamp(Timestamp timestamp) {
        set("timestamp", timestamp);
        return this;
    }

    public SensorValueModel setType(int type) {
        set("type", type);
        return this;
    }
}
