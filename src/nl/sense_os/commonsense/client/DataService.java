package nl.sense_os.commonsense.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("data")
public interface DataService extends RemoteService {
	public User checkLogin(String userName, String password);
	public User isSessionAlive();
	public void logout();
	public String getPhoneDetails();
}
