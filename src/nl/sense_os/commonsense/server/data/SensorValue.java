package nl.sense_os.commonsense.server.data;

import java.sql.Timestamp;

public class SensorValue {

	private Timestamp timestamp;
	private String value;

	public SensorValue() {
	}

	public SensorValue(Timestamp timestamp, String value) {
		this.timestamp = timestamp;
		this.value = value;
	}
	
	public Timestamp getTimestamp() {
		return timestamp;
	}

	public String getValue() {
		return value;
	}
	
	
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
