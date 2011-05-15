package nl.sense_os.commonsense.shared.constants;

import nl.sense_os.commonsense.client.rpc.BuildingServiceAsync;
import nl.sense_os.commonsense.client.rpc.GroupsProxyAsync;
import nl.sense_os.commonsense.client.rpc.SensorsProxyAsync;

public class Constants {

    public static final boolean TEST_MODE = true;

    public static final boolean TED_MODE = TEST_MODE && false;

    /**
     * Registry key for the building service, stored as @link {@link BuildingServiceAsync}.
     */
    public static final String REG_BUILDING_SVC = "BuildingService";

    /**
     * Registry key for the list of all devices for the current user
     */
    public static final String REG_DEVICE_LIST = "DevicesList";
    /**
     * Registry key for the list of all environments for the current user
     */
    public static final String REG_ENVIRONMENT_LIST = "EnvironmentList";

    /**
     * Registry key for the list of group sensors for the current user
     * 
     * @deprecated Use {@link #REG_SENSOR_LIST} instead.
     */
    @Deprecated
    public static final String REG_GROUP_SENSORS = "GroupSensors";

    /**
     * Registry key for the list of groups for the current user
     */
    public static final String REG_GROUPS = "Groups";

    /**
     * Registry key for the groups proxy, stored as @link {@link GroupsProxyAsync}.
     */
    public static final String REG_GROUPS_PROXY = "GroupsService";

    /**
     * Registry key for the tree of sensors for the current user
     * 
     * @deprecated Use {@link #REG_SENSOR_LIST} instead.
     */
    @Deprecated
    public static final String REG_MY_SENSORS_TREE = "MySensors";

    /**
     * Registry key for the list of all sensors for the current user
     */
    public static final String REG_SENSOR_LIST = "SensorsList";

    /**
     * Registry key for the sensors proxy, stored as @link {@link SensorsProxyAsync}.
     */
    public static final String REG_SENSORS_PROXY = "TagsService";

    /**
     * Registry key for the list of services for the current user
     */
    public static final String REG_SERVICES = "Services";

    /**
     * Registry key for the session ID, stored as String
     */
    public static final String REG_SESSION_ID = "SessionId";

    /**
     * Registry key for the current User
     */
    public static final String REG_USER = "User";

    private Constants() {
        // Private constructor to make sure this class is not instantiated.
    }

}
