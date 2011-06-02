package nl.sense_os.commonsense.client.common.models;

import java.util.Map;

import com.extjs.gxt.ui.client.data.TreeModel;

/**
 * Model for a group (essentially a special user). GXT-style bean, used in various GXT components.
 */
public class GroupModel extends UserModel {

    private static final long serialVersionUID = 1L;

    public GroupModel() {
        super();
    }

    public GroupModel(Map<String, Object> properties) {
        super(properties);
    }

    public GroupModel(TreeModel parent) {
        super(parent);
    }

    public GroupModel(UserJso jso) {
        super(jso);
    }
}
