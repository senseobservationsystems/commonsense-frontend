package nl.sense_os.commonsense.shared.client.storage;

import java.util.logging.Logger;

import nl.sense_os.commonsense.lib.client.model.apiclass.User;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.storage.client.Storage;

public class AppWebStorage implements AppStorage {

	private static final Logger LOG = Logger.getLogger(AppWebStorage.class.getName());

	public AppWebStorage() {

		if (!Storage.isLocalStorageSupported()) {
			LOG.warning("HTML5 local storage is not supported!");
		}
		if (!Storage.isSessionStorageSupported()) {
			LOG.warning("HTML5 session storage is not supported!");
		}
	}

	@Override
	public void clearCurrentUser() {
		Storage.getSessionStorageIfSupported().removeItem("currentUser");
	}

	@Override
	public void clearRememberedUsername() {
		Storage.getLocalStorageIfSupported().removeItem("username");
	}

	@Override
	public User getCurrentUser() {
		String serialized = Storage.getSessionStorageIfSupported().getItem("currentUser");
		if (null == serialized) {
			return null;
		}
		try {
			return JsonUtils.safeEval(serialized);
		} catch (Exception e) {
			LOG.fine("Failed to deserialize current user from storage: " + serialized + ". " + e);
			return null;
		}
	}

	@Override
	public String getPersistedUsername() {
		return Storage.getLocalStorageIfSupported().getItem("username");
	}

	@Override
	public void rememberUsername(String username) {
		Storage.getLocalStorageIfSupported().setItem("username", username);
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
