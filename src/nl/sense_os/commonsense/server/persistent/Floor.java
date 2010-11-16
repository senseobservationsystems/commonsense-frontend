package nl.sense_os.commonsense.server.persistent;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Key;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class Floor {

    @Persistent
    private BlobKey blobKey;

    @Persistent
    private Date created;

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private Date modified;

    @Persistent
    private String name;

    @Persistent
    private int number;

    @Persistent
    private String url;

    @Persistent
    private String userId;

    public Floor(String url, int number, String name, String userId, Date created, Date modified) {
        setUrl(url);
        setNumber(number);
        setName(name);
        setUserId(userId);
        setCreated(created);
        setModified(modified);
    }

    public BlobKey getBlobKey() {
        return blobKey;
    }

    public Date getCreated() {
        return created;
    }

    public Key getKey() {
        return key;
    }

    public Date getModified() {
        return modified;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public String getUrl() {
        return url;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setBlobKey(BlobKey blobKey) {
        this.blobKey = blobKey;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}