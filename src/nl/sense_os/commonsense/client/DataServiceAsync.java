package nl.sense_os.commonsense.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.Date;
import java.util.List;

import nl.sense_os.commonsense.dto.TagModel;
import nl.sense_os.commonsense.dto.TaggedDataModel;
import nl.sense_os.commonsense.dto.UserModel;

public interface DataServiceAsync {
	public void checkLogin(String name, String password, AsyncCallback<UserModel> callback);
	public void getIvoSensorValues(TagModel tag, Date begin, Date end, 
            AsyncCallback<TaggedDataModel> callback);
    public void getSensorValues(TagModel tag, Date begin, Date end, 
            AsyncCallback<TaggedDataModel> callback);
    public void getSensorValuesPaged(TagModel tag, int offset, int limit, 
            AsyncCallback<TaggedDataModel> callback);
	public void getTags(TagModel rootTag, AsyncCallback<List<TagModel>> callback);
	public void isSessionAlive(AsyncCallback<UserModel> callback);
	public void logout(AsyncCallback<Void> callback);
}
