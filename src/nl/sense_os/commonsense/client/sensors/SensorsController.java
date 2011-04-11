package nl.sense_os.commonsense.client.sensors;

import java.util.List;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;

public class SensorsController extends Controller {

    private static final String TAG = "SensorsController";
    private View deleteDialog;
    private View unshareDialog;

    public SensorsController() {
        registerEventTypes(SensorsEvents.ShowDeleteDialog, SensorsEvents.AjaxDeleteSuccess,
                SensorsEvents.DeleteFailure, SensorsEvents.DeleteSuccess,
                SensorsEvents.DeleteRequest, SensorsEvents.AjaxDeleteFailure);
        registerEventTypes(SensorsEvents.ShowUnshareDialog, SensorsEvents.AjaxUnshareSuccess,
                SensorsEvents.UnshareFailure, SensorsEvents.UnshareSuccess,
                SensorsEvents.UnshareRequest, SensorsEvents.AjaxUnshareFailure);
    }

    /**
     * Deletes a list of sensors, using Ajax requests to CommonSense.
     * 
     * @param sensors
     *            The list of sensors that have to be deleted.
     * @param retryCount
     *            Counter for failed requests that were retried.
     */
    private void delete(List<SensorModel> sensors, int retryCount) {

        if (null != sensors && sensors.size() > 0) {
            ModelData sensor = sensors.get(0);

            // prepare request properties
            final String method = "DELETE";
            final String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id");
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(SensorsEvents.AjaxDeleteSuccess);
            onSuccess.setData("sensors", sensors);
            final AppEvent onFailure = new AppEvent(SensorsEvents.AjaxDeleteFailure);
            onFailure.setData("sensors", sensors);
            onFailure.setData("retry", retryCount);

            // send request to AjaxController
            final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
            ajaxRequest.setData("method", method);
            ajaxRequest.setData("url", url);
            ajaxRequest.setData("session_id", sessionId);
            ajaxRequest.setData("onSuccess", onSuccess);
            ajaxRequest.setData("onFailure", onFailure);
            Dispatcher.forwardEvent(ajaxRequest);
        } else {
            Dispatcher.forwardEvent(SensorsEvents.DeleteSuccess);
        }
    }

    /**
     * Handles a successful delete request. Removes the deleted sensor from the list, and calls back
     * to {@link #delete(List, int)}.
     * 
     * @param sensors
     *            List of sensors that have to be deleted.
     */
    private void deleteCallback(List<SensorModel> sensors) {
        // Goodbye sensor!
        sensors.remove(0);

        // continue with the rest of the list
        delete(sensors, 0);
    }

    /**
     * Handles a failed delete request. Retries the request up to three times, after this it gives
     * up and dispatches {@link SensorsEvents#DeleteFailure}.
     * 
     * @param sensors
     *            List of sensors that have to be deleted.
     * @param retryCount
     *            Number of times this request was attempted.
     */
    private void deleteFailure(List<SensorModel> sensors, int retryCount) {

        if (retryCount < 3) {
            retryCount++;
            delete(sensors, retryCount);
        } else {
            Dispatcher.forwardEvent(SensorsEvents.DeleteFailure);
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(SensorsEvents.DeleteRequest)) {
            // Log.d(TAG, "DeleteRequest");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            delete(sensors, 0);

        } else if (type.equals(SensorsEvents.AjaxDeleteSuccess)) {
            // Log.d(TAG, "AjaxDeleteSuccess");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            deleteCallback(sensors);

        } else if (type.equals(SensorsEvents.AjaxDeleteFailure)) {
            Log.w(TAG, "AjaxDeleteFailure");
            // final int code = event.getData("code");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final int retryCount = event.<Integer> getData("retry");
            deleteFailure(sensors, retryCount);

        } else if (type.equals(SensorsEvents.UnshareRequest)) {
            // Log.d(TAG, "UnshareRequest");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            unshare(sensors, 0);

        } else if (type.equals(SensorsEvents.AjaxUnshareSuccess)) {
            // Log.d(TAG, "AjaxUnshareSuccess");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            unshareCallback(sensors);

        } else if (type.equals(SensorsEvents.AjaxUnshareFailure)) {
            Log.w(TAG, "AjaxUnshareFailure");
            // final int code = event.getData("code");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final int retryCount = event.<Integer> getData("retry");
            unshareFailure(sensors, retryCount);

        } else if (type.equals(SensorsEvents.ShowDeleteDialog)
                || type.equals(SensorsEvents.DeleteSuccess)
                || type.equals(SensorsEvents.DeleteFailure)) {
            forwardToView(this.deleteDialog, event);

        } else if (type.equals(SensorsEvents.ShowUnshareDialog)
                || type.equals(SensorsEvents.UnshareSuccess)
                || type.equals(SensorsEvents.UnshareFailure)) {
            forwardToView(this.unshareDialog, event);

        } else {
            Log.w(TAG, "Unexpected event type");
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.deleteDialog = new DeleteDialog(this);
        this.unshareDialog = new UnshareDialog(this);
    }

    /**
     * Unshares a list of sensors from certain users, using Ajax requests to CommonSense.
     * 
     * @param sensors
     *            The list of sensors that have to be unshared. The sensors must have a "user"
     *            property, containing the ID of the user to unshare.
     * @param retryCount
     *            Counter for failed requests that were retried.
     */
    private void unshare(List<SensorModel> sensors, int retryCount) {
        if (null != sensors && sensors.size() > 0) {
            ModelData sensor = sensors.get(0);

            // get the user that we want to unshare the sensor with
            String userId = sensor.get("user");

            // prepare request properties
            final String method = "DELETE";
            final String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id") + "/users/"
                    + userId;
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(SensorsEvents.AjaxUnshareSuccess);
            onSuccess.setData("sensors", sensors);
            final AppEvent onFailure = new AppEvent(SensorsEvents.AjaxUnshareFailure);
            onFailure.setData("sensors", sensors);
            onFailure.setData("retry", retryCount);

            // send request to AjaxController
            final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
            ajaxRequest.setData("method", method);
            ajaxRequest.setData("url", url);
            ajaxRequest.setData("session_id", sessionId);
            ajaxRequest.setData("onSuccess", onSuccess);
            ajaxRequest.setData("onFailure", onFailure);
            Dispatcher.forwardEvent(ajaxRequest);
        } else {
            Dispatcher.forwardEvent(SensorsEvents.UnshareSuccess);
        }
    }

    private void unshareCallback(List<SensorModel> sensors) {
        // Goodbye sensor!
        sensors.remove(0);

        // continue with the rest of the list
        unshare(sensors, 0);
    }

    /**
     * Handles a failed unshare request. Retries the request up to three times, after this it gives
     * up and dispatches {@link SensorsEvents#UnshareFailure}.
     * 
     * @param sensors
     *            List of sensors that have to be unshared.
     * @param retryCount
     *            Number of times this request was attempted.
     */
    private void unshareFailure(List<SensorModel> sensors, int retryCount) {

        if (retryCount < 3) {
            retryCount++;
            unshare(sensors, retryCount);
        } else {
            Dispatcher.forwardEvent(SensorsEvents.UnshareFailure);
        }
    }

}
