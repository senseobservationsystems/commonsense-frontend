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
    private View removeDialog;

    public SensorsController() {
        registerEventTypes(SensorsEvents.ShowRemoveDialog, SensorsEvents.AjaxDeleteSuccess,
                SensorsEvents.DeleteFailure, SensorsEvents.DeleteSuccess,
                SensorsEvents.DeleteRequest, SensorsEvents.AjaxDeleteFailure);
    }

    private void deleteSensors(List<SensorModel> sensors, int retryCount) {

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

    private void deleteSensorsCallback(AppEvent event) {
        // Goodbye sensor!
        List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
        sensors.remove(0);

        // continue with the rest of the list
        deleteSensors(sensors, 0);
    }

    private void deletesSensorErrorCallback(AppEvent event) {

        List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
        int retryCount = event.<Integer> getData("retry");
        if (retryCount < 3) {
            retryCount++;
            deleteSensors(sensors, retryCount);
        } else {
            Dispatcher.forwardEvent(SensorsEvents.DeleteFailure);
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(SensorsEvents.DeleteRequest)) {
            Log.d(TAG, "DeleteRequest");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            deleteSensors(sensors, 0);

        } else if (type.equals(SensorsEvents.AjaxDeleteSuccess)) {
            // Log.d(TAG, "AjaxDeleteSuccess");
            deleteSensorsCallback(event);

        } else if (type.equals(SensorsEvents.AjaxDeleteFailure)) {
            Log.w(TAG, "AjaxDeleteFailure");
            deletesSensorErrorCallback(event);

        } else {
            forwardToView(this.removeDialog, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.removeDialog = new RemoveDialog(this);
    }

}
