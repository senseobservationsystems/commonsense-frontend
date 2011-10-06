package nl.sense_os.commonsense.client.common.models;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Model for a sensor. GXT-style bean, used in various GXT components.
 */
public class SensorModel extends BaseTreeModel {

    public static final String DATA_STRUCTURE = "data_structure";
    public static final String DATA_TYPE = "data_type";
    public static final String DESCRIPTION = "device_type";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String DISPLAY_NAME = "display_name";
    public static final String TYPE = "type";
    public static final String PAGER_TYPE = "pager_type";

    public static final String ALIAS = "alias";
    public static final String USERS = "users";
    public static final String AVAIL_SERVICES = "available_services";

    public static final String OWNER = "owner";
    public static final String OWNER_ID = "owner_id";
    public static final String OWNER_MOBILE = "owner_mobile";
    public static final String OWNER_NAME = "owner_name";
    public static final String OWNER_SURNAME = "owner_surname";
    public static final String OWNER_USERNAME = "owner_username";
    public static final String OWNER_EMAIL = "owner_email";

    public static final String DEVICE = "device";
    public static final String DEVICE_ID = "device_id";
    public static final String DEVICE_TYPE = "nested_device_type";
    public static final String DEVICE_UUID = "device_uuid";

    public static final String ENVIRONMENT = "environment";
    public static final String ENVIRONMENT_ID = "environment_id";
    public static final String ENVIRONMENT_NAME = "environment_name";
    public static final String ENVIRONMENT_FLOORS = "environment_floors";
    public static final String ENVIRONMENT_OUTLINE = "environment_outline";
    public static final String ENVIRONMENT_POSITION = "environment_position";

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(SensorModel.class.getName());

    public SensorModel() {
        super();
    }

    public SensorModel(Map<String, Object> properties) {
        super(properties);
    }

    public SensorModel(SensorJso jso) {
        this();
        setId(jso.getId());
        setName(jso.getName());
        setType(jso.getType());
        setDescription(jso.getPhysicalSensor());
        setDataStructure(jso.getDataStructure());
        setDataType(jso.getDataType());
        setDevice(jso.getDevice());
        setDisplayName(jso.getDisplayName());
        setEnvironment(jso.getEnvironment());
        setOwner(jso.getOwner());
        setUsers(jso.getUsers());
        setPagerType(jso.getPagerType());
    }

