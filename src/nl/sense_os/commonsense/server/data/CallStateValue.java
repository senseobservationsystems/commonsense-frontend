package nl.sense_os.commonsense.server.data;

import java.sql.Timestamp;

public class CallStateValue extends SensorValue {
    
    private String callState;
    private String number;
    
    public CallStateValue() {
        
    }
    
    public CallStateValue(Timestamp timestamp, String callState, String number) {
        super(timestamp, SensorValue.CALLSTATE);
        setCallState(callState);
        if (null != number) {
            setNumber(number);
        }
    }

    public void setCallState(String callState) {
        this.callState = callState;
    }
    
    public void setNumber(String number) {
        this.number = number;
    }
    
    public String getCallState() {
        return this.callState;
    }
    
    public String getNumber() {
        return this.number;
    }
}
