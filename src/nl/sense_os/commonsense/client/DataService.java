package nl.sense_os.commonsense.client;

import java.util.List;

import nl.sense_os.commonsense.data.Phone;
import nl.sense_os.commonsense.data.User;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("data")
public interface DataService extends RemoteService {
	public List<Phone> getPhoneDetails();
	public User checkLogin(String userName, String password);
	public User isSessionAlive();
	public void logout();
}
