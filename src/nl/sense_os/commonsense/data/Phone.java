package nl.sense_os.commonsense.data;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class Phone implements Serializable {
	
	private String id;
	private String brand;
	private String type;
	private String imei;
	private String ip;
	private String number;
	private String date;
	
	private List<Sensor> sensors;

	public Phone() {
	}
	
	public String getId() {
		return id;
	}

	public String getBrand() {
		return brand;
	}

	public String getType() {
		return type;
	}

	public String getImei() {
		return imei;
	}

	public String getIp() {
		return ip;
	}

	public String getNumber() {
		return number;
	}

	public String getDate() {
		return date;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public List<Sensor> getSensors() {
		return sensors;
	}

	public void setSensors(List<Sensor> sensors) {
		this.sensors = sensors;
	}
}
