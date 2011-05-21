package nl.sense_os.commonsense.client.sensors.share;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.shared.constants.Constants;
import nl.sense_os.commonsense.shared.constants.Urls;
import nl.sense_os.commonsense.shared.models.SensorModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;

public class SensorShareController extends Controller {

    private final static Logger logger = Logger.getLogger("SensorShareController");
    private View shareDialog;

    public SensorShareController() {
        registerEventTypes(SensorShareEvents.ShowShareDialog, SensorShareEvents.ShareRequest,
                SensorShareEvents.ShareComplete, SensorShareEvents.ShareCancelled,
                SensorShareEvents.ShareFailed, SensorShareEvents.ShareAjaxFailure,
                SensorShareEvents.ShareAjaxSuccess);
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(SensorShareEvents.ShareRequest)) {
            // logger.fine( "ShareRequest");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final String user = event.<String> getData("user");
            shareSensors(sensors, user, 0);

        } else if (type.equals(SensorShareEvents.ShareAjaxSuccess)) {
            // logger.fine( "ShareAjaxSuccess");
            // final String response = event.<String> getData("response");
            shareSensorCallback(event);

        } else if (type.equals(SensorShareEvents.ShareAjaxFailure)) {
            logger.warning("ShareAjaxFailure");
            // final int code = event.getData("code");
            shareSensorErrorCallback(event);

        } else

        /*
         * Pass through to View
         */
        {
            forwardToView(this.shareDialog, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.shareDialog = new SensorShareDialog(this);
    }

    private void shareSensorCallback(AppEvent event) {
        final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
        final String username = event.<String> getData("user");
        sensors.remove(0);
        shareSensors(sensors, username, 0);
    }

    private void shareSensorErrorCallback(AppEvent event) {
        final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
        final String username = event.<String> getData("user");
        int retryCount = event.<Integer> getData("retry");

        if (retryCount < 3) {
            // retry
            retryCount++;
            shareSensors(sensors, username, retryCount);

        } else {
            // give up
            Dispatcher.forwardEvent(SensorShareEvents.ShareFailed);
        }
    }

    /**
     * Does request to share a list of sensors with a user. If there are multiple sensors in the
     * list, this method calls itself for each sensor in the list.
     * 
     * @param event
     *            AppEvent with "sensors" and "user" properties
     */
    private void shareSensors(List<SensorModel> sensors, String username, int retryCount) {

        if (null != sensors && sensors.size() > 0) {
            // get first sensor from the list
            SensorModel sensor = sensors.get(0);

            // prepare request properties
            final String method = "POST";
            final String url = Urls.SENSORS + "/" + sensor.<String> get("id") + "/users.json";
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final String body = "{\"user\":{\"username\":\"" + username + "\"}}";
            final AppEvent onSuccess = new AppEvent(SensorShareEvents.ShareAjaxSuccess);
            onSuccess.setData("sensors", sensors);
            onSuccess.setData("user", username);
            final AppEvent onFailure = new AppEvent(SensorShareEvents.ShareAjaxFailure);
            onFailure.setData("sensors", sensors);
            onFailure.setData("user", username);
            onFailure.setData("retry", retryCount);

            // send request to AjaxController
            final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
            ajaxRequest.setData("method", method);
            ajaxRequest.setData("url", url);
            ajaxRequest.setData("session_id", sessionId);
            ajaxRequest.setData("body", body);
            ajaxRequest.setData("onSuccess", onSuccess);
            ajaxRequest.setData("onFailure", onFailure);

            Dispatcher.forwardEvent(ajaxRequest);

        } else {
            // done
            Dispatcher.forwardEvent(SensorShareEvents.ShareComplete);
        }
    }
}
