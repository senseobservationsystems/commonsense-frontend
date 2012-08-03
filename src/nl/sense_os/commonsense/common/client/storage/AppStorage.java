package nl.sense_os.commonsense.common.client.storage;

import nl.sense_os.commonsense.common.client.component.Resettable;
import nl.sense_os.commonsense.common.client.model.UserJso;

public interface AppStorage extends Resettable {

	void clearCurrentUser();

	void clearRememberedUsername();

	UserJso getCurrentUser();

	String getPersistedUsername();

	void rememberUsername(String username);

	void setCurrentUser(UserJso user);
}
