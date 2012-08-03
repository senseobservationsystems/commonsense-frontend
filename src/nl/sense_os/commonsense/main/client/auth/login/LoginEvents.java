package nl.sense_os.commonsense.main.client.auth.login;

import com.extjs.gxt.ui.client.event.EventType;

public class LoginEvents {

    // general event types
    public static final EventType LoginSuccess = new EventType();
    public static final EventType LoggedOut = new EventType();
    public static final EventType LoginRequest = new EventType();
    public static final EventType RequestLogout = new EventType();

    // login view and controller-only event types
    protected static final EventType AuthenticationFailure = new EventType();
    protected static final EventType LoginFailure = new EventType();

    // layout-related event types
    public static final EventType Show = new EventType();
    public static final EventType GoogleAuthRequest = new EventType();
    public static final EventType GoogleAuthResult = new EventType();
    public static final EventType GoogleAuthConflict = new EventType();
    public static final EventType GoogleAuthError = new EventType();
    public static final EventType GoogleConnectRequest = new EventType();
}
