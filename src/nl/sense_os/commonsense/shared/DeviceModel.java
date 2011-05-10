package nl.sense_os.commonsense.shared;

import java.util.Map;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

public class DeviceModel extends BaseTreeModel {

    private static final long serialVersionUID = 1L;
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String UUID = "uuid";

    public DeviceModel() {
        super();
    }

    public DeviceModel(Map<String, Object> properties) {
        super(properties);
    }

    public DeviceModel(TreeModel parent) {
        super(parent);
    }

    public String getId() {
        return get(ID);
    }

    public String getType() {
        return get(TYPE);
    }

    public String getUuid() {
        return get(UUID);
    }

    @Override
    public String toString() {
        return get("text", "Device #" + getId());
    }
}
