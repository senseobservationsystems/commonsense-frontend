package nl.sense_os.commonsense.client.auth;

import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Cookies;

public class SessionManager {

    public static void setSessionId(String sessionId) {
	if (Storage.isSessionStorageSupported()) {
	    Storage.getSessionStorageIfSupported().setItem("sessionId", sessionId);
	} else {
	    Cookies.setCookie("session_id", sessionId);
	}
    }

    public static String getSessionId() {
	if (Storage.isSessionStorageSupported()) {
	    return Storage.getSessionStorageIfSupported().getItem("sessionId");
	} else {
	    return Cookies.getCookie("session_id");
	}
    }

    public static void removeSessionId() {
	if (Storage.isSessionStorageSupported()) {
	    Storage.getSessionStorageIfSupported().removeItem("sessionId");
	} else {
	    Cookies.removeCookie("session_id");
	}
    }

    private SessionManager() {
	// private constructor to prevent instantiation
    }
}
