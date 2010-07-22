package nl.sense_os.commonsense.server.data;

import java.sql.Timestamp;

public class PositionValue extends SensorValue {

    private double accuracy;
    private double altitude;
    private double latitude;
    private double longitude;
    private double speed;
    
    public PositionValue() {
        
    }
    
    public PositionValue(Timestamp timestamp, double accuracy, double altitude, double latitude, double longitude, double speed) {
        super(timestamp, SensorValue.POSITION);
        setAccuracy(accuracy);
        setAltitude(altitude);
        setLatitude(latitude);
        setLongitude(longitude);
        setSpeed(speed);
    }
    
    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }
    
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    public double getAccuracy() {
        return this.accuracy;
    }
    
    public double getAltitude() {
        return this.altitude;
    }
    
    public double getLatitude() {
        return this.latitude;
    }
    
    public double getLongitude() {
        return this.longitude;
    }
    
    public double getSpeed() {
        return this.speed;
    }
}
