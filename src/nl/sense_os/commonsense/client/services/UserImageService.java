package nl.sense_os.commonsense.client.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;

import nl.sense_os.commonsense.dto.building.Floor;

/**
 * See <a href=
 * "http://ikaisays.com/2010/09/08/gwt-blobstore-the-new-high-performance-image-serving-api-and-cute-dogs-on-office-chairs/"
 * >blobstore image service</a>
 */
@RemoteServiceRelativePath("images")
public interface UserImageService extends RemoteService  {
    public String getBlobstoreUploadUrl();
    public Floor get(String key);
    public List<Floor> getRecentlyUploaded();
    public void deleteImage(String key);
}
