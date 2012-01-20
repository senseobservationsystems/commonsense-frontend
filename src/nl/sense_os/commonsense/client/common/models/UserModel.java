package nl.sense_os.commonsense.client.common.models;

import java.util.Map;
import java.util.logging.Logger;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

/**
 * Model for a user. GXT-style bean, used in various GXT components.
 */
public class UserModel extends BaseTreeModel {

    public static final String ID = "id";
    public static final String EMAIL = "email";
    public static final String MOBILE = "mobile";
    public static final String NAME = "name";
    public static final String SURNAME = "surname";
    public static final String USERNAME = "username";
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(UserModel.class.getName());

    public UserModel() {
        super();
    }

    public UserModel(Map<String, Object> properties) {
        super(properties);
    }

    public UserModel(TreeModel parent) {
        super(parent);
    }

    public UserModel(UserJso jso) {
        this();
        setId(jso.getId());
        setEmail(jso.getEmail());
        setMobile(jso.getMobile());
        setName(jso.getName());
        setSurname(jso.getSurname());
        setUsername(jso.getUsername());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserModel) {
            UserModel user = ((UserModel) obj);
            if (null == user.getParent()) {
                return getId() == ((UserModel) obj).getId();
            } else {
                if (user.getParent() != this.getParent()) {
                    return false;
                } else {
                    return getId() == ((UserModel) obj).getId();
                }
            }
        } else {
            return super.equals(obj);
        }
    }

    public String getEmail() {
        return get(EMAIL);
    }

    public int getId() {
        Object property = get(ID);
        if (property instanceof Integer) {
            return ((Integer) property).intValue();
        } else if (property instanceof String) {
            return Integer.parseInt((String) property);
        } else {
            LOGGER.severe("Missing property: " + ID);
            return -1;
        }
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

    public UserModel setEmail(String email) {
        set(EMAIL, email);
        return this;
    }

    public UserModel setId(int id) {
        set(ID, id);
        return this;
    }

    public UserModel setMobile(String mobile) {
        set(MOBILE, mobile);
        return this;
    }

    public UserModel setName(String name) {
        set(NAME, name);
        return this;
    }

    public UserModel setSurname(String surname) {
        set(SURNAME, surname);
        return this;
    }

    public UserModel setUsername(String username) {
        set(USERNAME, username);
        return this;
    }

    @Override
    public String toString() {
        return getUsername();
    }
}
