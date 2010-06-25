package nl.sense_os.commonsense.data;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class Sensor implements Serializable {

	private String id;
	private String name;

	private List<SensorValue> values;
	
	public Sensor() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<SensorValue> getValues() {
		return values;
	}

	public void setValues(List<SensorValue> values) {
		this.values = values;
	}
}
