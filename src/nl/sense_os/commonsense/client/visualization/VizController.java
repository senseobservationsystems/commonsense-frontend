package nl.sense_os.commonsense.client.visualization;

import java.util.Date;
import java.util.HashMap;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.json.overlays.DataPoint;
import nl.sense_os.commonsense.client.json.overlays.SensorDataResponse;
import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.states.StateEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.map.MapEvents;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.SensorModel;
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
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;
import com.google.gwt.visualization.client.VisualizationUtils;

public class VizController extends Controller {

    private static final String TAG = "VizController";
    private static final int PER_PAGE = 1000; // max: 1000
    private View vizView;
    private View progressDialog;
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
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(VizEvents.DataRequested)) {
            // Log.d(TAG, "DataRequested");

            showWaitDialog();

            final TreeModel sensor = event.<TreeModel> getData("sensor");
            final double startDate = event.<Double> getData("startDate");
            final double endDate = event.<Double> getData("endDate");
            final int page = 0;
            final SensorValueModel[] pagedValues = null;
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

            SensorValueModel[] newValues = parseDataResponse(response, sensor, pagedValues, page
                    * PER_PAGE);
            onDataSucces(sensor, startDate, endDate, page, newValues);

        } else {
            forwardToView(this.vizView, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.vizView = new VizView(this);
        this.progressDialog = new ProgressDialog(this);
        this.typeChooser = new VizTypeChooser(this);

        loadVizApi();
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
        new Timer() {

            @Override
            public void run() {
                if (false == isVizApiLoaded) {
                    MessageBox.confirm(null, "Google visualization API not loaded, retry?",
                            new Listener<MessageBoxEvent>() {

                                @Override
                                public void handleEvent(MessageBoxEvent be) {
                                    final Button b = be.getButtonClicked();
                                    if ("yes".equalsIgnoreCase(b.getText())) {
                                        loadVizApi();
                                    }
                                }
                            });
                }
            }
        }.schedule(1000 * 10);
    }

    private void onDataFailed(int code) {
        Dispatcher.forwardEvent(VizEvents.DataNotReceived);
    }

    private void onDataReceived(TaggedDataModel data) {
        forwardToView(this.vizView, new AppEvent(VizEvents.DataReceived, data));

        hideWaitDialog();
    }

    private void hideWaitDialog() {
        forwardToView(progressDialog, new AppEvent(VizEvents.HideProgress));
    }

    private void onDataSucces(TreeModel sensor, double startDate, double endDate, int page,
            SensorValueModel[] pagedValues) {

        updateWaitDialog(Math.min(PER_PAGE * (page + 1), pagedValues.length), pagedValues.length);

        // finish up, or request another page of data
        if (PER_PAGE * (page + 1) >= pagedValues.length) {
            sensor.set("cached_data", pagedValues);

            TagModel mdl = new TagModel(sensor.<String> get("name") + "/", 0, 0,
                    TagModel.TYPE_SENSOR);
            TaggedDataModel taggedData = new TaggedDataModel(mdl, pagedValues);
            onDataReceived(taggedData);
        } else {
            // see if there are more pages
            page++;

            requestData(sensor, startDate, endDate, page, pagedValues);
        }
    }

    private SensorValueModel[] parseDataResponse(String response, TreeModel sensor,
            SensorValueModel[] result, int offset) {

        try {
            Log.d(TAG, "Start overlaying...");
            SensorDataResponse jsResponse = JsonUtils.<SensorDataResponse> safeEval(response);
            Log.d(TAG, "Overlay done!");

            JsArray<DataPoint> data = jsResponse.getData();
            int total = jsResponse.getTotal();
            if (offset == 0 || result == null) {
                result = new SensorValueModel[total];
            }

            // update UI
            updateWaitDialog(offset + (data.length() >> 1), total);

            Log.d(TAG, "Received " + data.length() + " overlayed sensor data points");

            // get information on how to parse the data
            String dataType = sensor.get(SensorModel.DATA_TYPE);
            // TODO can we use the data structure info?
            // String dataStructure = sensor.get(SensorModel.DATA_STRUCTURE);

            DataPoint datapoint;
            double decimalTime;
            Date timestamp;
            String rawValue;
            String cleanValue;
            SensorValueModel value;
            for (int i = 0; i < data.length(); i++) {

                datapoint = data.get(i);

                // parse time
                decimalTime = Double.parseDouble(datapoint.getDate());
                timestamp = new Date((long) (decimalTime * 1000));

                // get value (always a String initially)
                rawValue = datapoint.getValue();
                cleanValue = rawValue.replaceAll("//", "");

                if (dataType.equals("json")) {
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
                        result[offset + i] = value;
                        continue;
                    }

                } else if (dataType.equals("float")) {
                    try {
                        double doubleValue = Double.parseDouble(cleanValue);
                        // Log.d(TAG, "FloatValue");
                        value = new FloatValueModel(timestamp, doubleValue);
                        result[offset + i] = value;
                        continue;
                    } catch (NumberFormatException e) {
                        // do nothing
                    }

                } else if (dataType.equals("bool")) {
                    boolean boolValue = Boolean.parseBoolean(cleanValue);
                    if (!boolValue && cleanValue.equalsIgnoreCase("false")) {
                        // Log.d(TAG, "BooleanValue");
                        value = new BooleanValueModel(timestamp, boolValue);
                        result[offset + i] = value;
                        continue;
                    }

                } else if (dataType.equals("string")) {
                    // Log.d(TAG, "StringValue");
                    value = new StringValueModel(timestamp, cleanValue);
                    result[offset + i] = value;
                    continue;

                } else {
                    Log.w(TAG, "Missing data type information!");
                    value = new StringValueModel(timestamp, cleanValue);
                    result[offset + i] = value;
                    continue;
                }
            }

        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException parsing sensor data: " + e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException parsing sensor data: " + e.getMessage());
            return null;
        }

        Log.d(TAG, "Finished parsing received data points");

        return result;
    }

    private void updateWaitDialog(double progress, double total) {
        Log.d(TAG, "Update progress: " + (progress / total));
        AppEvent update = new AppEvent(VizEvents.UpdateProgress);
        update.setData("progress", progress);
        update.setData("total", total);
        forwardToView(progressDialog, update);
    }

    private void showWaitDialog() {
        forwardToView(progressDialog, new AppEvent(VizEvents.ShowProgress));
    }

    private void requestData(TreeModel sensor, double startDate, double endDate, int page,
            SensorValueModel[] pagedValues) {

        final String method = "GET";
        String url = Constants.URL_DATA.replace("<id>", sensor.<String> get("id"));
        url += "?page=" + page;
        url += "&per_page=" + PER_PAGE;
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
