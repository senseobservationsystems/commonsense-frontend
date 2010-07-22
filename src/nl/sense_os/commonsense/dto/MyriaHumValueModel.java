package nl.sense_os.commonsense.dto;

import java.sql.Timestamp;

public class MyriaHumValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;

    public MyriaHumValueModel() {
        // empty constructor for Serialization
    }
    
    public MyriaHumValueModel(Timestamp timestamp, int nodeId, String sensorName, int value,
            int variance) {
        super(timestamp, SensorValueModel.MYRIA_HUMIDITY);
        setNodeId(nodeId);
        setSensorName(sensorName);
        setValue(value);
        setVariance(variance);
    }
    
    public int getNodeId() {
        return get("node_id", -1);
    }
    
    public String getSensorName() {
        return get("sensor_name");
    }
    
    public int getVariance() {
        return get("variance", -1);
    }
    
    public int getValue() {
        return get("value", -1);
    }

    public void setNodeId(int nodeId) {
        set("node_id", nodeId);
    }

    public void setSensorName(String sensorName) {
        set("sensor_name", sensorName);
    }
    
    public void setValue(int value) {
        set("value", value);
    }

    public void setVariance(int variance) {
        set("variance", variance);
    }
}
