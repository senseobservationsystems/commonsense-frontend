package nl.sense_os.commonsense.server.dao;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import nl.sense_os.commonsense.server.persistent.Building;
import nl.sense_os.commonsense.server.persistent.PMF;
import nl.sense_os.commonsense.shared.building.BuildingModel;
import nl.sense_os.commonsense.shared.building.FloorModel;

public class BuildingDao {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(BuildingDao.class.getName());

    public void delete(String encodedKey) {

        if (null == encodedKey) {
            return;
        }

        // generate the key for the persistence manager
        Key key = KeyFactory.stringToKey(encodedKey);

        // delete persisted Building object
        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            Building b = pm.getObjectById(Building.class, key);
            String[] floorKeys = b.getFloors();
            for (String floorKey : floorKeys) {
                new FloorDao().delete(floorKey);
            }

            // log.info("deleting building \"" + b.getName() + "\"");

            pm.deletePersistent(b);

        } finally {
            pm.close();
        }
    }

    private BuildingModel fromEntity(Building b) {

        // get the Building's floors
        String[] floorKeys = b.getFloors();
        ArrayList<FloorModel> floors = new ArrayList<FloorModel>();
        for (String key : floorKeys) {
            floors.add(new FloorDao().get(key));
        }

        BuildingModel result = new BuildingModel(b.getName(), floors, b.getUserId(),
                b.getCreated(), b.getModified());
        final Key key = b.getKey();
        if (null != key) {
            result.setKey(KeyFactory.keyToString(key));
        }
        return result;
    }

    public BuildingModel get(String encodedKey) {

        BuildingModel result = null;
        if (null == encodedKey) {
            return result;
        }

        // generate the key for the persistence manager
        Key key = KeyFactory.stringToKey(encodedKey);

        // get persisted Building object
        Building b = null;
        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            // log.info("getting building object");
            b = pm.getObjectById(Building.class, key);
        } finally {
            pm.close();
        }

        // convert to BuildingModel
        if (null != b) {
            result = fromEntity(b);
        }
        return result;
    }

    public List<BuildingModel> getUserBuildings(String userId) {

        PersistenceManager pm = PMF.get().getPersistenceManager();

        // log.info("querying recent building objects");
        Query query = pm.newQuery(Building.class);
        query.setFilter("userId == userIdParam");
        query.setOrdering("created desc");
        query.declareParameters("String userIdParam");
        List<BuildingModel> results = new ArrayList<BuildingModel>();
        try {
            @SuppressWarnings("unchecked")
            List<Building> buildings = (List<Building>) query.execute(userId);
            for (Building building : buildings) {
                results.add(fromEntity(building));
            }
        } finally {
            query.closeAll();
            pm.close();
        }

        return results;
    }

    public String store(BuildingModel building) {
        // create data entity and set current time and date
        Building toStore = toEntity(building);
        Date now = new Date();
        toStore.setCreated(now);
        toStore.setModified(now);

        // make persistent
        PersistenceManager pm = PMF.get().getPersistenceManager();
        String key = building.getKey();
        try {
            pm.makePersistent(toStore);
            key = KeyFactory.keyToString(toStore.getKey());
        } finally {
            pm.close();
        }
        return key;
    }

    private Building toEntity(BuildingModel b) {

        // get the Building's floor keys
        String[] floorKeys = new String[0];
        ArrayList<FloorModel> floors = b.getFloors();
        for (int i = 0; i < floors.size(); i++) {
            FloorModel floor = floors.get(i);
            if (null != floor) {
                String[] temp = new String[i + 1];
                if (floorKeys.length > 0) {
                    System.arraycopy(floorKeys, 0, temp, 0, floorKeys.length);
                }
                temp[i] = floor.getKey();
                floorKeys = temp;
            }
        }

        Building result = new Building(b.getName(), floorKeys, b.getUserId(), b.getCreated(),
                b.getModified());
        if (null != b.getKey()) {
            result.setKey(KeyFactory.stringToKey(b.getKey()));
        }
        return result;
    }

    public boolean update(BuildingModel b) {
        Key key = KeyFactory.stringToKey(b.getKey());

        // update persisted entity
        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            Building toUpdate = pm.getObjectById(Building.class, key);

            // log.info("update building \"" + toUpdate.getName() + "\"");

            toUpdate.setModified(new Date());
            toUpdate.setName(b.getName());

            List<FloorModel> oldFloors = fromEntity(toUpdate).getFloors();
            List<FloorModel> newFloors = b.getFloors();
            String[] floors = new String[newFloors.size()];
            for (int i = 0; i < newFloors.size(); i++) {
                FloorModel newFloor = newFloors.get(i);
                if (i < oldFloors.size()) {
                    // update old floor
                    FloorModel oldFloor = oldFloors.get(i);
                    if (false == newFloor.floorEquals(oldFloor)) {
                        new FloorDao().update(newFloor);
                    }
                } else if (null == newFloor.getKey()) {
                    // store new floor
                    String newKey = new FloorDao().store(newFloor);
                    newFloor.setKey(newKey);
                }
                floors[i] = newFloor.getKey();
            }
            toUpdate.setFloors(floors);
        } finally {
            pm.close();
        }
        return true;
    }
}
