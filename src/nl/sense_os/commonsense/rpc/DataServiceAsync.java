package nl.sense_os.commonsense.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;

@SuppressWarnings("unchecked")
public interface DataServiceAsync {
	public void getPhoneDetails(AsyncCallback callback);
	public void checkLogin(String userName, String password, AsyncCallback callback);
	public void isSessionAlive(AsyncCallback callback);
	public void logout(AsyncCallback callback);
}
