package nl.sense_os.commonsense.shared;

import java.util.Map;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

public class DeviceModel extends BaseTreeModel {

    private static final long serialVersionUID = 1L;
    public static final String KEY_ID = "id";
    public static final String KEY_TYPE = "type";
    public static final String KEY_UUID = "uuid";

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
        return get(KEY_ID);
    }

    public String getType() {
        return get(KEY_TYPE);
    }

    public String getUuid() {
        return get(KEY_UUID);
    }

    @Override
    public String toString() {
        return get("text", "Device #" + getId());
    }
}
