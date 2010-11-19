package nl.sense_os.commonsense.server.dao;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import java.util.Date;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import nl.sense_os.commonsense.dto.building.FloorModel;
import nl.sense_os.commonsense.server.persistent.Floor;
import nl.sense_os.commonsense.server.persistent.PMF;

public class FloorDao {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(FloorDao.class.getName());

    public void delete(String encodedKey) {

        if (null == encodedKey) {
            return;
        }
        
        // generate the key for the persistence manager
        Key key = KeyFactory.stringToKey(encodedKey);

        // delete persisted Building object
        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            Floor f = pm.getObjectById(Floor.class, key);

            BlobKey blobKey = f.getBlobKey();
            if (null != blobKey) {
                BlobstoreServiceFactory.getBlobstoreService().delete(blobKey);
            }

            // log.info("deleting floor \"" + f.getName() + "\"");
            
            pm.deletePersistent(f);

        } finally {
            pm.close();
        }
    }

    private FloorModel fromEntity(Floor f) {
        FloorModel result = new FloorModel(f.getUrl(), f.getNumber(), f.getName(), f.getUserId(),
                f.getCreated(), f.getModified());
        final Key key = f.getKey();
        if (null != key) {
            result.setKey(KeyFactory.keyToString(key));
        }
        final BlobKey blobKey = f.getBlobKey();
        if (null != blobKey) {
            result.setBlobKey(blobKey.getKeyString());
        }
        return result;
    }

    public FloorModel get(String encodedKey) {

        FloorModel result = null;
        if (null == encodedKey) {
            return result;
        }

        // generate the key for the persistence manager
        Key key = KeyFactory.stringToKey(encodedKey);

        PersistenceManager pm = PMF.get().getPersistenceManager();
        Floor f = null;
        try {
            // log.info("getting floor object");
            f = pm.getObjectById(Floor.class, key);
        } finally {
            pm.close();
        }

        if (null != f) {
            result = fromEntity(f);
        }
        return result;
    }

    public String store(FloorModel floor) {

        // create data object and set current date
        Floor toStore = toEntity(floor);
        Date now = new Date();
        toStore.setCreated(now);
        toStore.setModified(now);

        // make persistent
        PersistenceManager pm = PMF.get().getPersistenceManager();
        String key = floor.getKey();
        try {            
            pm.makePersistent(toStore);
            key = KeyFactory.keyToString(toStore.getKey());
        } finally {
            pm.close();
        }
        return key;
    }

    private Floor toEntity(FloorModel f) {
        Floor result = new Floor(f.getUrl(), f.getNumber(), f.getName(), f.getUserId(),
                f.getCreated(), f.getModified());
        final String key = f.getKey();
        if (null != key) {
            result.setKey(KeyFactory.stringToKey(key));
        }
        final String blobKeyString = f.getBlobKey();
        if (null != blobKeyString) {
            result.setBlobKey(new BlobKey(blobKeyString));
        }
        return result;
    }

    public void update(FloorModel f) {
        Key key = KeyFactory.stringToKey(f.getKey());

        // make persistent
        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            // log.info("updating floor \"" + f.getName() + "\"");
            
            Floor toUpdate = pm.getObjectById(Floor.class, key);
            toUpdate.setModified(new Date());
            toUpdate.setName(f.getName());
            toUpdate.setNumber(f.getNumber());
            toUpdate.setUrl(f.getUrl());
        } finally {
            pm.close();
        }
    }
}
