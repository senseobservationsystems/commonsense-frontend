package nl.sense_os.commonsense.shared.client.constant;

import nl.sense_os.commonsense.shared.client.communication.CommonSenseApi;
import nl.sense_os.commonsense.shared.client.util.Constants;

/**
 * Contains URLs and other constants for use in communication with the CommonSense API.
 * 
 * @deprecated Use {@link CommonSenseApi} class for communication with CommonSense
 */
public class Urls {

	private static final String _STABLE_BASE = "common.sense-os.nl/api";
	private static final String _RC_BASE = "rc.sense-os.nl/api";
    private static final String _BACKEND_RC_BASE = "rc.dev.sense-os.nl/api";
	private static final String _DEV_BASE = "api.dev.sense-os.nl";
	private static final String _GENERIC_BASE = "api.sense-os.nl";

	// find out which base URL to use
	public static final String HOST = Constants.STABLE_MODE ? _STABLE_BASE
            : Constants.RC_MODE ? _RC_BASE : Constants.DEV_MODE ? _DEV_BASE
                    : Constants.BACKEND_RC_MODE ? _BACKEND_RC_BASE : _GENERIC_BASE;

	public static final String PATH_ENV = "environments";
	public static final String PATH_GROUPS = "groups";
	public static final String PATH_LOGIN = "login";
	public static final String PATH_LOGIN_GOOGLE = "login/openID/google";
	public static final String PATH_LOGOUT = "logout";
	public static final String PATH_PW_RESET_REQUEST = "requestPasswordReset";
	public static final String PATH_PW_RESET = "resetPassword";
	public static final String PATH_SENSORS = "sensors";
	public static final String PATH_SERVICES = "services";
	public static final String PATH_STATES = "states";
	public static final String PATH_USERS = "users";

	public static final String HEADER_JSON_TYPE = "application/json";

	private Urls() {
		// empty constructor to make sure this class is not instantiated
	}
}
