package nl.sense_os.commonsense.shared.client.storage;

import nl.sense_os.commonsense.shared.client.component.Resettable;
import nl.sense_os.commonsense.shared.client.model.User;

public interface AppStorage extends Resettable {

	void clearCurrentUser();

	void clearRememberedUsername();

	User getCurrentUser();

	String getPersistedUsername();

	void rememberUsername(String username);

	void setCurrentUser(User user);
}
