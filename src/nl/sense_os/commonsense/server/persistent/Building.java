package nl.sense_os.commonsense.server.persistent;

import com.google.appengine.api.datastore.Key;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class Building {

    @Persistent
    private Date created;

    @Persistent
    private Date modified;

    @Persistent
    private String[] floors;

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private String name;

    @Persistent
    private String userId;

    public Building(String name, String[] floors, String userId, Date created, Date modified) {
        setName(name);
        setFloors(floors);
        setUserId(userId);
        setCreated(created);
        setModified(modified);
    }

    public Date getCreated() {
        return this.created;
    }

    public Date getModified() {
        return this.modified;
    }

    public String[] getFloors() {
        return this.floors;
    }

    public Key getKey() {
        return this.key;
    }

    public String getName() {
        return this.name;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public void setFloors(String[] floors) {
        this.floors = floors;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
