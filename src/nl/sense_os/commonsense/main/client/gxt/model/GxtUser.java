package nl.sense_os.commonsense.main.client.gxt.model;

import java.util.Map;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.model.User;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

/**
 * Model for a user. GXT-style bean, used in various GXT components.
 */
public class GxtUser extends BaseTreeModel {

    public static final String ID = "id";
    public static final String EMAIL = "email";
    public static final String MOBILE = "mobile";
    public static final String NAME = "name";
    public static final String SURNAME = "surname";
    public static final String USERNAME = "username";
    public static final String UUID = "uuid";
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(GxtUser.class.getName());

    public GxtUser() {
        super();
    }

    public GxtUser(Map<String, Object> properties) {
        super(properties);
    }

    public GxtUser(TreeModel parent) {
        super(parent);
    }

    public GxtUser(User jso) {
        this();
        setId(jso.getId());
        setEmail(jso.getEmail());
        setMobile(jso.getMobile());
        setName(jso.getName());
        setSurname(jso.getSurname());
        setUsername(jso.getUsername());
        setUuid(jso.getUuid());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GxtUser) {
            GxtUser user = ((GxtUser) obj);
            if (null == user.getParent()) {
                return getId() == ((GxtUser) obj).getId();
            } else {
                if (user.getParent() != this.getParent()) {
                    return false;
                } else {
                    return getId() == ((GxtUser) obj).getId();
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

    public String getUuid() {
        return get(UUID);
    }

    public GxtUser setEmail(String email) {
        set(EMAIL, email);
        return this;
    }

    public GxtUser setId(int id) {
        set(ID, id);
        return this;
    }

    public GxtUser setMobile(String mobile) {
        set(MOBILE, mobile);
        return this;
    }

    public GxtUser setName(String name) {
        set(NAME, name);
        return this;
    }

    public GxtUser setSurname(String surname) {
        set(SURNAME, surname);
        return this;
    }

    public GxtUser setUsername(String username) {
        set(USERNAME, username);
        return this;
    }

    public GxtUser setUuid(String uuid) {
        set(UUID, uuid);
        return this;
    }

    @Override
    public String toString() {
        return getUsername();
    }
}
