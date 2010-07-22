package nl.sense_os.commonsense.dto;

public class SensorModel extends SenseTreeModel {

    private static final long serialVersionUID = 1L;

    public SensorModel() {
        // empty constructor
    }

    public SensorModel(int type, String name, int phoneId) {
        setId(type);
        setPhoneId(phoneId);
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
    public int getPhoneId() {
        return get("phone_id", -1);
    }

    /**
     * Sets the sensor's name and also its "text" property, which is often used in the UI.
     * @param name the name.
     */
    public void setName(String name) {
        set("name", name);
        
        int phoneId = getPhoneId();
        if (-1 != phoneId) {
            setText(phoneId + ". " + name);
        } else {
            setText(name);
        }
    }
    
    public void setPhoneId(int phoneId) {
        set("phone_id", phoneId);
    }
    
    /**
     * @param text String describing the sensor model, often used in the UI.
     */
    public void setText(String text) {
        set("text", text);
    }
}
