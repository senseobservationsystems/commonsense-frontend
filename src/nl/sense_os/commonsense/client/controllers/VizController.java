package nl.sense_os.commonsense.client.controllers;

import java.util.Date;
import java.util.HashMap;

import nl.sense_os.commonsense.client.events.LoginEvents;
import nl.sense_os.commonsense.client.events.MainEvents;
import nl.sense_os.commonsense.client.events.VizEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.views.VizTypeChooser;
import nl.sense_os.commonsense.client.views.VizView;
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
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;
import com.google.gwt.visualization.client.VisualizationUtils;

public class VizController extends Controller {

    private static final String TAG = "VizController";

    // @formatter:off
    private static native void requestData(String url, String sessionId, TreeModel tag,
            VizController handler) /*-{
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
            handler.@nl.sense_os.commonsense.client.controllers.VizController::handleDataAuthError()();
        }

        function outputError() {
            handler.@nl.sense_os.commonsense.client.controllers.VizController::handleDataFailed(Lcom/extjs/gxt/ui/client/data/TreeModel;)(tag);
        }

        function outputResult() {
            handler.@nl.sense_os.commonsense.client.controllers.VizController::handleDataResponse(Ljava/lang/String;Lcom/extjs/gxt/ui/client/data/TreeModel;Ljava/lang/String;)(xhr.responseText,tag,url);
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

    private VizView vizView;
    private VizTypeChooser typeChooser;
    private boolean isVizApiLoaded;

    public VizController() {
        registerEventTypes(MainEvents.ShowVisualization);
        registerEventTypes(LoginEvents.LoggedIn, LoginEvents.LoggedOut);
        registerEventTypes(VizEvents.DataRequested, VizEvents.DataNotReceived,
                VizEvents.DataReceived);
        registerEventTypes(VizEvents.ShowTypeChoice, VizEvents.TypeChoiceCancelled);
        registerEventTypes(VizEvents.ShowLineChart, VizEvents.ShowTable, VizEvents.ShowMap,
                VizEvents.ShowNetwork);
        loadVizApi();
    }

    private void handleDataAuthError() {
        Dispatcher.forwardEvent(VizEvents.DataNotReceived);
    }

    private void handleDataFailed(TreeModel tag) {
        Dispatcher.forwardEvent(VizEvents.DataNotReceived);
    }

    private void handleDataResponse(String response, TreeModel tag, String url) {
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
            handleDataFailed(tag);
        }

        // append the parsed values to any previous pages of values
        SensorValueModel[] allValues = values;
        if (this.valuesPaged != null) {
            allValues = new SensorValueModel[valuesPaged.length + values.length];
            System.arraycopy(this.valuesPaged, 0, allValues, 0, this.valuesPaged.length);
            System.arraycopy(values, 0, allValues, valuesPaged.length, values.length);
        }

        if (values.length < 1000) {
            Log.d(TAG, "completed getting all pages of data");

            TagModel mdl = new TagModel(tag.<String> get("name") + "/", 0, 0, TagModel.TYPE_SENSOR);
            TaggedDataModel taggedData = new TaggedDataModel(mdl, allValues);
            Dispatcher.forwardEvent(VizEvents.DataReceived, taggedData);
            this.valuesPaged = null;
            this.page = 1;
        } else {
            // exactly 1000 values? see if there are more pages
            this.valuesPaged = allValues;
            this.page++;

            String sessionId = Registry.get(Constants.REG_SESSION_ID);
            url = url.replaceAll("\\?page=\\d+&", "\\?page=" + this.page + "&");
            Log.d(TAG, "new url: " + url);
            requestData(url, sessionId, tag, this);
        }
    }

    private SensorValueModel[] valuesPaged;
    private int page;

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(VizEvents.DataRequested)) {
            Log.d(TAG, "onDataRequested");
            onDataRequested(event);
        } else if (type.equals(VizEvents.ShowTypeChoice)
                || type.equals(VizEvents.TypeChoiceCancelled)) {
            forwardToView(this.typeChooser, event);
        } else {
            forwardToView(this.vizView, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.vizView = new VizView(this);
        this.typeChooser = new VizTypeChooser(this);
    }

    private void loadVizApi() {

        // Load the visualization API
        this.isVizApiLoaded = false;
        final Runnable vizCallback = new Runnable() {

            @Override
            public void run() {
                // Log.d(TAG, "onLoadVisualizationApi");
                isVizApiLoaded = true;
            }
        };
        VisualizationUtils.loadVisualizationApi(vizCallback);

        // double check that the API has been loaded
        Timer timer = new Timer() {

            @Override
            public void run() {
                if (false == isVizApiLoaded) {
                    MessageBox.alert("CommonSense",
                            "Google visualization API not loaded, please retry.",
                            new Listener<MessageBoxEvent>() {

                                @Override
                                public void handleEvent(MessageBoxEvent be) {
                                    loadVizApi();
                                }
                            });
                }
            }
        };
        timer.schedule(1000 * 10);
    }

    private void onDataRequested(AppEvent event) {

        this.valuesPaged = null;
        this.page = 1;

        TreeModel sensor = event.getData("tag");
        String url = Constants.URL_DATA.replace("<id>", "" + sensor.<String> get("id"));
        url += "?page=" + this.page;
        url += "&per_page=" + 1000;
        url += "&start_date=" + event.<Double> getData("startDate");
        url += "&end_date=" + event.<Double> getData("endDate");

        String owner = sensor.get("alias");
        if (null != owner) {
            url += "&alias=" + owner;
        }
        String sessionId = Registry.get(Constants.REG_SESSION_ID);

        sensor.set("retryCount", 0);
        requestData(url, sessionId, sensor, this);
    }
}
