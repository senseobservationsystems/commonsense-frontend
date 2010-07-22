package nl.sense_os.commonsense.dto;

import java.sql.Timestamp;

public class CallStateValueModel extends SensorValueModel {

    private static final long serialVersionUID = 1L;
    
    public CallStateValueModel() {
        
    }
    
    public CallStateValueModel(Timestamp timestamp, String callState, String number) {
        super(timestamp, SensorValueModel.CALLSTATE);
        setCallState(callState);
        if (null != number) {
            setNumber(number);
        }
    }

    public void setCallState(String callState) {
        set("call_state", callState);
    }
    
    public void setNumber(String number) {
        set("number", number);
    }
    
    public String getCallState() {
        return get("call_state");
    }
    
    public String getNumber() {
        return get("number");
    }
}
