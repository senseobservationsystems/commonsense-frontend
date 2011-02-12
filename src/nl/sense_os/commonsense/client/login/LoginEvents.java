package nl.sense_os.commonsense.client.login;

import com.extjs.gxt.ui.client.event.EventType;

public class LoginEvents {
    public static final EventType LoggedIn = new EventType();
    public static final EventType LoggedOut = new EventType();

    public static final EventType RequestLogin = new EventType();
    public static final EventType LoginReqSuccess = new EventType();
    public static final EventType LoginReqError = new EventType();
    public static final EventType AuthenticationFailure = new EventType();
    public static final EventType LoginError = new EventType();

    public static final EventType RequestLogout = new EventType();
    public static final EventType LogoutReqSuccess = new EventType();
    public static final EventType LogoutReqError = new EventType();

    public static final EventType UserReqSuccess = new EventType();
    public static final EventType UserReqError = new EventType();

    public static final EventType CancelLogin = new EventType();
    public static final EventType LoginCancelled = new EventType();

    public static final EventType Show = new EventType();
    public static final EventType Hide = new EventType();

}
