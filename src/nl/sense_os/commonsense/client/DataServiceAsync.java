package nl.sense_os.commonsense.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.sql.Timestamp;
import java.util.List;

import nl.sense_os.commonsense.dto.SenseTreeModel;
import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.TagModel;
import nl.sense_os.commonsense.dto.UserModel;

public interface DataServiceAsync {
	public void checkLogin(String name, String password, AsyncCallback<UserModel> callback);
	public void getPhoneDetails(AsyncCallback<List<SenseTreeModel>> callback);
	public void getSensors(int phoneId, AsyncCallback<List<SenseTreeModel>> callback);
    public void getSensorValues(TagModel tag, Timestamp begin, Timestamp end, AsyncCallback<List<SensorValueModel>> callback);
	public void getTags(String rootTag, AsyncCallback<List<TagModel>> callback);
	public void isSessionAlive(AsyncCallback<UserModel> callback);
	public void logout(AsyncCallback<Void> callback);
}
