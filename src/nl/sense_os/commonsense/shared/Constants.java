package nl.sense_os.commonsense.shared;

public class Constants {

    /**
     * Registry key for the building service
     */
    public static final String REG_BUILDING_SVC = "BuildingService";

    /**
     * Registry key for the groups service
     */
    public static final String REG_GROUPS_SVC = "GroupsService";

    /**
     * Registry key for the session id
     */
    public static final String REG_SESSION_ID = "SessionId";

    /**
     * Registry key for the sensor/device-tag service
     */
    public static final String REG_TAGS_SVC = "TagsService";

    /**
     * Registry key for the current User
     */
    public static final String REG_USER = "User";

    /**
     * Registry key for the list of sensors for the current user
     */
    public static final String REG_MY_SENSORS = "MySensors";

    /**
     * Registry key for the list of groups for the current user
     */
    public static final String REG_GROUPS = "Groups";

    /**
     * Registry key for the list of group sensors for the current user
     */
    public static final String REG_GROUP_SENSORS = "GroupSensors";

    /**
     * Registry key for the list of services for the current user
     */
    public static final String REG_SERVICES = "Services";

    public static final String URL__BASE = "http://api.sense-os.nl";
    public static final String URL_DATA = URL__BASE + "/sensors/<id>/data";
    public static final String URL_DEVICE_SENSORS = URL__BASE + "/devices/<id>/sensors";
    public static final String URL_DEVICES = URL__BASE + "/devices";
    public static final String URL_GROUPS = URL__BASE + "/groups";
    public static final String URL_LOGIN = URL__BASE + "/login";
    public static final String URL_LOGOUT = URL__BASE + "/logout";
    public static final String URL_SENSORS = URL__BASE + "/sensors";
    public static final String URL_USERS = URL__BASE + "/users";

    public static final String ICON_LOADING = "gxt/images/gxt/icons/loading.gif";
    public static final String ICON_BUTTON_GO = "gxt/images/gxt/icons/page-next.gif";

    private static final String MAPS_TEST_KEY = "ABQIAAAAcc8ibe_QaK2XBw4Vp-cVyBQYr_M-iqqVQWbBU0ti1KBe5MFjFxQAq9nNCLMy6cXkTX8xOCj9FjzFJA";
    public static final String MAPS_API_KEY = MAPS_TEST_KEY;
}
