package nl.sense_os.commonsense.data;

import com.google.gwt.json.client.JSONObject;

public class Phone {
	
	private String id;
	private String brand;
	private String type;
	private String imei;
	private String ip;
	private String number;
	private String date;

	public Phone(String id, JSONObject jsonPhone) {
		this.id     = id; 
		this.brand  = jsonPhone.get("brand").toString();
		this.type   = jsonPhone.get("type").toString();
		this.imei   = jsonPhone.get("imei").toString();
		this.ip     = jsonPhone.get("ip").toString();
		this.number = jsonPhone.get("number").toString();
		this.date   = jsonPhone.get("date").toString();	
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
	
}
