package nl.sense_os.commonsense.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

@SuppressWarnings("unchecked")
public interface DataServiceAsync {
	public void checkLogin(String userName, String password, AsyncCallback callback);
	public void isSessionAlive(AsyncCallback callback);
	public void logout(AsyncCallback callback);
	public void getPhoneDetails(AsyncCallback callback);
}
