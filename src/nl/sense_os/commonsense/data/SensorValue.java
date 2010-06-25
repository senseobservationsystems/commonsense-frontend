package nl.sense_os.commonsense.data;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class SensorValue implements Serializable {

	private Date date;
	private String value;

	public SensorValue() {
	}

	public SensorValue(Date date, String value) {
		this.date = date;
		this.value = value;
	}
	
	public Date getDate() {
		return date;
	}

	public String getValue() {
		return value;
	}
	
	
	public void setDate(Date date) {
		this.date = date;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
