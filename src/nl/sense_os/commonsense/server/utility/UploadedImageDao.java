package nl.sense_os.commonsense.server.utility;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.sense_os.commonsense.dto.building.Floor;
import nl.sense_os.commonsense.server.data.User;

public class UploadedImageDao {
    DatastoreService datastore;

    public UploadedImageDao() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    public Floor get(String encodedKey) {
        Key key = KeyFactory.stringToKey(encodedKey);
        try {
            Entity result = datastore.get(key);
            Floor image = fromEntity(result);
            image.setKey(encodedKey);
            return image;
        } catch (EntityNotFoundException e) {
            return null;
        }
    }
    
    public List<Floor> getRecent(User user) {
        Query query = new Query("UploadedImage");
        query.addFilter(Floor.OWNER_ID, FilterOperator.EQUAL, "" + user.getId());
        query.addSort(Floor.CREATED_AT, SortDirection.DESCENDING);
        FetchOptions options = FetchOptions.Builder.withLimit(25);

        ArrayList<Floor> results = new ArrayList<Floor>();
        for (Entity result : datastore.prepare(query).asIterable(options)) {
            Floor image = fromEntity(result);
            results.add(image);
        }
        return results;
    }
    
    public void delete(String encodedKey) {
        Key key = KeyFactory.stringToKey(encodedKey);
        datastore.delete(key);
    }

    private Floor fromEntity(Entity result) {
        Floor image = new Floor();
        image.setCreatedAt((Date) result.getProperty(Floor.CREATED_AT));
        image.setServingUrl((String) result
                .getProperty(Floor.SERVING_URL));
        
        image.setOwnerId((String) result.getProperty(Floor.OWNER_ID));

        if (image.getKey() == null) {
            String encodedKey = KeyFactory.keyToString(result.getKey());
            image.setKey(encodedKey);
        }

        return image;
    }
}
