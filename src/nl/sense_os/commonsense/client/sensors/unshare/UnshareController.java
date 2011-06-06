package nl.sense_os.commonsense.client.sensors.unshare;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.common.models.UserModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;

public class UnshareController extends Controller {

    private static final Logger LOGGER = Logger.getLogger(UnshareController.class.getName());
    private View dialog;

    public UnshareController() {
        registerEventTypes(UnshareEvents.ShowUnshareDialog);
        registerEventTypes(UnshareEvents.UnshareRequest, UnshareEvents.UnshareComplete,
                UnshareEvents.UnshareAjaxSuccess, UnshareEvents.UnshareAjaxFailure);
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(UnshareEvents.UnshareRequest)) {
            LOGGER.finest("UnshareRequest");
            SensorModel sensor = event.getData("sensor");
            List<UserModel> users = event.getData("users");
            onUnshareRequest(sensor, users);

        } else if (type.equals(UnshareEvents.UnshareAjaxSuccess)) {
            LOGGER.finest("UnshareAjaxSuccess");
            String response = event.getData("response");
            SensorModel sensor = event.getData("sensor");
            List<UserModel> users = event.getData("users");
            int index = event.getData("index");
            onUnshareSuccess(response, sensor, users, index);

        } else if (type.equals(UnshareEvents.UnshareAjaxFailure)) {
            LOGGER.warning("UnshareAjaxFailure");
            onUnshareFailure();

        } else

        /*
         * Pass through to dialog
         */
        {
            forwardToView(this.dialog, event);
        }

    }

    private void onUnshareFailure() {
        forwardToView(this.dialog, new AppEvent(UnshareEvents.UnshareFailed));
    }

    private void onUnshareSuccess(String response, SensorModel sensor, List<UserModel> users,
            int index) {
        // update the sensor model
        List<UserModel> sensorUsers = sensor.<List<UserModel>> get("users");
        sensorUsers.remove(users.get(index));
        sensor.set(SensorModel.USERS, sensorUsers);

        // continue with the next user to remove
        index++;
        unshare(sensor, users, index);
    }

    private void onUnshareRequest(SensorModel sensor, List<UserModel> users) {
        unshare(sensor, users, 0);
    }

    @Override
    protected void initialize() {
        this.dialog = new UnshareDialog(this);
        super.initialize();
    }

    private void unshare(SensorModel sensor, List<UserModel> users, int index) {

        if (index < users.size()) {
            UserModel user = users.get(index);

            // prepare request properties
            final String method = "DELETE";
            final String url = Urls.SENSORS + "/" + sensor.getId() + "/users/" + user.getId()
                    + ".json";
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(UnshareEvents.UnshareAjaxSuccess);
            onSuccess.setData("sensor", sensor);
            onSuccess.setData("index", index);
            onSuccess.setData("users", users);
            final AppEvent onFailure = new AppEvent(UnshareEvents.UnshareAjaxFailure);

            // send request to AjaxController
            final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
            ajaxRequest.setData("method", method);
            ajaxRequest.setData("url", url);
            ajaxRequest.setData("session_id", sessionId);
            ajaxRequest.setData("onSuccess", onSuccess);
            ajaxRequest.setData("onFailure", onFailure);

            Dispatcher.forwardEvent(ajaxRequest);
        } else {
            Dispatcher.forwardEvent(UnshareEvents.UnshareComplete);
        }
    }

}
