package nl.sense_os.commonsense.client.common.constants;

public class Urls {

    private static final String _STABLE_BASE = "common.sense-os.nl/api";
    private static final String _RC_BASE = "rc.sense-os.nl/api";
    private static final String _APIGEE_BASE = "api-senseos.apigee.com";
    private static final String _DEV_BASE = "api.dev.sense-os.nl";
    private static final String _TED_BASE = "217.77.159.221/restful/api";

    // find out which base URL to use
    private static final String _INTERMED_BASE_1 = Constants.RC_MODE ? _RC_BASE : _STABLE_BASE;
    private static final String _INTERMED_BASE_2 = Constants.APIGEE_MODE
            ? _APIGEE_BASE
            : _INTERMED_BASE_1;
    private static final String _INTERMED_BASE_3 = Constants.DEV_MODE
            ? _DEV_BASE
            : _INTERMED_BASE_2;
    public static final String HOST = Constants.TED_MODE ? _TED_BASE : _INTERMED_BASE_3;

    public static final String PATH_ENV = "environments";
    public static final String PATH_GROUPS = "groups";
    public static final String PATH_LOGIN = "login";
    public static final String PATH_LOGIN_GOOGLE = "login/openID/google";
    public static final String PATH_LOGOUT = "logout";
    public static final String PATH_SENSORS = "sensors";
    public static final String PATH_SERVICES = "services";
    public static final String PATH_STATES = "states";
    public static final String PATH_USERS = "users";

    public static final String HEADER_JSON_TYPE = "application/json";

    private Urls() {
        // empty constructor to make sure this class is not instantiated
    }
}
