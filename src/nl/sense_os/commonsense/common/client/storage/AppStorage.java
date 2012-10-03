package nl.sense_os.commonsense.common.client.storage;

import nl.sense_os.commonsense.common.client.component.Resettable;
import nl.sense_os.commonsense.common.client.model.User;

public interface AppStorage extends Resettable {

	void clearCurrentUser();

	void clearRememberedUsername();

	User getCurrentUser();

	String getPersistedUsername();

	void rememberUsername(String username);

	void setCurrentUser(User user);
}
