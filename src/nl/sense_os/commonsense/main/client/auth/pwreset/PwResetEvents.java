package nl.sense_os.commonsense.main.client.auth.pwreset;

import com.extjs.gxt.ui.client.event.EventType;

public class PwResetEvents {

    public static final EventType ShowDialog = new EventType();
    static final EventType SubmitRequest = new EventType();
    static final EventType PwRemindSuccess = new EventType();
    static final EventType PwRemindFailure = new EventType();
    static final EventType PwRemindNotFound = new EventType();
    public static final EventType ShowNewPasswordForm = new EventType();
    static final EventType NewPasswordRequest = new EventType();
    static final EventType NewPasswordSuccess = new EventType();
    static final EventType NewPasswordFailure = new EventType();
}
