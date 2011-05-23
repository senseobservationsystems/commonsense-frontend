package nl.sense_os.commonsense.client.states.defaults;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.models.DeviceModel;
import nl.sense_os.commonsense.client.common.models.SensorModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;

public class StateDefaultsController extends Controller {

    private static final Logger logger = Logger.getLogger("StateDefaultsController");
    private View dialog;

    public StateDefaultsController() {
        registerEventTypes(StateDefaultsEvents.CheckDefaults,
                StateDefaultsEvents.CheckDefaultsRequest, StateDefaultsEvents.AjaxDefaultsSuccess,
                StateDefaultsEvents.AjaxDefaultsFailure, StateDefaultsEvents.CheckDefaultsSuccess);
    }

    private void checkDefaults(List<DeviceModel> devices, boolean overwrite) {

        // prepare request properties
        final String method = "POST";
        final String url = Urls.STATES + "/default/check.json";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(StateDefaultsEvents.AjaxDefaultsSuccess);
        final AppEvent onFailure = new AppEvent(StateDefaultsEvents.AjaxDefaultsFailure);

        // prepare body
        String body = "{\"sensors\":[";
        List<SensorModel> sensors = Registry.get(Constants.REG_SENSOR_LIST);
        for (SensorModel sensor : sensors) {
            DeviceModel sensorDevice = sensor.getDevice();
            if (sensorDevice != null && devices.contains(sensorDevice)) {
                body += "\"" + sensor.getId() + "\",";
            }
        }
        if (body.length() > 1) {
            body = body.substring(0, body.length() - 1);
        }
        body += "],";
        body += "\"update\":\"" + (overwrite ? 1 : 0) + "\"";
        body += "}";

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("body", body);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);

        Dispatcher.forwardEvent(ajaxRequest);
    }

    private void onCheckDefaultsSuccess(String response) {
        Dispatcher.forwardEvent(StateDefaultsEvents.CheckDefaultsSuccess);
    }

    private void onCheckDefaultsFailure() {
        forwardToView(this.dialog, new AppEvent(StateDefaultsEvents.CheckDefaultsFailure));
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(StateDefaultsEvents.CheckDefaultsRequest)) {
            logger.fine("CheckDefaultsRequest");
            List<DeviceModel> devices = event.getData("devices");
            boolean overwrite = event.getData("overwrite");
            checkDefaults(devices, overwrite);

        } else if (type.equals(StateDefaultsEvents.AjaxDefaultsSuccess)) {
            logger.fine("AjaxDefaultsSuccess");
            final String response = event.<String> getData("response");
            onCheckDefaultsSuccess(response);

        } else if (type.equals(StateDefaultsEvents.AjaxDefaultsFailure)) {
            logger.warning("AjaxDefaultsFailure");
            // final int code = event.getData("code");
            onCheckDefaultsFailure();

        } else

        /*
         * Pass on to state tree view
         */
        {
            forwardToView(this.dialog, event);
        }

    }

    @Override
    protected void initialize() {
        super.initialize();
        this.dialog = new StateDefaultsDialog(this);
    }

}
