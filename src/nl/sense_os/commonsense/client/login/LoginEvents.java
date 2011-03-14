package nl.sense_os.commonsense.client.login;

import com.extjs.gxt.ui.client.event.EventType;

public class LoginEvents {

    // general event types
    public static final EventType LoggedIn = new EventType();
    public static final EventType LoggedOut = new EventType();
    public static final EventType RequestLogin = new EventType();
    public static final EventType RequestLogout = new EventType();

    // login view and controller-only event types
    protected static final EventType AuthenticationFailure = new EventType();
    protected static final EventType CancelLogin = new EventType();
    protected static final EventType LoginCancelled = new EventType();
    protected static final EventType LoginError = new EventType();

    // Ajax-related event types
    public static final EventType AjaxLoginFailure = new EventType();
    public static final EventType AjaxLoginSuccess = new EventType();
    public static final EventType AjaxLogoutFailure = new EventType();
    public static final EventType AjaxLogoutSuccess = new EventType();
    public static final EventType AjaxUserFailure = new EventType();
    public static final EventType AjaxUserSuccess = new EventType();

    // layout-related event types
    public static final EventType Show = new EventType();
}
