package nl.sense_os.commonsense.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.sql.Timestamp;
import java.util.List;

import nl.sense_os.commonsense.dto.SenseTreeModel;
import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.UserModel;

@RemoteServiceRelativePath("data")
public interface DataService extends RemoteService {
	public UserModel checkLogin(String name, String password);
	public List<SenseTreeModel> getPhoneDetails();
	public List<SenseTreeModel> getSensors(int phoneId);
	public List<SensorValueModel> getSensorValues(int phoneId, int sensorId, Timestamp begin, Timestamp end);
	public UserModel isSessionAlive();
	public void logout();
}
