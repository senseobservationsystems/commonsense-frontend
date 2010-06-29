package nl.sense_os.commonsense.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

import nl.sense_os.commonsense.dto.PhoneModel;
import nl.sense_os.commonsense.dto.SensorModel;
import nl.sense_os.commonsense.dto.UserModel;

public interface DataServiceAsync {
	public void getPhoneDetails(AsyncCallback<List<PhoneModel>> callback);
	public void checkLogin(String name, String password, AsyncCallback<UserModel> callback);
	public void isSessionAlive(AsyncCallback<UserModel> callback);
	public void logout(AsyncCallback<Void> callback);
	public void getSensors(String phoneId, AsyncCallback<List<SensorModel>> callback);
}
