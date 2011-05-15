package nl.sense_os.commonsense.shared;

import nl.sense_os.commonsense.client.rpc.BuildingServiceAsync;
import nl.sense_os.commonsense.client.rpc.GroupsProxyAsync;
import nl.sense_os.commonsense.client.rpc.SensorsProxyAsync;

public class Constants {

    private Constants() {
        // Private constructor to make sure this class is not instantiated.
    }

    public static final boolean TEST_MODE = true;
    public static final boolean TED_MODE = TEST_MODE && false;

    /**
     * Registry key for the building service, stored as @link {@link BuildingServiceAsync}.
     */
    public static final String REG_BUILDING_SVC = "BuildingService";

    /**
     * Registry key for the groups proxy, stored as @link {@link GroupsProxyAsync}.
     */
    public static final String REG_GROUPS_PROXY = "GroupsService";

    /**
     * Registry key for the session ID, stored as String
     */
    public static final String REG_SESSION_ID = "SessionId";

    /**
     * Registry key for the sensors proxy, stored as @link {@link SensorsProxyAsync}.
     */
    public static final String REG_SENSORS_PROXY = "TagsService";

    /**
     * Registry key for the current User
     */
    public static final String REG_USER = "User";

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
     * Registry key for the list of all devices for the current user
     */
    public static final String REG_DEVICE_LIST = "DevicesList";

    /**
     * Registry key for the list of all environments for the current user
     */
    public static final String REG_ENVIRONMENT_LIST = "EnvironmentList";

    /**
     * Registry key for the list of groups for the current user
     */
    public static final String REG_GROUPS = "Groups";

    /**
     * Registry key for the list of group sensors for the current user
     * 
     * @deprecated Use {@link #REG_SENSOR_LIST} instead.
     */
    @Deprecated
    public static final String REG_GROUP_SENSORS = "GroupSensors";

    /**
     * Registry key for the list of services for the current user
     */
    public static final String REG_SERVICES = "Services";

    private static final String BASE_URL_STABLE = "http://api.sense-os.nl";
    private static final String BASE_URL_TED = "http://217.77.159.221/restful/api";
    public static final String URL__BASE = TED_MODE ? BASE_URL_TED : BASE_URL_STABLE;
    public static final String URL_DATA = URL__BASE + "/sensors/<id>/data";
    public static final String URL_DEVICE_SENSORS = URL__BASE + "/devices/<id>/sensors";
    public static final String URL_DEVICES = URL__BASE + "/devices";
    public static final String URL_GROUPS = URL__BASE + "/groups";
    public static final String URL_LOGIN = URL__BASE + "/login";
    public static final String URL_LOGOUT = URL__BASE + "/logout";
    public static final String URL_SENSORS = URL__BASE + "/sensors";
    public static final String URL_ENVIRONMENTS = URL__BASE + "/environments";
    public static final String URL_STATES = URL__BASE + "/states";
    public static final String URL_USERS = URL__BASE + "/users";

    /** Google Maps API key, generated for http://common-sense-test.appspot.com */
    private static final String MAPS_KEY_TEST = "ABQIAAAAcc8ibe_QaK2XBw4Vp-cVyBQYr_M-iqqVQWbBU0ti1KBe5MFjFxQAq9nNCLMy6cXkTX8xOCj9FjzFJA";
    /** Google Maps API key, generated for http://common-sense.appspot.com */
    private static final String MAPS_KEY_STABLE = "ABQIAAAA3D_pshePfBK3EagBrZGggxRw2HbSAHXTbPxYxhYhGrEpeVUi1BS8AbO5bPL1UMzVm1LL2thJx-M_jw";
    public static final String MAPS_KEY = TEST_MODE ? MAPS_KEY_TEST : MAPS_KEY_STABLE;
}
