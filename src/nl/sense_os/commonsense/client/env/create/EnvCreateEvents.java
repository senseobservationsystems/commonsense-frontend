package nl.sense_os.commonsense.client.env.create;

import com.extjs.gxt.ui.client.event.EventType;

public class EnvCreateEvents {

    public static final EventType ShowCreator = new EventType();
    protected static final EventType CreateRequest = new EventType();
    public static final EventType CreateSuccess = new EventType();
    protected static final EventType CreateFailure = new EventType();
    protected static final EventType CreateAjaxSuccess = new EventType();
    protected static final EventType CreateAjaxFailure = new EventType();
    protected static final EventType AddSensorsAjaxSuccess = new EventType();
    protected static final EventType AddSensorsAjaxFailure = new EventType();
    protected static final EventType CreateSensorAjaxSuccess = new EventType();
    protected static final EventType CreateSensorAjaxFailure = new EventType();
    protected static final EventType PositionSensorAjaxSuccess = new EventType();
    protected static final EventType PositionSensorAjaxFailure = new EventType();
    protected static final EventType SetPositionAjaxSuccess = new EventType();
    protected static final EventType SetPositionAjaxFailure = new EventType();

    protected static final EventType Forward = new EventType();
    protected static final EventType Back = new EventType();
    protected static final EventType Cancel = new EventType();
    protected static final EventType OutlineComplete = new EventType();
}
