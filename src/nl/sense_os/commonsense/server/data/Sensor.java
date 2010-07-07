package nl.sense_os.commonsense.server.data;

public class Sensor {

	private String id;
	private String name;
	private String phoneId;

	public Sensor() {
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPhoneId() {
        return this.phoneId;
    }

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}
	
    public void setPhoneId(String phoneId) {
	    this.phoneId = phoneId;
	}
}
