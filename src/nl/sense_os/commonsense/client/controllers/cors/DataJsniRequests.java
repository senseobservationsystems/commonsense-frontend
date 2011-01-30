package nl.sense_os.commonsense.client.controllers.cors;

import java.util.Date;
import java.util.HashMap;

import nl.sense_os.commonsense.client.controllers.VizController;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.TagModel;
import nl.sense_os.commonsense.shared.sensorvalues.BooleanValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.FloatValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.JsonValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.SensorValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.StringValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.TaggedDataModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

/**
 * Helper class for cross-origin data requests using the JSNI. Has strict ties to
 * {@link VizController}.
 */
public class DataJsniRequests {

    private static final String TAG = "DataJsniRequests";

    /**
     * Requests sensor data from CommonSense. Calls back to
     * {@link #parseData(String, TreeModel, SensorValueModel[], int, VizController, String)},
     * {@link VizController#onDataAuthError()}, or {@link VizController#onDataFailed(TreeModel)}.
     * 
     * @param url
     *            URL of /users/current GET method
     * @param sessionId
     *            session ID for authentication
     * @param sensor
     *            TreeModel of the sensor that the data is requested for. Passed to the handler to
     *            make it possible to do recursive data requests if the number of data points
     *            exceeds the maximum page size.
     * @param page
     *            the current page number
     * @param pagedValues
     *            the sensor values that have been fetched in previous pages
     * @param handler
     *            VizController object to handle the callbacks
     * @see <a href="http://goo.gl/ajJWN">Making cross domain JavaScript requests</a>
     */
    // @formatter:off
    public static native void requestData(String url, String sessionId, TreeModel sensor,
            int page, SensorValueModel[] pagedValues, VizController handler) /*-{
        var isIE8 = window.XDomainRequest ? true : false;
        var xhr = createCrossDomainRequest();

        function createCrossDomainRequest() {
            if (isIE8) { return new window.XDomainRequest(); } 
            else { return new XMLHttpRequest(); }
        }

        function readyStateHandler() {
            if (xhr.readyState == 4) {
                if (xhr.status == 200) { outputResult(); } 
                else if (xhr.status == 403) { outputAuthentication(); } 
                else { outputError(); }
            }
        }

        function outputAuthentication() {
            handler.@nl.sense_os.commonsense.client.controllers.VizController::onDataAuthError()();
        }

        function outputError() {
            handler.@nl.sense_os.commonsense.client.controllers.VizController::onDataFailed(Lcom/extjs/gxt/ui/client/data/TreeModel;)(sensor);
        }

        function outputResult() {
            @nl.sense_os.commonsense.client.controllers.cors.DataJsniRequests::parseData(Ljava/lang/String;Lcom/extjs/gxt/ui/client/data/TreeModel;[Lnl/sense_os/commonsense/shared/sensorvalues/SensorValueModel;ILnl/sense_os/commonsense/client/controllers/VizController;Ljava/lang/String;)(xhr.responseText,sensor,pagedValues,page,handler,url);
        }

        if (xhr) {
            if (isIE8) {
                url = url + "&session_id=" + sessionId;
                xhr.open("GET", url);
                xhr.onload = outputResult;
                xhr.onerror = outputError;
                xhr.send();
            } else {
                xhr.open('GET', url, true);
                xhr.onreadystatechange = readyStateHandler;
                xhr.setRequestHeader("X-SESSION_ID",sessionId);
                xhr.send();
            }
        } else {
            outputError();
        }
    }-*/;
    // @formatter:on

    private static void parseData(String response, TreeModel tag, SensorValueModel[] pagedValues,
            int page, VizController handler, String url) {
        SensorValueModel[] values = null;
        try {
            JSONObject obj = JSONParser.parseStrict(response).isObject();
            JSONArray data = obj.get("data").isArray();

            Log.d(TAG, "Received " + data.size() + " sensor data points");

            values = new SensorValueModel[data.size()];
            JSONObject datapoint;
            double decimalTime;
            Date timestamp;
            String rawValue;
            String cleanValue;
            SensorValueModel value;
            for (int i = 0; i < data.size(); i++) {

                datapoint = data.get(i).isObject();

                // parse time
                decimalTime = Double.parseDouble(datapoint.get("date").isString().stringValue());
                timestamp = new Date((long) (decimalTime * 1000));

                // get value (always a String initially)
                rawValue = datapoint.get("value").isString().stringValue();
                cleanValue = rawValue.replaceAll("//", "");

                if ((cleanValue.charAt(0) == '{')
                        && (cleanValue.charAt(cleanValue.length() - 1) == '}')) {
                    JSONObject jsonValue = JSONParser.parseStrict(cleanValue).isObject();
                    if (null != jsonValue) {
                        // Log.d(TAG, "JsonValue");

                        HashMap<String, Object> fields = new HashMap<String, Object>();
                        for (String fieldKey : jsonValue.keySet()) {
                            JSONValue fieldValue = jsonValue.get(fieldKey);

                            JSONNumber numberField = fieldValue.isNumber();
                            if (null != numberField) {
                                fields.put(fieldKey, numberField.doubleValue());
                                continue;
                            }

                            JSONString stringField = fieldValue.isString();
                            if (null != stringField) {
                                fields.put(fieldKey, stringField.stringValue());
                                continue;
                            }
                            fields.put(fieldKey, fieldValue.toString());
                        }
                        value = new JsonValueModel(timestamp, fields);
                        values[i] = value;
                        continue;
                    }
                }

                try {
                    double doubleValue = Double.parseDouble(cleanValue);
                    // Log.d(TAG, "FloatValue");
                    value = new FloatValueModel(timestamp, doubleValue);
                    values[i] = value;
                    continue;
                } catch (NumberFormatException e) {
                    // do nothing
                }

                boolean boolValue = Boolean.parseBoolean(cleanValue);
                if (!boolValue && cleanValue.equalsIgnoreCase("false")) {
                    // Log.d(TAG, "BooleanValue");
                    value = new BooleanValueModel(timestamp, boolValue);
                    values[i] = value;
                    continue;
                }

                // Log.d(TAG, "StringValue");
                value = new StringValueModel(timestamp, cleanValue);
                values[i] = value;
                continue;
            }

        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException parsing sensor data: " + e.getMessage());
            handler.onDataFailed(tag);
        }

        // append the parsed values to any previous pages of values
        SensorValueModel[] allValues = values;
        if (pagedValues != null) {
            allValues = new SensorValueModel[pagedValues.length + values.length];
            System.arraycopy(pagedValues, 0, allValues, 0, pagedValues.length);
            System.arraycopy(values, 0, allValues, pagedValues.length, values.length);
        }

        if (values.length < 1000) {
            Log.d(TAG, "completed getting all pages of data");

            TagModel mdl = new TagModel(tag.<String> get("name") + "/", 0, 0, TagModel.TYPE_SENSOR);
            TaggedDataModel taggedData = new TaggedDataModel(mdl, allValues);
            handler.onDataReceived(taggedData);
        } else {
            // exactly 1000 values? see if there are more pages
            pagedValues = allValues;
            page++;

            String sessionId = Registry.get(Constants.REG_SESSION_ID);
            url = url.replaceAll("\\?page=\\d+&", "\\?page=" + page + "&");
            Log.d(TAG, "new url: " + url);
            DataJsniRequests.requestData(url, sessionId, tag, page, pagedValues, handler);
        }
    }
}
