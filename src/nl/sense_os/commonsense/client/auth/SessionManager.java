package nl.sense_os.commonsense.client.auth;

import java.util.Date;

import nl.sense_os.commonsense.client.common.constants.Constants;

import com.google.gwt.user.client.Cookies;

public class SessionManager {

    private static final Date expires = new Date(System.currentTimeMillis() + 3600000l * 24 * 365);

    public static void setSessionId(String sessionId) {
	String domain = Constants.DEV_MODE ? "dev.sense-os.nl" : ".sense-os.nl";
	Cookies.setCookie("session_id", sessionId, expires, domain, null, false);
    }

    public static String getSessionId() {
	return Cookies.getCookie("session_id");
    }

    public static void removeSessionId() {
	Cookies.removeCookie("session_id");
    }

    private SessionManager() {
	// private constructor to prevent instantiation
    }
}
