package nl.sense_os.commonsense.dto;

import java.sql.Timestamp;

public class BluetoothValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;

    public BluetoothValueModel() {
        
    }
    
    public BluetoothValueModel(Timestamp timestamp, String localAddress, String[] names,
            String[] addresses, String[] rssis) {
        super(timestamp, SensorValueModel.BLUETOOTH_DISC);
        setLocalAddress(localAddress);
        setNames(names);
        setAddresses(addresses);
        setRssis(rssis);
    }
    
    public void setAddresses(String[] addresses) {
        set("addresses", addresses);
    }
    
    public void setLocalAddress(String localAddress) {
        set("local_address", localAddress);
    }
    
    public void setNames(String[] names) {
        set("names", names);
    }
    
    public void setRssis(String[] rssis) {
        set("rssis", rssis);
    }
    
    public String[] getAddresses() {
        return get("addresses");
    }
    
    public String getLocalAddress() {
        return get("local_address");
    }
    
    public String[] getNames() {
        return get("names");
    }
    
    public String[] getRssis() {
        return get("rssis");
    }
}
