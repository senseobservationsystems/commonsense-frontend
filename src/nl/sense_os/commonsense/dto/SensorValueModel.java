package nl.sense_os.commonsense.dto;

import com.extjs.gxt.ui.client.data.BaseModel;

import java.sql.Timestamp;

public abstract class SensorValueModel extends BaseModel {
    /**
     * Audio stream location. value: String (direct connection URI)
     * 
     * @deprecated
     */
    @Deprecated
    public static final int AUDIOSTREAM = 2;
    /**
     * Bluetooth address. value: String (UUID)
     */
    public static final int BLUETOOTH_ADDR = 3;
    /**
     * Bluetooth discovery information, JSON object. local_bt_address: String (MAC address);
     * bt_devices: List of BtDevices.
     */
    public static final int BLUETOOTH_DISC = 4;
    /**
     * Phone call state, JSON object. state: String (idle / calling / ringing) incomingNumber:
     * String
     */
    public static final int CALLSTATE = 11;
    /**
     * Data connection state. value: String (connected / disconnected)
     */
    public static final int DATA_CONNECTION = 10;
    /**
     * Device properties, JSON object. brand: String type: String
     */
    public static final int DEVICE_PROPS = 12;
    /**
     * IP address. value: String
     */
    public static final int IP = 13;
    /**
     * Microphone stream value: String (stream URL)
     */
    public static final int MIC = 5;
    /**
     * MyriaNed humidity measurement, JSON object. node_id: int; sensor: String (sensor name);
     * value: int (1/1000 Degrees); variance: int
     */
    public static final int MYRIA_HUMIDITY = 7;
    /**
     * MyriaNed temperature measurement, JSON object. node_id: int; sensor: String (sensor name);
     * value: int (1/100 Celsius); variance: int
     */
    public static final int MYRIA_TEMPERATURE = 6;
    /**
     * Noise sensor. value: float
     */
    public static final int NOISE = 1;
    /**
     * Position, JSON object. accuracy: float altitude: float latitude: float longitude: float
     * speed: float
     */
    public static final int POSITION = 14;
    private static final long serialVersionUID = 1L;
    /**
     * Sense service state, JSON object. state: String manualSet: boolean phone number: String
     */
    public static final int SERVICE_STATE = 8;
    /**
     * Unread text messages. value: boolean
     */
    public static final int UNREAD_MSG = 9;

    public SensorValueModel() {
        // empty constructor necessary for serializing
    }

    public SensorValueModel(Timestamp timestamp, int type) {
        setTimestamp(timestamp);
        setType(type);
    }

    public Timestamp getTimestamp() {
        return get("timestamp");
    }

    public int getType() {
        return get("type", -1);
    }

    public void setTimestamp(Timestamp timestamp) {
        set("timestamp", timestamp);
    }

    public void setType(int type) {
        set("type", type);
    }
}
