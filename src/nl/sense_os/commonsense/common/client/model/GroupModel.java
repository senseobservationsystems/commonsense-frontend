package nl.sense_os.commonsense.common.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class GroupModel extends UserModel {

    private static final long serialVersionUID = 1L;

    public static final String ACCESS_PASSWORD = "access_password";
    public static final String DESCRIPTION = "description";
    public static final String EMAIL = "email";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String OPT_SENSORS = "optional_sensors";
    public static final String REQ_SENSORS = "required_sensors";
    public static final String HAS_ACCESS_PW = "has_access_password";
    public static final String PASSWORD = "password";
    public static final String ANONYMOUS = "anonymous";
    public static final String HIDDEN = "hidden";
    public static final String PUBLIC = "public";
    public static final String SHOW_EMAIL_REQ = "required_show_email";
    public static final String SHOW_ID_REQ = "required_show_id";
    public static final String SHOW_FIRST_NAME_REQ = "required_show_first_name";
    public static final String SHOW_SURNAME_REQ = "required_show_surname";
    public static final String SHOW_PHONE_REQ = "required_show_phone_number";
    public static final String SHOW_USERNAME_REQ = "required_show_username";
    public static final String ALLOW_LIST_USERS = "default_list_users";
    public static final String ALLOW_ADD_USERS = "default_add_users";
    public static final String ALLOW_REMOVE_USERS = "default_remove_users";
    public static final String ALLOW_LIST_SENSORS = "default_list_sensors";
    public static final String ALLOW_ADD_SENSORS = "default_add_sensors";
    public static final String ALLOW_REMOVE_SENSORS = "default_remove_sensors";

    public GroupModel() {
        super();
    }

    public GroupModel(GroupJso jso) {
        setDescription(jso.getDescription());
        setEmail(jso.getEmail());
        setId(jso.getId());
        setName(jso.getName());
        setOptSensors(jso.getOptSensors());
        setReqSensors(jso.getReqSensors());
        setHasAccessPw(jso.hasAccessPassword());
        setAnonymous(jso.isAnonymous());
        setHidden(jso.isHidden());
        setPublic(jso.isPublic());
        setShowEmailReq(jso.isShowEmailReq());
        setShowIdReq(jso.isShowIdReq());
        setShowFirstNameReq(jso.isShowFirstNameReq());
        setShowPhoneReq(jso.isShowPhoneReq());
        setShowSurnameReq(jso.isShowSurnameReq());
        setShowUsernameReq(jso.isShowUsernameReq());
        setAllowAddSensors(jso.isAllowCreateSensors());
        setAllowAddUsers(jso.isAllowCreateUsers());
        setAllowListSensors(jso.isAllowReadSensors());
        setAllowListUsers(jso.isAllowReadUsers());
        setAllowRemoveSensors(jso.isAllowDeleteSensors());
        setAllowRemoveUsers(jso.isAllowDeleteUsers());
    }

    public GroupModel(Map<String, Object> properties) {
        super(properties);
    }

    public GroupModel(TreeModel parent) {
        super(parent);
    }

    public String getAccessPassword() {
        return get(ACCESS_PASSWORD);
    }

    public String getDescription() {
        return get(DESCRIPTION);
    }

    public String getEmail() {
        return get(EMAIL);
    }

    public int getId() {
        return get(ID, -1);
    }

    public String getName() {
        return get(NAME);
    };

    public List<String> getOptSensors() {
        return get(OPT_SENSORS, new ArrayList<String>());
    }

    public String getPassword() {
        return get(PASSWORD);
    };

    public List<String> getReqSensors() {
        return get(REQ_SENSORS, new ArrayList<String>());
    }

    public Boolean hasAccessPassword() {
        return get(HAS_ACCESS_PW);
    };

    public Boolean isAllowAddSensors() {
        return get(ALLOW_ADD_SENSORS);
    }

    public Boolean isAllowAddUsers() {
        return get(ALLOW_ADD_USERS);
    };

    public Boolean isAllowListSensors() {
        return get(ALLOW_LIST_SENSORS);
    }

    public Boolean isAllowListUsers() {
        return get(ALLOW_LIST_USERS);
    };

    public Boolean isAllowRemoveSensors() {
        return get(ALLOW_REMOVE_SENSORS);
    }

    public Boolean isAllowRemoveUsers() {
        return get(ALLOW_REMOVE_USERS);
    };

    public Boolean isAnonymous() {
        return get(ANONYMOUS);
    }

    public Boolean isHidden() {
        return get(HIDDEN);
    };

    public Boolean isPublic() {
        return get(PUBLIC);
    };

    public Boolean isShowEmailReq() {
        return get(SHOW_EMAIL_REQ);
    }

    public Boolean isShowFirstNameReq() {
        return get(SHOW_FIRST_NAME_REQ);
    }

    public Boolean isShowIdReq() {
        return get(SHOW_ID_REQ);
    }

    public Boolean isShowPhoneReq() {
        return get(SHOW_PHONE_REQ);
    }

    public Boolean isShowSurnameReq() {
        return get(SHOW_SURNAME_REQ);
    }

    public Boolean isShowUsernameReq() {
        return get(SHOW_USERNAME_REQ);
    }

    public void setAccessPassword(String password) {
        set(ACCESS_PASSWORD, password);
    }

    public void setAllowAddSensors(boolean b) {
        set(ALLOW_ADD_SENSORS, b);
    };

    public void setAllowAddUsers(boolean b) {
        set(ALLOW_ADD_USERS, b);
    }

    public void setAllowListSensors(boolean b) {
        set(ALLOW_LIST_SENSORS, b);
    };

    public void setAllowListUsers(boolean b) {
        set(ALLOW_LIST_USERS, b);
    }

    public void setAllowRemoveSensors(boolean b) {
        set(ALLOW_REMOVE_SENSORS, b);
    };

    public void setAllowRemoveUsers(boolean b) {
        set(ALLOW_REMOVE_USERS, b);
    }

    public void setAnonymous(boolean anonymous) {
        set(ANONYMOUS, anonymous);
    };

    public void setDescription(String description) {
        set(DESCRIPTION, description);
    }

    public GroupModel setEmail(String email) {
        set(EMAIL, email);
        return this;
    }

    public void setHasAccessPw(boolean hasAccessPw) {
        set(HAS_ACCESS_PW, hasAccessPw);
    };

    public void setHidden(boolean hidden) {
        set(HIDDEN, hidden);
    }

    public GroupModel setId(int id) {
        set(ID, id);
        return this;
    };

    public GroupModel setName(String name) {
        set(NAME, name);
        return this;
    }

    public void setOptSensors(List<String> sensors) {
        set(OPT_SENSORS, sensors);
    };

    public void setPassword(String password) {
        set(PASSWORD, password);
    }

    public void setPublic(boolean p) {
        set(PUBLIC, p);
    };

    public void setReqSensors(List<String> sensors) {
        set(REQ_SENSORS, sensors);
    };

    public void setShowEmailReq(boolean b) {
        set(SHOW_EMAIL_REQ, b);
    }

    public void setShowFirstNameReq(boolean b) {
        set(SHOW_FIRST_NAME_REQ, b);
    }

    public void setShowIdReq(boolean b) {
        set(SHOW_ID_REQ, b);
    }

    public void setShowPhoneReq(boolean b) {
        set(SHOW_PHONE_REQ, b);
    }

    public void setShowSurnameReq(boolean b) {
        set(SHOW_SURNAME_REQ, b);
    }

    public void setShowUsernameReq(boolean b) {
        set(SHOW_USERNAME_REQ, b);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        if (-1 != getId()) {
            json.put(ID, new JSONNumber(getId()));
        }
        if (null != getName()) {
            json.put(NAME, new JSONString(getName()));
        }
        if (null != getEmail()) {
            json.put(EMAIL, new JSONString(getEmail()));
        }
        if (null != getUsername()) {
            json.put(USERNAME, new JSONString(getUsername()));
        }
        if (null != getPassword()) {
            json.put(PASSWORD, new JSONString(getPassword()));
        }
        if (null != getMobile()) {
            json.put(MOBILE, new JSONString(getMobile()));
        }
        if (null != getDescription()) {
            json.put(DESCRIPTION, new JSONString(getDescription()));
        }
        if (null != isPublic()) {
            json.put(PUBLIC, JSONBoolean.getInstance(isPublic()));
        }
        if (null != isHidden()) {
            json.put(HIDDEN, JSONBoolean.getInstance(isHidden()));
        }
        if (null != isAnonymous()) {
            json.put(ANONYMOUS, JSONBoolean.getInstance(isAnonymous()));
        }
        if (getAccessPassword() != null) {
            json.put(ACCESS_PASSWORD, new JSONString(getAccessPassword()));
        }
        if (getReqSensors().size() > 0) {
            JSONArray reqSensors = new JSONArray();
            for (String req : getReqSensors()) {
                reqSensors.set(reqSensors.size(), new JSONString(req));
            }
            json.put(REQ_SENSORS, reqSensors);
        }
        if (getOptSensors().size() > 0) {
            JSONArray optSensors = new JSONArray();
            for (String opt : getReqSensors()) {
                optSensors.set(optSensors.size(), new JSONString(opt));
            }
            json.put(OPT_SENSORS, optSensors);
        }
        if (null != isAllowListUsers()) {
            json.put(ALLOW_LIST_USERS, JSONBoolean.getInstance(isAllowListUsers()));
        }
        if (null != isAllowAddUsers()) {
            json.put(ALLOW_ADD_USERS, JSONBoolean.getInstance(isAllowAddUsers()));
        }
        if (null != isAllowRemoveUsers()) {
            json.put(ALLOW_REMOVE_USERS, JSONBoolean.getInstance(isAllowRemoveUsers()));
        }
        if (null != isAllowAddSensors()) {
            json.put(ALLOW_LIST_SENSORS, JSONBoolean.getInstance(isAllowListSensors()));
        }
        if (null != isAllowListSensors()) {
            json.put(ALLOW_ADD_SENSORS, JSONBoolean.getInstance(isAllowAddSensors()));
        }
        if (null != isAllowRemoveSensors()) {
            json.put(ALLOW_REMOVE_SENSORS, JSONBoolean.getInstance(isAllowRemoveSensors()));
        }
        if (null != isShowFirstNameReq()) {
            json.put(SHOW_FIRST_NAME_REQ, JSONBoolean.getInstance(isShowFirstNameReq()));
        }
        if (null != isShowSurnameReq()) {
            json.put(SHOW_SURNAME_REQ, JSONBoolean.getInstance(isShowSurnameReq()));
        }
        if (null != isShowEmailReq()) {
            json.put(SHOW_EMAIL_REQ, JSONBoolean.getInstance(isShowEmailReq()));
        }
        if (null != isShowUsernameReq()) {
            json.put(SHOW_USERNAME_REQ, JSONBoolean.getInstance(isShowUsernameReq()));
        }
        if (null != isShowIdReq()) {
            json.put(SHOW_ID_REQ, JSONBoolean.getInstance(isShowIdReq()));
        }

        return json;
    }
}
