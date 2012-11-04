package nl.sense_os.commonsense.main.client.sensormanagement.sharing;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.model.GxtUser;
import nl.sense_os.commonsense.main.client.sensormanagement.sharing.ShareWithUserView.Presenter;
import nl.sense_os.commonsense.main.client.sensormanagement.sharing.component.GxtShareWithUserDialog;
import nl.sense_os.commonsense.main.client.sensormanagement.sharing.component.GxtSharingCompleteDialog;
import nl.sense_os.commonsense.main.client.sensormanagement.sharing.component.GxtSharingFailureDialog;
import nl.sense_os.commonsense.shared.client.communication.CommonSenseApi;
import nl.sense_os.commonsense.shared.client.communication.httpresponse.GetGroupUsersResponse;
import nl.sense_os.commonsense.shared.client.model.User;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public class SensorSharer implements Presenter {

    private static final Logger LOG = Logger.getLogger(SensorSharer.class.getName());

    private List<GxtSensor> sensors;

    private ShareWithUserView usernameDialog;
    private SharingCompleteView successDialog;
    private SharingFailureView failureDialog;

    public SensorSharer(MainClientFactory clientFactory) {

    }

    @Override
    public void onCancelClick() {
        usernameDialog.setBusy(false);
        usernameDialog.hide();

        if (null != successDialog) {
            successDialog.hide();
            successDialog = null;
        }

        if (null != failureDialog) {
            failureDialog.hide();
            failureDialog = null;
        }
    }

    private void onShareComplete(List<GxtSensor> sensors) {

        // update library
        for (GxtSensor sensor : sensors) {
            List<GxtSensor> library = Registry
                    .get(nl.sense_os.commonsense.shared.client.util.Constants.REG_SENSOR_LIST);
            int index = library.indexOf(sensor);
            if (index != -1) {
                LOG.fine("Updating sensor users in the library");
                library.get(index).setUsers(sensor.getUsers());
            } else {
                LOG.warning("Cannot find the newly shared sensor in the library!");
            }
        }

        // remove the username dialog
        usernameDialog.setBusy(false);
        usernameDialog.hide();

        // inform user of success
        successDialog = new GxtSharingCompleteDialog();
        successDialog.setPresenter(this);
        successDialog.show();
    }

    private void onShareError(int code, Throwable error, List<GxtSensor> sensors, String username,
            int index, int retryCount) {

        if (retryCount < 3) {
            // retry
            retryCount++;
            share(sensors, username, index, retryCount);

        } else {
            onShareFailure(code, error);
        }
    }

    private void onShareFailure(int code, Throwable error) {

        usernameDialog.setBusy(false);

        // inform user of success
        failureDialog = new GxtSharingFailureDialog();
        failureDialog.setPresenter(this);
        failureDialog.show(code, error);
    }

    private void onShareSuccess(String response, List<GxtSensor> sensors, int index, String username) {

        // parse list of users from the response
        List<User> users = new ArrayList<User>();
        if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
            GetGroupUsersResponse jso = JsonUtils.unsafeEval(response);
            users = jso.getUsers();
        }

        // convert to Ext
        List<GxtUser> gxtUsers = new ArrayList<GxtUser>(users.size());
        for (User u : users) {
            gxtUsers.add(new GxtUser(u));
        }

        // update the sensor model
        GxtSensor sensor = sensors.get(index);
        sensor.setUsers(gxtUsers);

        index++;

        share(sensors, username, index, 0);
    }

    @Override
    public void onSubmitClick() {
        usernameDialog.setBusy(true);

        if (null != failureDialog) {
            failureDialog.hide();
            failureDialog = null;
        }

        String username = usernameDialog.getUsername();
        share(sensors, username, 0, 0);
    }

    /**
     * Does request to share a list of sensors with a user. If there are multiple sensors in the
     * list, this method calls itself for each sensor in the list.
     * 
     * @param event
     *            AppEvent with "sensors" and "user" properties
     */
    private void share(final List<GxtSensor> sensors, final String username, final int index,
            final int retryCount) {

        if (null != sensors && index < sensors.size()) {

            // get first sensor from the list
            GxtSensor sensor = sensors.get(index);
            String sensorId = sensor.getId();

            // prepare request callback
            RequestCallback callback = new RequestCallback() {

                @Override
                public void onError(Request request, Throwable exception) {
                    onShareError(-1, exception, sensors, username, index, retryCount);
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    int statusCode = response.getStatusCode();
                    if (Response.SC_CREATED == statusCode) {
                        onShareSuccess(response.getText(), sensors, index, username);
                    } else {
                        onShareError(statusCode, new Throwable(response.getStatusText()), sensors,
                                username, index, retryCount);
                    }
                }
            };

            // send request
            CommonSenseApi.shareSensor(callback, sensorId, username);

        } else {
            // done
            onShareComplete(sensors);
        }
    }

    public void start(List<GxtSensor> sensors) {
        this.sensors = sensors;

        // show dialog to get username
        usernameDialog = new GxtShareWithUserDialog();
        usernameDialog.setPresenter(this);
        usernameDialog.show();
    }
}
