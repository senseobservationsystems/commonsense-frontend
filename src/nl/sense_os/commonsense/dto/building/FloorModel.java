package nl.sense_os.commonsense.dto.building;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Date;

public class FloorModel extends BaseModel implements IsSerializable {

    public static final String KEY_BLOB_KEY = "blobstore_key";
    public static final String KEY_CREATED = "created";
    public static final String KEY_DB_KEY = "datastore_key";
    public static final String KEY_MODIFIED = "modified";
    public static final String KEY_NAME = "name";
    public static final String KEY_NR = "number";
    public static final String KEY_URL = "url";
    public static final String KEY_USER = "user_id";
    private static final long serialVersionUID = 1L;

    public FloorModel() {

    }

    public FloorModel(String url, int number, String name, String userId, Date created,
            Date modified) {
        setUrl(url);
        setNumber(number);
        setName(name);
        setUserId(userId);
        setCreated(created);
        setModified(modified);
    }

    /**
     * @return the creation date, or null if it was not set
     */
    public Date getCreated() {
        return get(KEY_CREATED);
    }

    /**
     * @return the datastore key String, or null
     */
    public String getKey() {
        return get(KEY_DB_KEY);
    }

    /**
     * @return the blobstore key String, or null
     */
    public String getBlobKey() {
        return get(KEY_BLOB_KEY);
    }

    /**
     * @return the latest modification date, or null if it was not set
     */
    public Date getModified() {
        return get(KEY_MODIFIED);
    }

    /**
     * @return the name, or null if it was not set
     */

    public String getName() {
        return get(KEY_NAME);
    }

    /**
     * @return the floor number, or <code>Integer.MIN_VALUE</code> if it was not set
     */
    public int getNumber() {
        return get(KEY_NR, Integer.MIN_VALUE);
    }

    /**
     * @return the floor image URL, or null if it was not set
     */

    public String getUrl() {
        return get(KEY_URL);
    }

    /**
     * @return the ID of this floor's owner, or null if it was not set
     */
    public String getUserId() {
        return get(KEY_USER);
    }

    /**
     * @param date
     *            the creation date
     */
    public void setCreated(Date date) {
        set(KEY_CREATED, date);
    }

    /**
     * @param key
     *            the datastore key represented as a String
     */
    public void setKey(String key) {
        set(KEY_DB_KEY, key);
    }

    /**
     * @param key
     *            the blobstore key represented as a String
     */
    public void setBlobKey(String key) {
        set(KEY_BLOB_KEY, key);
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
     *            the floor name
     */
    public void setName(String name) {
        set(KEY_NAME, name);
        set("text", name); // "text" is often used as default in GXT
    }

    /**
     * @param number
     *            the floor number
     */
    public void setNumber(int number) {
        set(KEY_NR, number);
    }

    /**
     * @param url
     *            the floor image URL
     */
    public void setUrl(String url) {
        set(KEY_URL, url);
    }

    /**
     * @param userId
     *            the user ID of the floor owner
     */
    public void setUserId(String userId) {
        set(KEY_USER, userId);
    }

    /**
     * @param f
     *            FloorModel to compare this to
     * @return true if the two floors are equal
     */
    public boolean floorEquals(FloorModel f) {
        boolean key = false;
        if (null != f.getKey() && null != this.getKey()) {
            key = (f.getKey().equals(this.getKey()));
        } else if (null == f.getKey() && null == this.getKey()) {
            key = true;
        }
        boolean blobKey = false;
        if (null != f.getBlobKey() && null != this.getBlobKey()) {
            blobKey = (f.getBlobKey().equals(this.getBlobKey()));
        } else if (null == f.getBlobKey() && null == this.getBlobKey()) {
            blobKey = true;
        }
        boolean url = false;
        if (null != f.getUrl() && null != this.getUrl()) {
            url = (f.getUrl().equals(this.getUrl()));
        } else if (null == f.getUrl() && null == this.getUrl()) {
            url = true;
        }
        boolean date = false;
        if (null != f.getCreated() && null != this.getCreated()) {
            date = (f.getCreated().equals(this.getCreated()));
        } else if (null == f.getCreated() && null == this.getCreated()) {
            date = true;
        }
        boolean name = false;
        if (null != f.getName() && null != this.getName()) {
            name = (f.getName().equals(this.getName()));
        } else if (null == f.getName() && null == this.getName()) {
            name = true;
        }
        boolean user = false;
        if (null != f.getUserId() && null != this.getUserId()) {
            user = (f.getUserId().equals(this.getUserId()));
        } else if (null == f.getUserId() && null == this.getUserId()) {
            user = true;
        }
        boolean nr = false;
        if (Integer.MIN_VALUE != f.getNumber() && Integer.MIN_VALUE != this.getNumber()) {
            nr = (f.getNumber() == this.getNumber());
        } else if (Integer.MIN_VALUE == f.getNumber() && Integer.MIN_VALUE == this.getNumber()) {
            nr = true;
        }
        return blobKey && key && url && date && name && nr && user;
    }
}