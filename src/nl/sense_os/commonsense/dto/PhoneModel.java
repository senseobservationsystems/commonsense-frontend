package nl.sense_os.commonsense.dto;

public class PhoneModel extends SenseTreeModel {

    private static final long serialVersionUID = 1L;

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
        
        String id = getId();
        if (null != id) {
            setText(type + "(" + id + ")");
        } else {
            setText(type);
        }
    }
}
