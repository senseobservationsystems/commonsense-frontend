package nl.sense_os.commonsense.dto;

public class SensorModel extends SenseTreeModel {

    private static final long serialVersionUID = 1L;

    public SensorModel() {
        // empty constructor
    }

    public SensorModel(String id, String name, String phone) {
        setId(id);
        setPhoneId(phone);
        setName(name);
    }
    
    /**
     * @return the sensor's name, or null if it was not set.
     */
    public String getName() {
        return get("name");
    }
    
    /**
     * @return the sensor's phone id, or null if it was not set.
     */
    public String getPhoneId() {
        return get("phone_id");
    }

    /**
     * Sets the sensor's name and also its "text" property, which is often used in the UI.
     * @param name the name.
     */
    public void setName(String name) {
        set("name", name);
        
        String phoneId = getPhoneId();
        if (null != phoneId) {
            setText(phoneId + ". " + name);
        } else {
            setText(name);
        }
    }
    
    public void setPhoneId(String phoneId) {
        set("phone_id", phoneId);
    }
    
    /**
     * @param text String describing the sensor model, often used in the UI.
     */
    public void setText(String text) {
        set("text", text);
    }
}
