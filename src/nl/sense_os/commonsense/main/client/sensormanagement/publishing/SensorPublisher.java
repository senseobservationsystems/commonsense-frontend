package nl.sense_os.commonsense.main.client.sensormanagement.publishing;

import java.util.List;

import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.model.GxtUser;
import nl.sense_os.commonsense.main.client.sensormanagement.publishing.ConfirmPublicationView.Presenter;
import nl.sense_os.commonsense.main.client.sensormanagement.publishing.component.GxtConfirmPublicationDialog;
import nl.sense_os.commonsense.main.client.sensormanagement.publishing.component.GxtPublicationFailureDialog;
import nl.sense_os.commonsense.main.client.sensormanagement.publishing.component.GxtPublicationSuccessDialog;
import nl.sense_os.commonsense.shared.client.util.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class SensorPublisher implements Presenter {

    private List<GxtSensor> sensors;
    private ConfirmPublicationView confirmDialog;
    private PublicationCompleteView successDialog;
    private PublicationFailureView failureDialog;

    public SensorPublisher(MainClientFactory clientFactory) {

    }

    @Override
    public void onCancelClick() {
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

    private void onPublicationError(int code, Throwable error) {
        confirmDialog.setBusy(false);

        failureDialog = new GxtPublicationFailureDialog();
        failureDialog.setPresenter(this);
        failureDialog.show(code, error);
    }

    private void onPublicationResponse(Response response) {
        if (response.getStatusCode() == Response.SC_OK) {
            if (JsonUtils.safeToEval(response.getText())) {
                JSONObject jso = new JSONObject(JsonUtils.safeEval(response.getText()));
                JSONString status = jso.get("status").isString();
                if (status != null && status.stringValue().equals("200")) {
                    String url = jso.get("url").isString().stringValue();
                    String title = jso.get("url").isString().stringValue();
                    String name = jso.get("name").isString().stringValue();
                    JSONArray sensorArray = jso.get("sensors").isArray();
                    int[] sensorIds = new int[sensorArray.size()];
                    for (int i = 0; i < sensorArray.size(); i++) {
                        String s = sensorArray.get(i).isString().stringValue();
                        sensorIds[i] = Integer.parseInt(s);
                    }
                    onPublicationSuccess(url, title, name, sensorIds);
                } else {
                    onPublicationError(-1,
                            new Throwable("Unexpected response: '" + response.getText() + "'"));
                }
            } else {
                onPublicationError(-1,
                        new Throwable("Unable to parse response: '" + response.getText() + "'"));
            }
        } else {
            onPublicationError(response.getStatusCode(), new Throwable(response.getStatusText()));
        }
    }

    private void onPublicationSuccess(String url, String title, String name, int[] sensorIds) {
        confirmDialog.setBusy(false);

        successDialog = new GxtPublicationSuccessDialog();
        successDialog.setPresenter(this);
        successDialog.setInfo(url, title, name, sensorIds);
        successDialog.show();
    }

    @Override
    public void onPublishClick() {
        confirmDialog.setBusy(true);

        if (null != failureDialog) {
            failureDialog.hide();
            failureDialog = null;
        }

        GxtUser user = Registry.get(Constants.REG_USER);
        publish(user, sensors, confirmDialog.isAnonymous());
    }

    /**
     * Sends request to publish list of sensors to the Rotterdam Open Data Store.
     * 
     * @param sensors
     *            Sensors to publish
     * @param anonymous
     *            Boolean to select anonymous publication
     */
    private void publish(GxtUser user, List<GxtSensor> sensors, boolean anonymous) {

        // prepare request data
        String uuid = user.getUuid();
        JSONObject json = new JSONObject();
        json.put("username", new JSONString(user.getUsername()));
        json.put("uuid", new JSONString(uuid));
        json.put("anonymous", JSONBoolean.getInstance(anonymous));
        JSONArray array = new JSONArray();
        for (int i = 0; i < sensors.size(); i++) {
            GxtSensor sensor = sensors.get(i);
            JSONObject sensorJson = new JSONObject();
            sensorJson.put("id", new JSONString(sensor.getId()));
            sensorJson.put("name", new JSONString(sensor.getName()));
            sensorJson.put("description", new JSONString(sensor.getDescription()));
            array.set(i, sensorJson);
        }
        json.put("sensors", array);
        String data = json.toString();

        // prepare callback
        RequestCallback callback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                int code = -1;
                onPublicationError(code, exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                onPublicationResponse(response);
            }
        };

        // prepare request details
        Method method = RequestBuilder.POST;
        String url = "/rod/cs.php";

        // send request
        try {
            RequestBuilder builder = new RequestBuilder(method, url);
            builder.setHeader("Content-Type", "application/json");
            builder.setHeader("Accept", "application/json");
            builder.sendRequest(data, callback);
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }

    public void start(List<GxtSensor> sensors) {
        this.sensors = sensors;

        confirmDialog = new GxtConfirmPublicationDialog();
        confirmDialog.setNumberOfSensors(sensors.size());
        confirmDialog.setPresenter(this);
        confirmDialog.show();
    }
}
