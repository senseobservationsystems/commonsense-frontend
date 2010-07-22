package nl.sense_os.commonsense.dto;

import java.sql.Timestamp;

public class PositionValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;

    public PositionValueModel() {
        
    }
    
    public PositionValueModel(Timestamp timestamp, double accuracy, double altitude, double latitude, double longitude, double speed) {
        super(timestamp, SensorValueModel.POSITION);
        setAccuracy(accuracy);
        setAltitude(altitude);
        setLatitude(latitude);
        setLongitude(longitude);
        setSpeed(speed);
    }
    
    public void setAccuracy(double accuracy) {
        set("accuracy", accuracy);
    }
    
    public void setAltitude(double altitude) {
        set("altitude", altitude);
    }
    
    public void setLatitude(double latitude) {
        set("latitude", latitude);
    }
    
    public void setLongitude(double longitude) {
        set("longitude", longitude);
    }
    
    public void setSpeed(double speed) {
        set("speed", speed);
    }
    
    public double getAccuracy() {
        return get("accuracy");
    }
    
    public double getAltitude() {
        return get("altitude");
    }
    
    public double getLatitude() {
        return get("latitude");
    }
    
    public double getLongitude() {
        return get("longitude");
    }
    
    public double getSpeed() {
        return get("speed");
    }    
}
