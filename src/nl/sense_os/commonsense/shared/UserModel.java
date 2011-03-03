package nl.sense_os.commonsense.shared;

import java.util.Map;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

public class UserModel extends BaseTreeModel {

    public static final String KEY_EMAIL = "email";
    public static final String KEY_ID = "id";
    public static final String KEY_MOBILE = "mobile";
    public static final String KEY_NAME = "name";
    public static final String KEY_SURNAME = "surname";
    public static final String KEY_USERNAME = "username";
    private static final long serialVersionUID = 1L;
    private static final String KEY_UUID = "uuid";

    public UserModel() {
        super();
    }

    public UserModel(Map<String, Object> properties) {
        super(properties);
    }

    public UserModel(TreeModel parent) {
        super(parent);
    }

    public UserModel(int id, String username, String email, String name, String surname,
            String mobile, String uuid) {
        setId(id);
        setUsername(username);
        setName(name);
        setSurname(surname);
        setEmail(email);
        setMobile(mobile);
        setUuid(uuid);
    }

    private void setUuid(String uuid) {
        set(KEY_UUID, uuid);
    }

    public String getEmail() {
        return get(KEY_EMAIL);
    }

    public int getId() {
        return get(KEY_ID, -1);
    }

    public String getMobile() {
        return get(KEY_MOBILE);
    }

    public String getName() {
        return get(KEY_NAME);
    }

    public String getSurname() {
        return get(KEY_SURNAME);
    }

    public String getUsername() {
        return get(KEY_USERNAME);
    }

    private UserModel setEmail(String email) {
        set(KEY_EMAIL, email);
        return this;

    }

    public UserModel setId(int id) {
        set(KEY_ID, id);
        return this;
    }

    private UserModel setMobile(String mobile) {
        set(KEY_MOBILE, mobile);
        return this;
    }

    public UserModel setName(String name) {
        set(KEY_NAME, name);
        return this;
    }

    private UserModel setSurname(String surname) {
        set(KEY_SURNAME, surname);
        return this;
    }

    private UserModel setUsername(String username) {
        set(KEY_USERNAME, username);
        set("text", username);
        return this;
    }

    @Override
    public String toString() {
        return get("text", super.toString());
    }

    public String getUuid() {
        return get(KEY_UUID);
    }
}
