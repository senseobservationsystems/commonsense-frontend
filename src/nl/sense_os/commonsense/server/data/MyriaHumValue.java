package nl.sense_os.commonsense.server.data;

import java.sql.Timestamp;

public class MyriaHumValue extends SensorValue {
    
    private int nodeId;
    private String sensorName;
    private int value;
    private int variance;

    public MyriaHumValue() {
        // empty constructor for Serialization
    }
    
    public MyriaHumValue(Timestamp timestamp, int nodeId, String sensorName, int value,
            int variance) {
        super(timestamp, SensorValue.MYRIA_HUMIDITY);
        setNodeId(nodeId);
        setSensorName(sensorName);
        setValue(value);
        setVariance(variance);
    }
    
    public int getNodeId() {
        return this.nodeId;
    }
    
    public String getSensorName() {
        return this.sensorName;
    }
    
    public int getVariance() {
        return this.variance;
    }
    
    public int getValue() {
        return this.value;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }
    
    public void setValue(int value) {
        this.value = value;
    }

    public void setVariance(int variance) {
        this.variance = variance;
    }
}
