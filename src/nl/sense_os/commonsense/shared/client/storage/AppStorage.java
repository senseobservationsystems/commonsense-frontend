package nl.sense_os.commonsense.shared.client.storage;

import nl.sense_os.commonsense.lib.client.model.apiclass.User;
import nl.sense_os.commonsense.shared.client.component.Resettable;

public interface AppStorage extends Resettable {

	void clearCurrentUser();

	void clearRememberedUsername();

	User getCurrentUser();

	String getPersistedUsername();

	void rememberUsername(String username);

	void setCurrentUser(User user);
}
