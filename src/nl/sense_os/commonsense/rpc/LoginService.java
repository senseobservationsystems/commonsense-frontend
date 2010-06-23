package nl.sense_os.commonsense.rpc;

import nl.sense_os.commonsense.data.User;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("login")
public interface LoginService extends RemoteService {
	public User checkLogin(String userName, String password);
	public User isSessionAlive();
	public void logout();
}
