package nl.sense_os.commonsense.client.events;

import com.extjs.gxt.ui.client.event.EventType;

public class LoginEvents {
    public static final EventType LoggedIn = new EventType();
    public static final EventType LoggedOut = new EventType();
    public static final EventType AuthenticationFailure = new EventType();
    public static final EventType LoginError = new EventType();
    public static final EventType RequestLogin = new EventType();
    public static final EventType RequestLogout = new EventType();
    public static final EventType CancelLogin = new EventType();
    public static final EventType LoginCancelled = new EventType();
}
