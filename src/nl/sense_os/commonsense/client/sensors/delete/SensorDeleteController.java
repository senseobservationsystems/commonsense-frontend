package nl.sense_os.commonsense.client.sensors.delete;

import java.util.List;

import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
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

public class SensorDeleteController extends Controller {

    private final static String TAG = "DeleteController";
    private View deleteDialog;

    public SensorDeleteController() {
        registerEventTypes(SensorDeleteEvents.ShowDeleteDialog,
                SensorDeleteEvents.DeleteAjaxSuccess, SensorDeleteEvents.DeleteAjaxFailure,
                SensorDeleteEvents.DeleteRequest, SensorDeleteEvents.DeleteSuccess,
                SensorDeleteEvents.DeleteFailure);
    }

    /**
     * Deletes a list of sensors, using Ajax requests to CommonSense.
     * 
     * @param sensors
     *            The list of sensors that have to be deleted.
     * @param index
     *            List index of the current sensor to be deleted.
     * @param retryCount
     *            Counter for failed requests that were retried.
     */
    private void delete(List<SensorModel> sensors, int index, int retryCount) {

        if (index < sensors.size()) {
            ModelData sensor = sensors.get(index);

            // prepare request properties
            final String method = "DELETE";
            final String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id");
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(SensorDeleteEvents.DeleteAjaxSuccess);
            onSuccess.setData("sensors", sensors);
            onSuccess.setData("index", index);
            final AppEvent onFailure = new AppEvent(SensorDeleteEvents.DeleteAjaxFailure);
            onFailure.setData("sensors", sensors);
            onFailure.setData("index", index);
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
            // done!
            Dispatcher.forwardEvent(SensorDeleteEvents.DeleteSuccess);
        }
    }

    /**
     * Handles a successful delete request. Removes the deleted sensor from the list, and calls back
     * to {@link #delete(List, int)}.
     * 
     * @param sensors
     *            List of sensors that have to be deleted.
     */
    private void deleteCallback(List<SensorModel> sensors, int index) {
        // continue with the rest of the list
        index++;
        delete(sensors, index, 0);
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
    private void deleteFailure(List<SensorModel> sensors, int index, int retryCount) {

        if (retryCount < 3) {
            // retry
            retryCount++;
            delete(sensors, index, retryCount);
        } else {
            // give up
            Dispatcher.forwardEvent(SensorDeleteEvents.DeleteFailure);
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(SensorDeleteEvents.DeleteRequest)) {
            // Log.d(TAG, "DeleteRequest");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            delete(sensors, 0, 0);

        } else if (type.equals(SensorDeleteEvents.DeleteAjaxSuccess)) {
            // Log.d(TAG, "AjaxDeleteSuccess");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final int index = event.<Integer> getData("index");
            deleteCallback(sensors, index);

        } else if (type.equals(SensorDeleteEvents.DeleteAjaxFailure)) {
            Log.w(TAG, "AjaxDeleteFailure");
            // final int code = event.getData("code");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final int index = event.<Integer> getData("index");
            final int retryCount = event.<Integer> getData("retry");
            deleteFailure(sensors, index, retryCount);

        } else

        /*
         * Pass through to View
         */
        {
            forwardToView(this.deleteDialog, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.deleteDialog = new SensorDeleteDialog(this);
    }

}
