package nl.sense_os.commonsense.client.auth.login;

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
}
