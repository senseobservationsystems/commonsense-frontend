package nl.sense_os.commonsense.dto;

import java.sql.Timestamp;

import nl.sense_os.commonsense.server.data.SensorValue;

public class MyriaTempValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;

    public MyriaTempValueModel() {
        // empty constructor for Serialization
    }
    
    public MyriaTempValueModel(Timestamp timestamp, int nodeId, String sensorName, int value,
            int variance) {
        super(timestamp, SensorValue.MYRIA_TEMPERATURE, null);
        setNodeId(nodeId);
        setSensorName(sensorName);
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
    
    public int getIntValue() {
        return get("value", -1);
    }

    public void setNodeId(int nodeId) {
        set("node_id", nodeId);
    }

    public void setSensorName(String sensorName) {
        set("sensor_name", sensorName);
    }

    public void setVariance(int variance) {
        set("variance", variance);
    }
}
