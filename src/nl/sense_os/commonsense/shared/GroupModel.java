package nl.sense_os.commonsense.shared;

import java.util.Map;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

public class GroupModel extends BaseTreeModel {

    private static final long serialVersionUID = 1L;
    public static final String KEY_ID = "id";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_NAME = "name";
    public static final String KEY_UUID = "UUID";

    public GroupModel() {
        super();
    }

    public GroupModel(Map<String, Object> properties) {
        super(properties);
    }

    public GroupModel(TreeModel parent) {
        super(parent);
    }
}
