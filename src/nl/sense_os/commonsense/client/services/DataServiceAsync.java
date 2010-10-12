package nl.sense_os.commonsense.client.services;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.Date;
import java.util.List;

import nl.sense_os.commonsense.dto.TagModel;
import nl.sense_os.commonsense.dto.TaggedDataModel;
import nl.sense_os.commonsense.dto.UserModel;
import nl.sense_os.commonsense.dto.building.Floor;

public interface DataServiceAsync {
    
    public void addLocationValues(int x, int y, int deviceId, String blobKey, AsyncCallback<Void> callback);
    
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

    public void getBlobstoreUploadUrl(AsyncCallback<String> callback);

    void get(String key, AsyncCallback<Floor> callback);

    void getRecentImages(AsyncCallback<List<Floor>> callback);

    void deleteImage(String key, AsyncCallback<Void> callback);
}
