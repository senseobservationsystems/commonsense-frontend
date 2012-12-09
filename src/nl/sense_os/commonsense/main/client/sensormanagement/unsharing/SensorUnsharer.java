package nl.sense_os.commonsense.main.client.sensormanagement.unsharing;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.lib.client.communication.CommonSenseClient;
import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.model.GxtUser;
import nl.sense_os.commonsense.main.client.sensormanagement.unsharing.ConfirmUnshareView.Presenter;
import nl.sense_os.commonsense.main.client.sensormanagement.unsharing.component.GxtConfirmUnshareDialog;
import nl.sense_os.commonsense.main.client.sensormanagement.unsharing.component.GxtUnsharingCompleteDialog;
import nl.sense_os.commonsense.main.client.sensormanagement.unsharing.component.GxtUnsharingFailureDialog;
import nl.sense_os.commonsense.shared.client.util.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public class SensorUnsharer implements Presenter {

    private static final Logger LOG = Logger.getLogger(SensorUnsharer.class.getName());
    private List<GxtSensor> sensors;
    private ConfirmUnshareView confirmDialog;
    private UnsharingFailureView failureDialog;
    private UnsharingCompleteView successDialog;

    public SensorUnsharer(MainClientFactory clientFactory) {

    }

    public void start(List<GxtSensor> sensors) {
        this.sensors = sensors;

        confirmDialog = new GxtConfirmUnshareDialog();
        confirmDialog.setPresenter(this);
        confirmDialog.show();
    }

    @Override
    public void onCancelClick() {
        confirmDialog.setBusy(false);
        confirmDialog.hide();

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
    public void onSubmitClick() {
        confirmDialog.setBusy(false);

        if (null != failureDialog) {
            failureDialog.hide();
            failureDialog = null;
        }

        GxtSensor sensor = sensors.get(0);
        List<GxtUser> users = sensor.getUsers();
        unshare(sensor, users, 0);
    }

    private void onUnshareComplete(GxtSensor sensor) {

        // update library
        List<GxtSensor> library = Registry.get(Constants.REG_SENSOR_LIST);
        int index = library.indexOf(sensor);
        if (index != -1) {
            LOG.fine("Updating sensor's users in the library");
            library.get(index).setUsers(null);
            library.get(index).setUsers(sensor.getUsers());
        } else {
            LOG.warning("Cannot find the unshared sensor in the library!");
        }

        confirmDialog.setBusy(false);
        confirmDialog.hide();

        // show info
        successDialog = new GxtUnsharingCompleteDialog();
        successDialog.setPresenter(this);
        successDialog.show();
    }

    private void onUnshareFailure(int code, Throwable error) {

        confirmDialog.setBusy(false);

        // show error
        failureDialog = new GxtUnsharingFailureDialog();
        failureDialog.setPresenter(this);
        failureDialog.show(code, error);
    }

    private void onUnshareSuccess(String response, GxtSensor sensor, List<GxtUser> users, int index) {
        // update the sensor model
        List<GxtUser> sensorUsers = sensor.getUsers();
        sensorUsers.remove(users.get(index));
        sensor.setUsers(sensorUsers);

        // continue with the next user to remove
        index++;
        unshare(sensor, users, index);
    }

    private void unshare(final GxtSensor sensor, final List<GxtUser> users, final int index) {

        if (index < users.size()) {
            GxtUser user = users.get(index);

            GxtUser currentUser = Registry
                    .<GxtUser> get(nl.sense_os.commonsense.shared.client.util.Constants.REG_USER);
            if (currentUser.equals(user)) {
                LOG.finest("Skipped unsharing with the current user...");
                unshare(sensor, users, index + 1);
                return;
            }
            LOG.fine("Unsharing " + sensor + " with " + user);

            // prepare request callback
            RequestCallback callback = new RequestCallback() {

                @Override
                public void onError(Request request, Throwable exception) {
                    onUnshareFailure(-1, exception);
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    int statusCode = response.getStatusCode();
                    if (Response.SC_OK == statusCode) {
                        onUnshareSuccess(response.getText(), sensor, users, index);
                    } else {
                        onUnshareFailure(statusCode, new Throwable(response.getStatusText()));
                    }
                }
            };

            // send request
            CommonSenseClient.getClient().unshareSensor(callback, sensor.getId(), user.getId());

        } else {
            onUnshareComplete(sensor);
        }
    }
}
