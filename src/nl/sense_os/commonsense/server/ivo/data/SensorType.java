package nl.sense_os.commonsense.server.ivo.data;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class SensorType {
    /**
     * Boolean sensor value.
     */
    public static final int BOOL = 1;
    /**
     * Float sensor value.
     */
    public static final int FLOAT = 2;
    /**
     * JSON sensor value.
     */
    public static final int JSON = 3;
    /**
     * String sensor value.
     */
    public static final int STRING = 4;
    
    @Persistent
    private int id;
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

    @Persistent
    private String name;

    @Persistent
	private int type;
	
    public SensorType(int id, String name, int type) {
    	this.id = id;
    	this.name = name;
    	this.type = type;
    }
    
	public int getId() {
		return id;
	}
	
	public Key getKey() {
        return key;
    }
    
    public String getName() {
		return name;
	}

	public int getType() {
		return type;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setKey(Key key) {
        this.key = key;
    }

    public void setName(String name) {
		this.name = name;
	}

    public void setType(int type) {
		this.type = type;
	}	
}
