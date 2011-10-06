package nl.sense_os.commonsense.client.states.defaults;

import java.util.List;
import java.util.logging.Logger;

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
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;

public class StateDefaultsController extends Controller {

    private static final Logger LOG = Logger.getLogger(StateDefaultsController.class.getName());
    private View dialog;

    public StateDefaultsController() {
        registerEventTypes(StateDefaultsEvents.CheckDefaults,
                StateDefaultsEvents.CheckDefaultsRequest, StateDefaultsEvents.CheckDefaultsSuccess);
    }

    private void checkDefaults(List<DeviceModel> devices, boolean overwrite) {

        // prepare request properties
        final Method method = RequestBuilder.POST;
        final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
        urlBuilder.setPath(Urls.PATH_STATES + "/default/check.json");
        final String url = urlBuilder.buildString();
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);

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

        // prepare request callback
        RequestCallback reqCallback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                LOG.warning("POST default services onError callback: " + exception.getMessage());
                onCheckDefaultsFailure();
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("POST default services response received: " + response.getStatusText());
                int statusCode = response.getStatusCode();
                if (Response.SC_OK == statusCode) {
                    onCheckDefaultsSuccess(response.getText());
                } else {
                    LOG.warning("POST default services returned incorrect status: " + statusCode);
                    onCheckDefaultsFailure();
                }
            }
        };

        // send request
        RequestBuilder builder = new RequestBuilder(method, url);
        builder.setHeader("X-SESSION_ID", sessionId);
        builder.setHeader("Content-Type", Urls.HEADER_JSON_TYPE);
        try {
            builder.sendRequest(body, reqCallback);
        } catch (RequestException e) {
            LOG.warning("POST default services request threw exception: " + e.getMessage());
            onCheckDefaultsFailure();
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(StateDefaultsEvents.CheckDefaultsRequest)) {
            LOG.fine("CheckDefaultsRequest");
            List<DeviceModel> devices = event.getData("devices");
            boolean overwrite = event.getData("overwrite");
            checkDefaults(devices, overwrite);

        } else

        /*
         * Pass on to view
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

    private void onCheckDefaultsFailure() {
        forwardToView(this.dialog, new AppEvent(StateDefaultsEvents.CheckDefaultsFailure));
    }

    private void onCheckDefaultsSuccess(String response) {
        Dispatcher.forwardEvent(StateDefaultsEvents.CheckDefaultsSuccess);
    }

}
