package nl.sense_os.commonsense.server.data;

import java.sql.Timestamp;

public class ServiceStateValue extends SensorValue {

    private String serviceState;
    private String phoneNumber;
    private String manualSet;

    public ServiceStateValue() {

    }

    public ServiceStateValue(Timestamp timestamp, String serviceState, String phoneNumber,
            String manualSet) {
        super(timestamp, SensorValue.SERVICE_STATE);
        setState(serviceState);
        if (null != phoneNumber) {
            setPhoneNumber(phoneNumber);
        }
        setManualSet(manualSet);
    }

    public String getManualSet() {
        return this.manualSet;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getState() {
        return this.serviceState;
    }

    public void setManualSet(String manual) {
        this.manualSet = manual;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setState(String state) {
        this.serviceState = state;
    }
}
