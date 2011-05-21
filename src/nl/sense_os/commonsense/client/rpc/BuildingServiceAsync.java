package nl.sense_os.commonsense.client.rpc;

import java.util.List;

import nl.sense_os.commonsense.shared.building.BuildingModel;
import nl.sense_os.commonsense.shared.building.FloorModel;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>BuildingService</code>.
 * 
 * @deprecated Building service is no longer used
 */
@Deprecated
public interface BuildingServiceAsync {

    void deleteBuilding(String key, AsyncCallback<Void> callback);

    void getBlobstoreUploadUrl(String params, AsyncCallback<String> callback);

    void getBuilding(String key, AsyncCallback<BuildingModel> callback);

    void getUserBuildings(String userId, AsyncCallback<List<BuildingModel>> callback);

    void storeBuilding(BuildingModel building, AsyncCallback<String> callback);

    void updateBuilding(BuildingModel building, AsyncCallback<Void> callback);

    void updateFloor(FloorModel floor, AsyncCallback<Void> callback);
}
