package nl.sense_os.commonsense.shared.models;

import java.util.Map;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

public class UserModel extends BaseTreeModel {

    public static final String ID = "id";
    public static final String EMAIL = "email";
    public static final String MOBILE = "mobile";
    public static final String NAME = "name";
    public static final String SURNAME = "surname";
    public static final String USERNAME = "username";
    private static final String UUID = "uuid";
    private static final long serialVersionUID = 1L;

    public UserModel() {
        super();
    }

    public UserModel(Map<String, Object> properties) {
        super(properties);
    }

    public UserModel(String id, String username, String email, String name, String surname,
            String mobile, String uuid) {
        setId(id);
        setUsername(username);
        setName(name);
        setSurname(surname);
        setEmail(email);
        setMobile(mobile);
        setUuid(uuid);
    }

    public UserModel(TreeModel parent) {
        super(parent);
    }

    public String getEmail() {
        return get(EMAIL);
    }

    public String getId() {
        return get(ID);
    }

    public String getMobile() {
        return get(MOBILE);
    }

    public String getName() {
        return get(NAME);
    }

    public String getSurname() {
        return get(SURNAME);
    }

    public String getUsername() {
        return get(USERNAME);
    }

    public String getUuid() {
        return get(UUID);
    }

    private UserModel setEmail(String email) {
        set(EMAIL, email);
        return this;

    }

    public UserModel setId(String id) {
        set(ID, id);
        return this;
    }

    private UserModel setMobile(String mobile) {
        set(MOBILE, mobile);
        return this;
    }

    public UserModel setName(String name) {
        set(NAME, name);
        return this;
    }

    private UserModel setSurname(String surname) {
        set(SURNAME, surname);
        return this;
    }

    private UserModel setUsername(String username) {
        set(USERNAME, username);
        set("text", username);
        return this;
    }

    private void setUuid(String uuid) {
        set(UUID, uuid);
    }

    @Override
    public String toString() {
        return get(USERNAME, "User #" + getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserModel) {
            return this.getId().equals(((UserModel) obj).getId());
        } else {
            return super.equals(obj);
        }
    }
}
