package nl.sense_os.commonsense.shared;

import com.extjs.gxt.ui.client.data.BaseModel;

public class UserModel extends BaseModel {

    public static final String KEY_EMAIL = "email";
    public static final String KEY_ID = "id";
    public static final String KEY_MOBILE = "mobile";
    public static final String KEY_NAME = "name";
    public static final String KEY_SURNAME = "surname";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_UUID = "uuid";
    private static final long serialVersionUID = 1L;

    public UserModel() {

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

    public String getUuid() {
        return get(KEY_UUID);
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

    private UserModel setUuid(String uuid) {
        set(KEY_UUID, uuid);
        return this;
    }

    @Override
    public String toString() {
        return get("text", super.toString());
    }
}
