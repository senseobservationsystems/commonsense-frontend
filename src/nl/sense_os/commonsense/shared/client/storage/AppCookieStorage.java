package nl.sense_os.commonsense.shared.client.storage;

import java.util.Date;
import java.util.logging.Logger;

import nl.sense_os.commonsense.shared.client.model.User;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.user.client.Cookies;

public class AppCookieStorage implements AppStorage {

	private static final Logger LOG = Logger.getLogger(AppCookieStorage.class.getName());
	private static final long DEFAULT_EXPIRY = 1000l * 60 * 60 * 24 * 14;

	public AppCookieStorage() {
		LOG.config("Using cookies for storage");
	}

	@Override
	public void clearCurrentUser() {
		Cookies.removeCookie("currentUser");
	}

	@Override
	public void clearRememberedUsername() {
		Cookies.removeCookie("username");
	}

	@Override
	public User getCurrentUser() {
		String serialized = Cookies.getCookie("currentUser");
		try {
			return JsonUtils.safeEval(serialized);
		} catch (Exception e) {
			LOG.fine("Failed to deserialize current user from storage: " + serialized + ". " + e);
			return null;
		}
	}

	@Override
	public String getPersistedUsername() {
		return Cookies.getCookie("username");
	}

	@Override
	public void rememberUsername(String username) {
		Cookies.setCookie("username", username, new Date(System.currentTimeMillis()
				+ DEFAULT_EXPIRY));
	}

	@Override
	public void reset() {
		clearCurrentUser();
	}

	@Override
	public void setCurrentUser(User user) {
		// TODO
	}
}
