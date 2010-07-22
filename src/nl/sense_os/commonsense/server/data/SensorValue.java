package nl.sense_os.commonsense.server.data;

import java.sql.Timestamp;

public abstract class SensorValue {

    /**
     * Noise sensor.
     * value: float
     */
    public static final int NOISE = 1;
    /**
     * Audio stream location.
     * value: String (direct connection URI)
     * @deprecated
     */
    public static final int AUDIOSTREAM = 2;
    /**
     * Bluetooth address.
     * value: String (UUID)
     */
    public static final int BLUETOOTH_ADDR = 3;
    /**
     * Bluetooth discovery information, JSON object. 
     * local_bt_address: String (MAC address);
     * bt_devices: List of BtDevices.
     */
    public static final int BLUETOOTH_DISC = 4;
    /**
     * Microphone stream
     * value: String (stream URL)
     */
    public static final int MIC = 5;
    /**
     * MyriaNed temperature measurement, JSON object.
     * node_id: int;
     * sensor: String (sensor name);
     * value: int (1/100 Celsius);
     * variance: int
     */
    public static final int MYRIA_TEMPERATURE = 6;
    /**
     * MyriaNed humidity measurement, JSON object.
     * node_id: int;
     * sensor: String (sensor name);
     * value: int (1/1000 Degrees);
     * variance: int
     */
    public static final int MYRIA_HUMIDITY = 7;
    /**
     * Sense service state, JSON object.
     * state: String
     * manualSet: boolean
     * phone number: String
     */
    public static final int SERVICE_STATE = 8;
    /**
     * Unread text messages.
     * value: boolean
     */
    public static final int UNREAD_MSG = 9;
    /**
     * Data connection state.
     * value: String (connected / disconnected)
     */
    public static final int DATA_CONNECTION = 10;
    /**
     * Phone call state, JSON object.
     * state: String (idle / calling / ringing)
     * incomingNumber: String
     */
    public static final int CALLSTATE = 11;
    /**
     * Device properties, JSON object.
     * brand: String
     * type: String
     */
    public static final int DEVICE_PROPS = 12;
    /**
     * IP address.
     * value: String
     */
    public static final int IP = 13;
    /**
     * Position, JSON object.
     * accuracy: float
     * altitude: float
     * latitude: float
     * longitude: float
     * speed: float
     */
    public static final int POSITION = 14;
	private Timestamp timestamp;
	private int type;

	public SensorValue() {
	    
	}

	public SensorValue(Timestamp timestamp, int type) {
		this.timestamp = timestamp;
		this.type = type;
	}
	
	public Timestamp getTimestamp() {
		return timestamp;
	}
	
	public int getType() {
	    return type;
	}
	
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setType(int type) {
	    this.type = type;
	}
}
