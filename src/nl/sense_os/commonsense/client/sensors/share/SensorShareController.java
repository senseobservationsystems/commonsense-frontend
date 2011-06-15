package nl.sense_os.commonsense.client.sensors.share;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.common.models.UserModel;
import nl.sense_os.commonsense.client.groups.list.GetGroupUsersResponseJso;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.core.client.JsonUtils;

public class SensorShareController extends Controller {

    private final static Logger LOG = Logger.getLogger(SensorShareController.class.getName());
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
            LOG.finest("ShareRequest");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final String user = event.<String> getData("user");
            shareSensor(sensors, user, 0, 0);

        } else if (type.equals(SensorShareEvents.ShareAjaxSuccess)) {
            LOG.finest("ShareAjaxSuccess");
            final String response = event.<String> getData("response");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final int index = event.getData("index");
            final String username = event.<String> getData("user");
            onShareSensorSuccess(response, sensors, index, username);

        } else if (type.equals(SensorShareEvents.ShareAjaxFailure)) {
            LOG.warning("ShareAjaxFailure");
            // final int code = event.getData("code");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final String username = event.<String> getData("user");
            final int index = event.getData("index");
            final int retryCount = event.getData("retry");
            onShareSensorFailure(sensors, username, index, retryCount);

        } else

        /*
         * Pass through to View
         */
        {
            forwardToView(shareDialog, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        shareDialog = new SensorShareDialog(this);
    }

    private void onShareSensorFailure(List<SensorModel> sensors, String username, int index,
            int retryCount) {

        if (retryCount < 3) {
            // retry
            retryCount++;
            shareSensor(sensors, username, index, retryCount);

        } else {
            // give up
            Dispatcher.forwardEvent(SensorShareEvents.ShareFailed);
        }
    }

    private void onShareSensorSuccess(String response, List<SensorModel> sensors, int index,
            String username) {

        // parse list of users from the response
        List<UserModel> users = new ArrayList<UserModel>();
        if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
            GetGroupUsersResponseJso jso = JsonUtils.unsafeEval(response);
            users = jso.getUsers();
        }

        // update the sensor model
        SensorModel sensor = sensors.get(index);
        sensor.set(SensorModel.USERS, users);

        index++;

        shareSensor(sensors, username, index, 0);
    }

    /**
     * Does request to share a list of sensors with a user. If there are multiple sensors in the
     * list, this method calls itself for each sensor in the list.
     * 
     * @param event
     *            AppEvent with "sensors" and "user" properties
     */
    private void shareSensor(List<SensorModel> sensors, String username, int index, int retryCount) {

        if (null != sensors && index < sensors.size()) {
            // get first sensor from the list
            SensorModel sensor = sensors.get(index);

            // prepare request properties
            final String method = "POST";
            final String url = Urls.SENSORS + "/" + sensor.getId() + "/users.json";
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final String body = "{\"user\":{\"username\":\"" + username + "\"}}";
            final AppEvent onSuccess = new AppEvent(SensorShareEvents.ShareAjaxSuccess);
            onSuccess.setData("sensors", sensors);
            onSuccess.setData("user", username);
            onSuccess.setData("index", index);
            final AppEvent onFailure = new AppEvent(SensorShareEvents.ShareAjaxFailure);
            onFailure.setData("sensors", sensors);
            onFailure.setData("user", username);
            onFailure.setData("index", index);
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
