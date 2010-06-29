package nl.sense_os.commonsense.dto;

import com.extjs.gxt.ui.client.data.BaseModel;

@SuppressWarnings("serial")
public class PhoneModel extends BaseModel {

    public PhoneModel() {
    }

    public PhoneModel(String id, String brand, String type, String imei, String ip, String number,
            String date) {
        setId(id);
        setBrand(brand);
        setType(type);
        setImei(imei);
        setIp(ip);
        setNumber(number);
        setDate(date);
    }

    public String getBrand() {
        return get("brand");
    }

    public String getDate() {
        return get("date");
    }

    public String getId() {
        return get("id");
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

    public String getType() {
        return get("type");
    }

    public void setBrand(String brand) {
        set("brand", brand);
    }

    public void setDate(String date) {
        set("date", date);
    }

    public void setId(String id) {
        set("id", id);

        if (null == getType()) {
            setText(id);
        } else {
            setText(id + ". " + getType());
        }
    }

    public void setImei(String imei) {
        set("imei", imei);
    }

    public void setIp(String ip) {
        set("ip", ip);
    }

    public void setNumber(String number) {
        set("number", number);
    }

    public void setText(String text) {
        set("text", text);
    }

    public void setType(String type) {
        set("type", type);

        if (null == getId()) {
            setText(type);
        } else {
            setText(getId() + ". " + type);
        }
    }

//    public List<Sensor> getSensors() {
//        return sensors;
//    }
//
//    public void setSensors(List<Sensor> sensors) {
//        this.sensors = sensors;
//    }

}
