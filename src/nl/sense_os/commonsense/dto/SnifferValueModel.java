package nl.sense_os.commonsense.dto;

import java.sql.Timestamp;

public class SnifferValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;

    public SnifferValueModel() {
        // empty constructor for Serialization
    }
    
    public SnifferValueModel(Timestamp timestamp, String value, String nodeId, String sensorName,
            String variance) {
        super(timestamp, value);
        setNodeId(nodeId);
        setSensorName(sensorName);
        setVariance(variance);
    }
    
    public String getNodeId() {
        return get("node_id");
    }
    
    public String getSensorName() {
        return get("sensor_name");
    }
    
    public String getVariance() {
        return get("variance");
    }
    
    public String getValue() {
        return get("value");
    }

    public void setNodeId(String nodeId) {
        set("node_id", nodeId);
    }

    public void setSensorName(String sensorName) {
        set("sensor_name", sensorName);
    }

    public void setVariance(String variance) {
        set("variance", variance);
    }
}
