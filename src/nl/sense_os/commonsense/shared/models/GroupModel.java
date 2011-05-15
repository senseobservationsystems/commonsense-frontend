package nl.sense_os.commonsense.shared.models;

import java.util.Map;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

public class GroupModel extends BaseTreeModel {

    private static final long serialVersionUID = 1L;
    public static final String ID = "id";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";
    public static final String NAME = "name";

    public GroupModel() {
        super();
    }

    public GroupModel(Map<String, Object> properties) {
        super(properties);
    }

    public GroupModel(TreeModel parent) {
        super(parent);
    }

    public String getId() {
        return get(ID);
    }

    public String getEmail() {
        return get(EMAIL);
    }

    public String getUsername() {
        return get(USERNAME);
    }

    public String getName() {
        return get(NAME);
    }

    @Override
    public String toString() {
        return get("text", "Group #" + getId());
    }
}
