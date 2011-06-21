package nl.sense_os.commonsense.client.sensors.share;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

public class SensorShareController extends Controller {

    private final static Logger LOG = Logger.getLogger(SensorShareController.class.getName());
    private View shareDialog;

    public SensorShareController() {
        registerEventTypes(SensorShareEvents.ShowShareDialog, SensorShareEvents.ShareRequest,
                SensorShareEvents.ShareComplete, SensorShareEvents.ShareCancelled,
                SensorShareEvents.ShareFailed);
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(SensorShareEvents.ShareRequest)) {
            LOG.finest("ShareRequest");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final String user = event.<String> getData("user");
            shareSensor(sensors, user, 0, 0);

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
        sensor.setUsers(users);

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
    private void shareSensor(final List<SensorModel> sensors, final String username,
            final int index, final int retryCount) {

        if (null != sensors && index < sensors.size()) {
            // get first sensor from the list
            SensorModel sensor = sensors.get(index);

            // prepare request properties
            final Method method = RequestBuilder.POST;
            final String url = Urls.SENSORS + "/" + sensor.getId() + "/users.json";
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final String body = "{\"user\":{\"username\":\"" + username + "\"}}";

            // prepare request callback
            RequestCallback reqCallback = new RequestCallback() {

                @Override
                public void onError(Request request, Throwable exception) {
                    LOG.warning("POST sensor user onError callback: " + exception.getMessage());
                    onShareSensorFailure(sensors, username, index, retryCount);
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    LOG.finest("POST sensor user received: " + response.getStatusText());
                    int statusCode = response.getStatusCode();
                    if (Response.SC_CREATED == statusCode) {
                        onShareSensorSuccess(response.getText(), sensors, index, username);
                    } else {
                        LOG.warning("POST sensor user returned incorrect status: " + statusCode);
                        onShareSensorFailure(sensors, username, index, retryCount);
                    }
                }
            };

            // send request
            RequestBuilder builder = new RequestBuilder(method, url);
            builder.setHeader("X-SESSION_ID", sessionId);
            try {
                builder.sendRequest(body, reqCallback);
            } catch (RequestException e) {
                LOG.warning("POST sensor user request threw exception: " + e.getMessage());
                onShareSensorFailure(sensors, username, index, retryCount);
            }

        } else {
            // done
            onShareSuccess(sensors);
        }
    }

    private void onShareSuccess(List<SensorModel> sensors) {

        // update library
        for (SensorModel sensor : sensors) {
            List<SensorModel> library = Registry.get(Constants.REG_SENSOR_LIST);
            int index = library.indexOf(sensor);
            if (index != -1) {
                LOG.fine("Updating sensor users in the library");
                library.get(index).setUsers(sensor.getUsers());
            } else {
                LOG.warning("Cannot find the newly shared sensor in the library!");
            }
        }

        // dispatch event
        Dispatcher.forwardEvent(SensorShareEvents.ShareComplete);
    }
}
