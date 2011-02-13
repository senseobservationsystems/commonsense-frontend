package nl.sense_os.commonsense.client.visualization;

import java.util.Date;
import java.util.HashMap;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.states.StateEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.map.MapEvents;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.TagModel;
import nl.sense_os.commonsense.shared.sensorvalues.BooleanValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.FloatValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.JsonValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.SensorValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.StringValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.TaggedDataModel;

import com.chap.links.client.Timeline;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
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
    private View vizView;
    private View typeChooser;
    private boolean isVizApiLoaded;

    public VizController() {
        registerEventTypes(MainEvents.Init);
        registerEventTypes(VizEvents.Show);
        registerEventTypes(LoginEvents.LoggedOut);
        registerEventTypes(VizEvents.DataRequested, VizEvents.DataNotReceived,
                VizEvents.DataReceived);
        registerEventTypes(VizEvents.ShowTypeChoice, VizEvents.TypeChoiceCancelled);
        registerEventTypes(VizEvents.ShowLineChart, VizEvents.ShowTable, VizEvents.ShowNetwork);
        registerEventTypes(StateEvents.FeedbackReady, StateEvents.FeedbackComplete,
                StateEvents.FeedbackCancelled);

        registerEventTypes(MapEvents.MapReady);

        registerEventTypes(VizEvents.AjaxDataFailure, VizEvents.AjaxDataSuccess);

        loadVizApi();
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(VizEvents.DataRequested)) {
            Log.d(TAG, "DataRequested");
            final TreeModel sensor = event.<TreeModel> getData("sensor");
            final double startDate = event.<Double> getData("startDate");
            final double endDate = event.<Double> getData("endDate");
            final int page = 0;
            final SensorValueModel[] pagedValues = new SensorValueModel[0];
            requestData(sensor, startDate, endDate, page, pagedValues);

        } else if (type.equals(VizEvents.ShowTypeChoice)
                || type.equals(VizEvents.TypeChoiceCancelled)) {
            forwardToView(this.typeChooser, event);

        } else if (type.equals(VizEvents.AjaxDataFailure)) {
            Log.w(TAG, "AjaxDataFailure");
            final int code = event.getData("code");
            onDataFailed(code);

        } else if (type.equals(VizEvents.AjaxDataSuccess)) {
            final String response = event.<String> getData("response");
            final TreeModel sensor = event.<TreeModel> getData("sensor");
            double startDate = event.<Double> getData("startDate");
            double endDate = event.<Double> getData("endDate");
            final int page = event.getData("page");
            final SensorValueModel[] pagedValues = event
                    .<SensorValueModel[]> getData("paged_values");

            final SensorValueModel[] newValues = parseDataResponse(response);
            onDataSucces(sensor, startDate, endDate, page, pagedValues, newValues);

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
                Log.d(TAG, "Google Visualization API loaded...");
                isVizApiLoaded = true;
            }
        };
        VisualizationUtils.loadVisualizationApi(vizCallback, Timeline.PACKAGE);

        // double check that the API has been loaded within 10 seconds
        Timer timer = new Timer() {

            @Override
            public void run() {
                if (false == isVizApiLoaded) {
                    MessageBox.confirm(null, "Google visualization API not loaded, retry?",
                            new Listener<MessageBoxEvent>() {

                                @Override
                                public void handleEvent(MessageBoxEvent be) {
                                    final Button b = be.getButtonClicked();
                                    if ("ok".equalsIgnoreCase(b.getText())) {
                                        loadVizApi();
                                    }
                                }
                            });
                }
            }
        };
        timer.schedule(1000 * 10);
    }

    public void onDataFailed(int code) {
        Dispatcher.forwardEvent(VizEvents.DataNotReceived);
    }

    public void onDataReceived(TaggedDataModel data) {
        forwardToView(this.vizView, new AppEvent(VizEvents.DataReceived, data));
    }

    private void onDataSucces(TreeModel sensor, double startDate, double endDate, int page,
            SensorValueModel[] pagedValues, SensorValueModel[] newValues) {

        // merge new values with paged values
        SensorValueModel[] allValues = newValues;
        if (pagedValues != null) {
            allValues = new SensorValueModel[pagedValues.length + newValues.length];
            System.arraycopy(pagedValues, 0, allValues, 0, pagedValues.length);
            System.arraycopy(newValues, 0, allValues, pagedValues.length, newValues.length);
        }

        // finish up, or request another page of data
        if (newValues.length < 1000) {
            sensor.set("cached_data", allValues);

            TagModel mdl = new TagModel(sensor.<String> get("name") + "/", 0, 0,
                    TagModel.TYPE_SENSOR);
            TaggedDataModel taggedData = new TaggedDataModel(mdl, allValues);
            onDataReceived(taggedData);
        } else {
            // exactly 1000 values? see if there are more pages
            pagedValues = allValues;
            page++;

            requestData(sensor, startDate, endDate, page, pagedValues);
        }
    }

    private SensorValueModel[] parseDataResponse(String response) {
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
            return null;
        }

        Log.d(TAG, "Finished parsing received data points");

        return values;
    }

    private void requestData(TreeModel sensor, double startDate, double endDate, int page,
            SensorValueModel[] pagedValues) {

        // set retry count as sensor property
        sensor.set("retryCount", 0);

        final String method = "GET";
        String url = Constants.URL_DATA.replace("<id>", "" + sensor.<String> get("id"));
        url += "?page=" + page;
        url += "&per_page=" + 1000;
        url += "&start_date=" + startDate;
        url += "&end_date=" + endDate;
        if (null != sensor.get("alias")) {
            url += "&alias=" + sensor.get("alias");
        }
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(VizEvents.AjaxDataSuccess);
        onSuccess.setData("sensor", sensor);
        onSuccess.setData("startDate", startDate);
        onSuccess.setData("endDate", endDate);
        onSuccess.setData("page", page);
        onSuccess.setData("paged_values", pagedValues);
        final AppEvent onFailure = new AppEvent(VizEvents.AjaxDataFailure);

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);
        Dispatcher.forwardEvent(ajaxRequest);
    }
}
