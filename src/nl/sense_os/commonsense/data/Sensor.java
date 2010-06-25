package nl.sense_os.commonsense.data;

import java.io.Serializable;
import java.util.Collection;

@SuppressWarnings("serial")
public class Sensor implements Serializable {

	private String id;
	private String name;

	private Collection<SensorValue> values;
	
	public Sensor(String id) {
		this.id = id;
		this.name = "";
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

}
