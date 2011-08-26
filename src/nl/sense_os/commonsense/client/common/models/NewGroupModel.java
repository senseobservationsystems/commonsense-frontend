package nl.sense_os.commonsense.client.common.models;

import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

public class NewGroupModel extends BaseTreeModel {

    private static final long serialVersionUID = 1L;

    public static final String DESCRIPTION = "description";
    public static final String EMAIL = "email";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String OPT_SENSORS = "optional_sensors";
    public static final String REQ_SENSORS = "required_sensors";
    public static final String HAS_ACCESS_PW = "has_access_password";
    public static final String ANONYMOUS = "anonymous";
    public static final String HIDDEN = "hidden";
    public static final String PUBLIC = "public";
    public static final String SHOW_EMAIL_REQ = "required_show_email";
    public static final String SHOW_ID_REQ = "required_show_id";
    public static final String SHOW_FIRST_NAME_REQ = "required_show_first_name";
    public static final String SHOW_SURNAME_REQ = "required_show_surname";
    public static final String SHOW_PHONE_REQ = "required_show_phone_number";

    public NewGroupModel() {
        super();
    }

    public NewGroupModel(Map<String, Object> properties) {
        super(properties);
    }

    public NewGroupModel(TreeModel parent) {
        super(parent);
    }

    public NewGroupModel(GroupJso jso) {

    }

    public void setDescription(String description) {
        set(DESCRIPTION, description);
    }

    public String getDescription() {
        return get(DESCRIPTION);
    };

    public void setEmail(String email) {
        set(EMAIL, email);
    }

    public String getEmail() {
        return get(EMAIL);
    };

    public void setId(int id) {
        set(ID, id);
    }

    public int getId() {
        return get(ID);
    };

    public void setName(String name) {
        set(NAME, name);
    }

    public String getName() {
        return get(NAME);
    };

    public void setOptSensors(List<String> sensors) {
        set(OPT_SENSORS, sensors);
    }

    public List<String> getOptSensors() {
        return get(OPT_SENSORS);
    };

    public void setReqSensors(List<String> sensors) {
        set(REQ_SENSORS, sensors);
    }

    public List<String> getReqSensors() {
        return get(REQ_SENSORS);
    };

    public void setHasAccessPw(boolean hasAccessPw) {
        set(HAS_ACCESS_PW, hasAccessPw);
    }

    public boolean hasAccessPassword() {
        return get(HAS_ACCESS_PW);
    };

    public void setAnonymous(boolean anonymous) {
        set(ANONYMOUS, anonymous);
    }

    public boolean isAnonymous() {
        return get(ANONYMOUS);
    };

    public void setHidden(boolean hidden) {
        set(HIDDEN, hidden);
    }

    public boolean isHidden() {
        return get(HIDDEN);
    };

    public void setPublic(boolean p) {
        set(PUBLIC, p);
    }

    public boolean isPublic() {
        return get(PUBLIC);
    };

    public void setShowEmailReq(boolean b) {
        set(SHOW_EMAIL_REQ, b);
    }

    public boolean isShowEmailReq() {
        return get(SHOW_EMAIL_REQ);
    };

    public void setShowFirstNameReq(boolean b) {
        set(SHOW_FIRST_NAME_REQ, b);
    }

    public boolean isShowFirstNameReq() {
        return get(SHOW_FIRST_NAME_REQ);
    };

    public void setShowIdReq(boolean b) {
        set(SHOW_ID_REQ, b);
    }

    public boolean isShowIdReq() {
        return get(SHOW_ID_REQ);
    };

    public void setShowPhoneReq(boolean b) {
        set(SHOW_PHONE_REQ, b);
    }

    public boolean isShowPhoneReq() {
        return get(SHOW_PHONE_REQ);
    };

    public void setShowSurnameReq(boolean b) {
        set(SHOW_SURNAME_REQ, b);
    }

    public boolean isShowSurnameReq() {
        return get(SHOW_SURNAME_REQ);
    };
}
