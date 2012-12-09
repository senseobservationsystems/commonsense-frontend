package nl.sense_os.commonsense.main.client.sensormanagement.deleting;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.lib.client.communication.CommonSenseClient;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.sensormanagement.deleting.component.GxtConfirmRemovalDialog;
import nl.sense_os.commonsense.main.client.sensormanagement.deleting.component.GxtRemovalCompleteDialog;
import nl.sense_os.commonsense.main.client.sensormanagement.deleting.component.GxtRemovalFailedDialog;
import nl.sense_os.commonsense.shared.client.util.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public class SensorDeleter implements ConfirmRemovalView.Presenter {

    private static final Logger LOG = Logger.getLogger(SensorDeleter.class.getName());
    private List<GxtSensor> sensors;
    private ConfirmRemovalView confirmationDialog;
    private RemovalCompleteView successDialog;
    private RemovalFailedView failureDialog;

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
    private void delete(final List<GxtSensor> sensors, final int index, final int retryCount) {

        if (index < sensors.size()) {
            GxtSensor sensor = sensors.get(index);

            // prepare request callback
            RequestCallback callback = new RequestCallback() {

                @Override
                public void onError(Request request, Throwable exception) {
                    onDeleteError(-1, exception, sensors, index, retryCount);
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    int statusCode = response.getStatusCode();
                    if (Response.SC_OK == statusCode) {
                        onDeleteSuccess(sensors, index);
                    } else if (Response.SC_NOT_FOUND == statusCode) {
                        // already deleted?
                        onDeleteSuccess(sensors, index);
                    } else {
                        onDeleteError(statusCode, new Throwable(response.getStatusText()), sensors,
                                index, retryCount);
                    }
                }
            };

            // send request
            CommonSenseClient.getClient().deleteSensor(callback, sensor.getId());

        } else {
            // done!
            onDeleteComplete();
        }
    }

    private String getConfirmationText() {
        String message = "Are you sure you want to remove the selected sensor from your list?";
        if (sensors.size() > 1) {
            message = "Are you sure you want to remove all " + sensors.size()
                    + " selected sensors from your list?";
        }
        message += "<br><br>";
        if (sensors.size() > 1) {
            message += "Warning: the removal can not be undone! Any data you stored for these sensors will be lost. Forever.";
        } else {
            message += "Warning: the removal can not be undone! Any data you stored for this sensor will be lost. Forever.";
        }
        return message;
    }

    @Override
    public void onCancelClick() {
        confirmationDialog.hide();
        if (null != successDialog) {
            successDialog.hide();
            successDialog = null;
        }
        if (null != failureDialog) {
            failureDialog.hide();
            failureDialog = null;
        }
    }

    @Override
    public void onDeleteClick() {
        confirmationDialog.setBusy(true);

        if (null != failureDialog) {
            failureDialog.hide();
            failureDialog = null;
        }

        delete(sensors, 0, 0);
    }

    private void onDeleteComplete() {
        confirmationDialog.setBusy(false);
        confirmationDialog.hide();

        // show info
        successDialog = new GxtRemovalCompleteDialog();
        successDialog.setPresenter(this);
        successDialog.show();
    }

    /**
     * Handles a failed delete request. Retries the request up to three times.
     * 
     * @param code
     * @param error
     * @param sensors
     *            List of sensors that have to be deleted.
     * @param index
     * @param retryCount
     *            Number of times this request was attempted.
     */
    private void onDeleteError(int code, Throwable error, List<GxtSensor> sensors, int index,
            int retryCount) {
        LOG.warning("Failed to delete sensor! Error: " + code + " " + error.getMessage());

        if (retryCount < 3) {
            // retry
            retryCount++;
            delete(sensors, index, retryCount);
        } else {
            // give up
            onDeleteFailure(code, error);
        }
    }

    private void onDeleteFailure(int code, Throwable error) {
        confirmationDialog.setBusy(false);

        // show prompt to retry
        failureDialog = new GxtRemovalFailedDialog();
        failureDialog.setPresenter(this);
        failureDialog.show(code, error);
    }

    /**
     * Handles a successful delete request. Removes the deleted sensor from the list, and calls back
     * to {@link #delete(List, int)}.
     * 
     * @param sensors
     *            List of sensors that have to be deleted.
     */
    private void onDeleteSuccess(List<GxtSensor> sensors, int index) {

        // remove the sensor from the cached library
        boolean removed = Registry.<List<GxtSensor>> get(Constants.REG_SENSOR_LIST).remove(
                sensors.get(index));
        if (!removed) {
            LOG.warning("Failed to remove the sensor from the library!");
        }

        // continue with the rest of the list
        index++;
        delete(sensors, index, 0);
    }

    public void start(List<GxtSensor> sensors) {
        this.sensors = sensors;
        confirmationDialog = new GxtConfirmRemovalDialog();
        confirmationDialog.setConfirmationText(getConfirmationText());
        confirmationDialog.setPresenter(this);
        confirmationDialog.show();
    }
}
