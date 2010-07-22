package nl.sense_os.commonsense.dto;

import java.sql.Timestamp;

public class ServiceStateValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;

    public ServiceStateValueModel() {

    }

    public ServiceStateValueModel(Timestamp timestamp, String serviceState, String phoneNumber,
            String manualSet) {
        super(timestamp, SensorValueModel.SERVICE_STATE);
        setState(serviceState);
        if (null != phoneNumber) {
            setPhoneNumber(phoneNumber);
        }
        setManualSet(manualSet);
    }

    public String getManualSet() {
        return get("manual_set");
    }

    public String getPhoneNumber() {
        return get("phone_number");
    }

    public String getState() {
        return get("service_state");
    }

    public void setManualSet(String manual) {
        set("manual_set", manual);
    }

    public void setPhoneNumber(String phoneNumber) {
        set("phone_number", phoneNumber);
    }

    public void setState(String state) {
        set("service_state", state);
    }
}
