package nl.sense_os.commonsense.dto;

import com.extjs.gxt.ui.client.data.BaseModel;

public class UserModel extends BaseModel {

    private static final long serialVersionUID = 1L;

    public UserModel() {
    }

    public UserModel(int id, String name, String password) {
        setId(id);
        setName(name);
        setPassword(password);
    }

    public int getId() {
        return get("id", -1);
    }

    public String getName() {
        return get("name");
    }

    public String getPassword() {
        return get("password");
    }

    public UserModel setId(int id) {
        set("id", id);
        return this;
    }

    public UserModel setName(String name) {
        set("name", name);
        return this;
    }

    public UserModel setPassword(String password) {
        set("password", password);
        return this;
    }
}
