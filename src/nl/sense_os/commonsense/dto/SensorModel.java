package nl.sense_os.commonsense.dto;

public class SensorModel extends SenseTreeModel {

    private static final long serialVersionUID = 1L;

    public SensorModel() {
        // empty constructor
    }

    public SensorModel(String id, String name, String phone) {
        setId(id);
        setName(name);
        setPhone(phone);
    }

    public String getName() {
        return get("name", "NAME");
    }
    
    public String getPhone() {
        return get("phone", "PHONE");
    }

    public void setName(String name) {
        set("name", name);
        set("text", name);
    }
    
    public void setPhone(String phoneId) {
        set("phone", phoneId);
    }
}
