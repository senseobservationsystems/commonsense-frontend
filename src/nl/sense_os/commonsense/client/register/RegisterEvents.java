package nl.sense_os.commonsense.client.register;

import com.extjs.gxt.ui.client.event.EventType;

public class RegisterEvents {

    public static final EventType Show = new EventType();

    protected static final EventType RegisterRequest = new EventType();
    protected static final EventType AjaxRegisterSuccess = new EventType();
    protected static final EventType AjaxRegisterFailure = new EventType();
    protected static final EventType RegisterSuccess = new EventType();
    protected static final EventType RegisterFailure = new EventType();

}
