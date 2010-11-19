package nl.sense_os.commonsense.dto.building;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.Date;

public class BuildingModel extends BaseModel implements IsSerializable {

    public static final String KEY_CREATED = "created";
    public static final String KEY_DB_KEY = "key";
    public static final String KEY_FLOORS = "floors";
    public static final String KEY_MODIFIED = "modified";
    public static final String KEY_NAME = "name";
    public static final String KEY_USER = "user_id";
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private ArrayList<FloorModel> _floors; // unused field is required for using the list in GWT RPC

    public BuildingModel() {

    }

    public BuildingModel(String name, ArrayList<FloorModel> floors, String userId, Date created,
            Date modified) {
        setName(name);
        setFloors(floors);
        setUserId(userId);
        setCreated(created);
        setModified(modified);
    }

    /**
     * @return the creation date, or a new Date(0)
     */
    public Date getCreated() {
        return get(KEY_CREATED, new Date(0));
    }

    /**
     * @return the list of FloorModels, or an empty list
     */
    public ArrayList<FloorModel> getFloors() {
        return get(KEY_FLOORS, new ArrayList<FloorModel>());
    }

    /**
     * @return the datstore key String, or null
     */
    public String getKey() {
        return get(KEY_DB_KEY);
    }

    /**
     * @return the latest modification date, or a new Date(0)
     */
    public Date getModified() {
        return get(KEY_MODIFIED, new Date(0));
    }

    /**
     * @return the name, or ""
     */
    public String getName() {
        return get(KEY_NAME, "");
    }

    /**
     * @return the ID of this building's user, or null if it was not set
     */
    public String getUserId() {
        return get(KEY_USER);
    }

    /**
     * @param date
     *            creation date
     */
    public void setCreated(Date date) {
        set(KEY_CREATED, date);
    }

    /**
     * @param floors
     *            list of FloorModel for this building
     */
    public void setFloors(ArrayList<FloorModel> floors) {
        set(KEY_FLOORS, floors);
    }

    /**
     * @param String
     *            representation of the datastore key
     */
    public void setKey(String key) {
        set(KEY_DB_KEY, key);
    }

    /**
     * @param date
     *            modification date
     */
    public void setModified(Date date) {
        set(KEY_MODIFIED, date);
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        set(KEY_NAME, name);
        set("text", name); // "text" is often used as default in GXT
    }

    /**
     * @param userId
     *            the user ID to set
     */
    public void setUserId(String userId) {
        set(KEY_USER, userId);
    }
}
