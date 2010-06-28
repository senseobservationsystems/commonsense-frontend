package nl.sense_os.commonsense.data;

import com.extjs.gxt.ui.client.data.BaseModel;

import java.io.Serializable;
import java.util.List;

public class Phone extends BaseModel implements Serializable {
	
//	private String id;
//	private String brand;
//	private String type;
//	private String imei;
//	private String ip;
//	private String number;
//	private String date;

    private static final long serialVersionUID = 1L;
	
	private List<Sensor> sensors;

    public Phone() {
	    
	}

    public Phone(String id) {
        setId(id);
    }
	
	public Phone(String id, String brand, String type, String imei, String ip, String number, String date){
	    setId(id);
	    setBrand(brand);
	    setType(type);
	    setImei(imei);
	    setIp(ip);
	    setNumber(number);
	    setDate(date);
	}
		
	public String getId() {
		return get("id");
	}

	public String getBrand() {
		return get("brand");
	}

	public String getType() {
		return get("type");
	}

	public String getImei() {
		return get("imei");
	}

	public String getIp() {
		return get("ip");
	}

	public String getNumber() {
		return get("number");
	}

	public String getDate() {
		return get("date");
	}

	public void setId(String id) {
        set("id", id);
//		this.id = id;
	}

	public void setBrand(String brand) {
        set("brand", brand);
//		this.brand = brand;
	}

	public void setType(String type) {
        set("type", type);
//		this.type = type;
	}

	public void setImei(String imei) {
        set("imei", imei);
//		this.imei = imei;
	}

	public void setIp(String ip) {
        set("ip", ip);
//		this.ip = ip;
	}

	public void setNumber(String number) {
        set("number", number);
//		this.number = number;
	}

	public void setDate(String date) {
        set("date", date);
//		this.date = date;
	}
	public List<Sensor> getSensors() {
		return sensors;
	}

	public void setSensors(List<Sensor> sensors) {
		this.sensors = sensors;
	}
}
