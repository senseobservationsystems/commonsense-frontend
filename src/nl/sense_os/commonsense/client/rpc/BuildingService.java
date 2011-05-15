package nl.sense_os.commonsense.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;

import nl.sense_os.commonsense.shared.building.BuildingModel;
import nl.sense_os.commonsense.shared.building.FloorModel;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("building")
public interface BuildingService extends RemoteService {

    public void deleteBuilding(String key);

    public String getBlobstoreUploadUrl(String params);

    public BuildingModel getBuilding(String key);

    public List<BuildingModel> getUserBuildings(String userId);

    public String storeBuilding(BuildingModel building);
    
    public void updateBuilding(BuildingModel building);
    
    public void updateFloor(FloorModel floor);
}
