package nl.sense_os.commonsense.server.ivo.data;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable
@Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
public abstract class SensorValue {
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
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
    
    @Persistent
    private int deviceId;

    @Persistent
    private int sensorType;
    
    @Persistent
    private Date timestamp;
    
    public SensorValue() {
        
    }

    public SensorValue(int deviceId, int sensorType, Date timestamp) {
        setDeviceId(deviceId);
        setSensorType(sensorType);
    	setTimestamp(timestamp);
    }
    
    public int getDeviceId() {
		return deviceId;
	}

	public Key getKey() {
        return key;
    }

	public int getSensorType() {
		return sensorType;
	}

	public Date getTimestamp() {
        return timestamp;
    }

    public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}
    
    public void setKey(Key key) {
        this.key = key;
    }
    
    public void setSensorType(int sensorType) {
		this.sensorType = sensorType;
	}

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

}
