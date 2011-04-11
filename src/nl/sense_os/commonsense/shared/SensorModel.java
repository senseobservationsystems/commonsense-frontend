package nl.sense_os.commonsense.shared;

import java.util.Map;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

public class SensorModel extends BaseTreeModel {

    private static final long serialVersionUID = 1L;

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String DATA_TYPE_ID = "data_type_id";
    public static final String DEVICE_TYPE = "device_type";
    public static final String PAGER_TYPE = "pager_type";
    public static final String DATA_TYPE = "data_type";
    public static final String DATA_STRUCTURE = "data_structure";
    public static final String OWNER = "owner";
    public static final String USERS = "users";

    public static final String DEVICE_DEVTYPE = "device_device_type";
    public static final String DEVICE_ID = "device_device_uuid";

    public SensorModel() {
        super();
    }

    public SensorModel(Map<String, Object> properties) {
        super(properties);
    }

    public SensorModel(TreeModel parent) {
        super(parent);
    }
}
