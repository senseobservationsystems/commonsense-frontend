package nl.sense_os.commonsense.shared.constants;

public class Urls {

    private static final String _STABLE_BASE = "http://api.sense-os.nl";
    private static final String _TED_BASE = "http://217.77.159.221/restful/api";
    public static final String BASE_URL = Constants.TED_MODE ? _TED_BASE : _STABLE_BASE;
    public static final String DATA = BASE_URL + "/sensors/<id>/data";
    public static final String DEVICE_SENSORS = BASE_URL + "/devices/<id>/sensors";
    public static final String DEVICES = BASE_URL + "/devices";
    public static final String ENVIRONMENTS = BASE_URL + "/environments";
    public static final String GROUPS = BASE_URL + "/groups";
    public static final String LOGIN = BASE_URL + "/login";
    public static final String LOGOUT = BASE_URL + "/logout";
    public static final String SENSORS = BASE_URL + "/sensors";
    public static final String STATES = BASE_URL + "/states";
    public static final String USERS = BASE_URL + "/users";

    private Urls() {
        // empty constructor to make sure this class is not instantiated
    }
}
