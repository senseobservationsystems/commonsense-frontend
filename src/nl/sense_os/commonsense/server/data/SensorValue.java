package nl.sense_os.commonsense.server.data;

import java.sql.Timestamp;

public abstract class SensorValue {
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
    /**
     * String sensor value.
     */
    public static final int STRING = 4;
    
	private String name;
	private Timestamp timestamp;
	private int type;

	public SensorValue() {
	    
	}

	public SensorValue(Timestamp timestamp, String name, int type) {
		setTimestamp(timestamp);
		setName(name);
		setType(type);
	}
	
	public String getName() {
	    return this.name;
	}
	
	public Timestamp getTimestamp() {
		return timestamp;
	}
	
	public int getType() {
	    return type;
	}
	
	public SensorValue setName(String name) {
	    this.name = name;
        return this;
	}
	
	public SensorValue setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
		return this;
	}
	
	public SensorValue setType(int type) {
	    this.type = type;
        return this;
	}
}
