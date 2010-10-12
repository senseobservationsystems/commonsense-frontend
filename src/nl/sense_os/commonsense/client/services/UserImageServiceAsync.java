package nl.sense_os.commonsense.client.services;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

import nl.sense_os.commonsense.dto.building.Floor;

/**
 * See <a href=
 * "http://ikaisays.com/2010/09/08/gwt-blobstore-the-new-high-performance-image-serving-api-and-cute-dogs-on-office-chairs/"
 * >blobstore image service</a>
 */
public interface UserImageServiceAsync {
    public void getBlobstoreUploadUrl(AsyncCallback<String> callback);

    void get(String key, AsyncCallback<Floor> callback);

    void getRecentlyUploaded(AsyncCallback<List<Floor>> callback);

    void deleteImage(String key, AsyncCallback<Void> callback);
}