    public SensorModel(TreeModel parent) {
        super(parent);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SensorModel) {
            final SensorModel sensor = (SensorModel) obj;

            boolean idEqual = getId() == sensor.getId();
            boolean ownerEqual = (getOwner() == null && sensor.getOwner() == null)
                    || (null != getOwner() && getOwner().equals(sensor.getOwner()));
            boolean environmentEqual = (getEnvironment() == null && sensor.getEnvironment() == null)
                    || (null != getEnvironment() && getEnvironment()
                            .equals(sensor.getEnvironment()));
            boolean deviceEqual = (getDevice() == null && sensor.getDevice() == null)
                    || (null != getDevice() && getDevice().equals(sensor.getDevice()));
            boolean parentEqual = (getParent() == null && sensor.getParent() == null)
                    || (null != getParent() && getParent().equals(sensor.getParent()));

            return idEqual && ownerEqual && environmentEqual && deviceEqual && parentEqual;

        } else {
            return super.equals(obj);
        }
    }

    /**
     * @return The ID of the group that can access this sensor, if the current user is not directly
     *         able to access it. If the sensor has no alias, -1 is returned.
     */
    public int getAlias() {
        Object property = get(ALIAS);
        if (property instanceof Integer) {
            return ((Integer) property).intValue();
        } else if (property instanceof String) {
            return Integer.parseInt((String) property);
        } else {
            return -1;
        }
    }

    /**
     * @return A list of services that are available for this sensor.
     */
    public List<ServiceModel> getAvailServices() {
        return get(AVAIL_SERVICES);
    }

    /**
     * @return The data structure of this sensor, if it has a complex data type. For JSON sensors,
     *         this should contain the names of the fields and their types.
     */
    public String getDataStructure() {
        return get(DATA_STRUCTURE);
    }

    /**
     * @return The data type of this sensor. E.g. 'float', 'string or 'json'.
     */
    public String getDataType() {
        return get(DATA_TYPE);
    }

    /**
     * @return The description of this sensor, of any.
     */
    public String getDescription() {
        return get(DESCRIPTION);
    }

    /**
     * @return The device that this sensor is connected to.
     */
    public DeviceModel getDevice() {
        return get(DEVICE);
    }

    /**
     * @return The sensor's display name.
     */
    public String getDisplayName() {
        return get(DISPLAY_NAME);
    }

    /**
     * @return The environment this sensor is connected to (if any).
     */
    public EnvironmentModel getEnvironment() {
        return get(ENVIRONMENT);
    }

    /**
     * @return The sensor's ID.
     */
    public int getId() {
        Object property = get(ID);
        if (property instanceof Integer) {
            return ((Integer) property).intValue();
        } else if (property instanceof String) {
            return Integer.parseInt((String) property);
        } else {
            LOGGER.severe("Missing property: " + ID);
            return -1;
        }
    }

    /**
     * @return The sensor's name.
     */
    public String getName() {
        return get(NAME);
    }

    /**
     * @return The sensor's owner.
     */
    public UserModel getOwner() {
        return get(OWNER);
    }

    /**
     * @return The sensor's pager type.
     */
    public String getPagerType() {
        return get(PAGER_TYPE);
    }

    /**
     * @return The sensor type, e.g. a physical sensor, or state sensor.
     */
    public int getType() {
        Object property = get(TYPE);
        if (property instanceof Integer) {
            return ((Integer) property).intValue();
        } else if (property instanceof String) {
            return Integer.parseInt((String) property);
        } else {
            LOGGER.severe("Missing property: " + TYPE);
            return -1;
        }
    }

    /**
     * @return The list of users that have access to this sensor.
     */
    public List<UserModel> getUsers() {
        return get(USERS);
    }

    public SensorModel setAlias(int alias) {
        set(ALIAS, alias);
        return this;
    }

    public SensorModel setAvailServices(List<ServiceModel> services) {
        set(AVAIL_SERVICES, services);
        return this;
    }

    public SensorModel setDataStructure(String dataStructure) {
        if (null == dataStructure) {
            remove(DATA_STRUCTURE);
        } else {
            set(DATA_STRUCTURE, dataStructure);
        }
        return this;
    }

    public SensorModel setDataType(String dataType) {
        if (null == dataType) {
            remove(DATA_TYPE);
        } else {
            set(DATA_TYPE, dataType);
        }
        return this;
    }

    public SensorModel setDescription(String physicalSensor) {
        if (null == physicalSensor) {
            remove(DESCRIPTION);
        } else {
            set(DESCRIPTION, physicalSensor);
        }
        return this;
    }

    public SensorModel setDevice(DeviceModel device) {
        if (null == device) {
            remove(DEVICE);
            remove(DEVICE_ID);
            remove(DEVICE_TYPE);
            remove(DEVICE_UUID);
        } else {
            set(DEVICE, device);
        }
        return this;
    }

    public SensorModel setDisplayName(String displayName) {
        if (null == displayName) {
            remove(DISPLAY_NAME);
        } else {
            set(DISPLAY_NAME, displayName);
        }
        return this;
    }

    public SensorModel setEnvironment(EnvironmentModel environment) {
        if (null == environment) {
            remove(ENVIRONMENT);
            remove(ENVIRONMENT_FLOORS);
            remove(ENVIRONMENT_ID);
            remove(ENVIRONMENT_NAME);
            remove(ENVIRONMENT_OUTLINE);
            remove(ENVIRONMENT_POSITION);
        } else {
            set(ENVIRONMENT, environment);
        }
        return this;
    }

    public SensorModel setId(int id) {
        set(ID, id);
        return this;
    }

    public SensorModel setName(String name) {
        if (null == name) {
            remove(NAME);
        } else {
            set(NAME, name);
        }
        return this;
    }

    public SensorModel setOwner(UserModel owner) {
        if (null == owner) {
            remove(OWNER);
            remove(OWNER_EMAIL);
            remove(OWNER_ID);
            remove(OWNER_MOBILE);
            remove(OWNER_NAME);
            remove(OWNER_SURNAME);
            remove(OWNER_USERNAME);
        } else {
            set(OWNER, owner);
        }
        return this;
    }

    public SensorModel setPagerType(String pagerType) {
        if (null == pagerType) {
            remove(PAGER_TYPE);
        } else {
            set(PAGER_TYPE, pagerType);
        }
        return this;
    }

    public SensorModel setType(int type) {
        set(TYPE, type);
        return this;
    }

    public SensorModel setUsers(List<UserModel> users) {
        if (null == users) {
            remove(USERS);
        } else {
            set(USERS, users);
        }
        return this;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
