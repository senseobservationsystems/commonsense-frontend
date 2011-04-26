package nl.sense_os.commonsense.shared;

import nl.sense_os.commonsense.client.services.BuildingServiceAsync;
import nl.sense_os.commonsense.client.services.GroupsProxyAsync;
import nl.sense_os.commonsense.client.services.SensorsProxyAsync;

public class Constants {

    private Constants() {
        // Private constructor to make sure this class is not instantiated.
    }

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
     * Registry key for the list of services for the current user, stored as @link {@link List
     * <ServiceModel>}
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
    public static final String URL_STATES = URL__BASE + "/states";
    public static final String URL_USERS = URL__BASE + "/users";

    public static final String ICON_LOADING = "gxt/images/gxt/icons/loading.gif";
    public static final String ICON_BUTTON_GO = "gxt/images/gxt/icons/page-next.gif";

    /** Google Maps API key, generated for http://common-sense-test.appspot.com */
    private static final String MAPS_TEST_KEY = "ABQIAAAAcc8ibe_QaK2XBw4Vp-cVyBQYr_M-iqqVQWbBU0ti1KBe5MFjFxQAq9nNCLMy6cXkTX8xOCj9FjzFJA";
    /** Google Maps API key, generated for http://common-sense.appspot.com */
    private static final String MAPS_STABLE_KEY = "ABQIAAAA3D_pshePfBK3EagBrZGggxRw2HbSAHXTbPxYxhYhGrEpeVUi1BS8AbO5bPL1UMzVm1LL2thJx-M_jw";
    public static final String MAPS_API_KEY = MAPS_TEST_KEY;

    public static final String testDataJson = "{\"data\":["
            + "{\"id\":\"20243680\",\"sensor_id\":\"89\",\"value\":\"{\\\"x-axis\\\":-0.14982382953166962,\\\"y-axis\\\":0.46309182047843933,\\\"z-axis\\\":9.847511291503906}\",\"date\":\"1302858147.5\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
            + "{\"id\":\"20245598\",\"sensor_id\":\"89\",\"value\":\"{\\\"x-axis\\\":-0.108962781727314,\\\"y-axis\\\":0.5039528608322144,\\\"z-axis\\\":9.806650161743164}\",\"date\":\"1302859047.68\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
            + "{\"id\":\"20246442\",\"sensor_id\":\"89\",\"value\":\"{\\\"x-axis\\\":-0.19068486988544464,\\\"y-axis\\\":0.46309182047843933,\\\"z-axis\\\":9.765789031982422}\",\"date\":\"1302859498.59\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
            + "{\"id\":\"20247605\",\"sensor_id\":\"89\",\"value\":\"{\\\"x-axis\\\":0,\\\"y-axis\\\":0.46309182047843933,\\\"z-axis\\\":9.806650161743164}\",\"date\":\"1302860007.35\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
            + "{\"id\":\"20249252\",\"sensor_id\":\"89\",\"value\":\"{\\\"x-axis\\\":0,\\\"y-axis\\\":0.5039528608322144,\\\"z-axis\\\":9.806650161743164}\",\"date\":\"1302860907.38\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"}"
            + "],\"total\":5}";

    public static final String testDataString = "{\"data\":["
            + "{\"id\":\"20243680\",\"sensor_id\":\"89\",\"value\":\"hoi1\",\"date\":\"1302858147.5\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
            + "{\"id\":\"20245598\",\"sensor_id\":\"89\",\"value\":\"hoi2\",\"date\":\"1302859047.68\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
            + "{\"id\":\"20246442\",\"sensor_id\":\"89\",\"value\":\"hoi3\",\"date\":\"1302859498.59\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
            + "{\"id\":\"20247605\",\"sensor_id\":\"89\",\"value\":\"hoi4\",\"date\":\"1302860007.35\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
            + "{\"id\":\"20249252\",\"sensor_id\":\"89\",\"value\":\"hoi5\",\"date\":\"1302860907.38\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"}"
            + "],\"total\":5}";

    public static final String testDataFloat = "{\"data\":["
            + "{\"id\":\"20243680\",\"sensor_id\":\"89\",\"value\":\"1\",\"date\":\"1302858147.5\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
            + "{\"id\":\"20245598\",\"sensor_id\":\"89\",\"value\":\"2\",\"date\":\"1302859047.68\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
            + "{\"id\":\"20246442\",\"sensor_id\":\"89\",\"value\":\"3\",\"date\":\"1302859498.59\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
            + "{\"id\":\"20247605\",\"sensor_id\":\"89\",\"value\":\"4\",\"date\":\"1302860007.35\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
            + "{\"id\":\"20249252\",\"sensor_id\":\"89\",\"value\":\"5\",\"date\":\"1302860907.38\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"}"
            + "],\"total\":5}";
}
