package nl.sense_os.commonsense.server.data;

public class Sensor {

	private int id;
	private String name;
	private int phoneId;

	public Sensor() {
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getPhoneId() {
        return this.phoneId;
    }

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}
	
    public void setPhoneId(int phoneId) {
	    this.phoneId = phoneId;
	}
}
