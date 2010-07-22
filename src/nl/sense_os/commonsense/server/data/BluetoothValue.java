package nl.sense_os.commonsense.server.data;

import java.sql.Timestamp;

public class BluetoothValue extends SensorValue {

    private String localAddress;
    private String[] addresses;
    private String[] names;
    private String[] rssis;

    public BluetoothValue() {

    }

    public BluetoothValue(Timestamp timestamp, String localAddress, String[] addresses,
            String[] names, String[] rssis) {
        super(timestamp, SensorValue.BLUETOOTH_DISC);
        setLocalAddress(localAddress);
        setNames(names);
        setAddresses(addresses);
        setRssis(rssis);
    }

    public void setAddresses(String[] addresses) {
        this.addresses = addresses;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public void setNames(String[] names) {
        this.names = names;
    }

    public void setRssis(String[] rssis) {
        this.rssis = rssis;
    }

    public String[] getAddresses() {
        return this.addresses;
    }

    public String getLocalAddress() {
        return this.localAddress;
    }

    public String[] getNames() {
        return this.names;
    }

    public String[] getRssis() {
        return this.rssis;
    }
}
