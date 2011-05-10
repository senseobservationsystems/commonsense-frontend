package nl.sense_os.commonsense.shared;

import java.util.Map;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

public class SensorModel extends BaseTreeModel {

    public static final String DATA_STRUCTURE = "data_structure";
    public static final String DATA_TYPE = "data_type";
    public static final String DATA_TYPE_ID = "data_type_id";
    public static final String DEVICE_TYPE = "device_type";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String DISPLAY_NAME = "display_name";
    public static final String TYPE = "type";
    public static final String PAGER_TYPE = "pager_type";
    public static final String OWNER = "owner";
    public static final String DEVICE = "device";
    public static final String ENVIRONMENT = "environment";
    private static final long serialVersionUID = 1L;

    public SensorModel() {
        super();
    }

    public SensorModel(Map<String, Object> properties) {
        super(properties);
    }

    public SensorModel(TreeModel parent) {
        super(parent);
    }

    public String getDataStructure() {
        return get(DATA_STRUCTURE);
    }

    public String getDataType() {
        return get(DATA_TYPE);
    }

    public String getDataTypeId() {
        return get(DATA_TYPE_ID);
    }

    public DeviceModel getDevice() {
        return get(DEVICE);
    }

    public String getDeviceType() {
        return get(DEVICE_TYPE);
    }

    public String getDisplayName() {
        return get(DISPLAY_NAME);
    }

    public EnvironmentModel getEnvironment() {
        return get(ENVIRONMENT);
    }

    public String getId() {
        return get(ID);
    }

    public String getName() {
        return get(NAME);
    }

    public UserModel getOwner() {
        return get(OWNER);
    }

    public String getPagerType() {
        return get(PAGER_TYPE);
    }

    public String getType() {
        return get(TYPE);
    }

    @Override
    public String toString() {
        return get(DISPLAY_NAME, "Sensor #" + getId());
    }
}
