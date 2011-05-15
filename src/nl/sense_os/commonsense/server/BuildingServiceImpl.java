package nl.sense_os.commonsense.server;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.rpc.BuildingService;
import nl.sense_os.commonsense.server.dao.BuildingDao;
import nl.sense_os.commonsense.server.dao.FloorDao;
import nl.sense_os.commonsense.shared.building.BuildingModel;
import nl.sense_os.commonsense.shared.building.FloorModel;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class BuildingServiceImpl extends RemoteServiceServlet implements BuildingService {
    
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(BuildingServiceImpl.class.getName());
    
    @Override
    public void deleteBuilding(String key) {
        BuildingDao dao = new BuildingDao();
        dao.delete(key);
    }

    @Override
    public BuildingModel getBuilding(String key) {
        BuildingDao dao = new BuildingDao();
        BuildingModel image = dao.get(key);
        return image;
    }

    @Override
    public String getBlobstoreUploadUrl(String params) {
        BlobstoreService service = BlobstoreServiceFactory.getBlobstoreService();
        String url = service.createUploadUrl("/floorupload" + params);
        return url;
    }

    @Override
    public List<BuildingModel> getUserBuildings(String userId) {
        BuildingDao dao = new BuildingDao();
        List<BuildingModel> buildings = dao.getUserBuildings(userId);
        return buildings;
    }

    @Override
    public String storeBuilding(BuildingModel building) {
        
        BuildingDao dao = new BuildingDao();
        
        String key = building.getKey();
        if (null == dao.get(building.getKey())) {
            key = dao.store(building);
        }
        return key;
    }

    @Override
    public void updateBuilding(BuildingModel building) {
        
        BuildingDao dao = new BuildingDao();
        dao.update(building);
    }

    @Override
    public void updateFloor(FloorModel floor) {
        
        FloorDao dao = new FloorDao();
        dao.update(floor);
    }
}
