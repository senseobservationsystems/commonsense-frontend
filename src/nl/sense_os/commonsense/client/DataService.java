package nl.sense_os.commonsense.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;

import nl.sense_os.commonsense.dto.PhoneModel;
import nl.sense_os.commonsense.dto.SensorModel;
import nl.sense_os.commonsense.dto.UserModel;

@RemoteServiceRelativePath("data")
public interface DataService extends RemoteService {
	public UserModel checkLogin(String name, String password);
	public UserModel isSessionAlive();
	public void logout();
	public List<PhoneModel> getPhoneDetails();
	public List<SensorModel> getSensors(String phoneId);
}
