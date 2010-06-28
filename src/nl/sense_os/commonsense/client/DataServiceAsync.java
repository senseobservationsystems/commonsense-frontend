package nl.sense_os.commonsense.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

import nl.sense_os.commonsense.data.Phone;
import nl.sense_os.commonsense.data.User;

public interface DataServiceAsync {
	public void getPhoneDetails(AsyncCallback<List<Phone>> callback);
	public void checkLogin(String userName, String password, AsyncCallback<User> callback);
	public void isSessionAlive(AsyncCallback<User> callback);
	public void logout(AsyncCallback<Void> callback);
}
