package nl.sense_os.commonsense.server.data;

public class Sensor {

	private String id;
	private String name;
	private String phone;

	public Sensor() {
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPhone() {
        return this.phone;
    }

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}
	
    public void setPhone(String phone) {
	    this.phone = phone;
	}
}
