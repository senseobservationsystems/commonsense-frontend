package nl.sense_os.commonsense.client.visualization.map;

import java.util.Date;
import java.util.HashMap;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.map.components.MapPanel;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.sensorvalues.BooleanValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.FloatValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.JsonValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.SensorValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.StringValueModel;

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
import com.google.gwt.maps.client.Maps;
import com.google.gwt.user.client.Timer;

public class MapController extends Controller {

    private static final String TAG = "MapController";
    private View mapView;
    private boolean isApiLoaded;

    public MapController() {
        registerEventTypes(MainEvents.Init);
        registerEventTypes(MapEvents.Show);
        registerEventTypes(MapEvents.LoadData);
        registerEventTypes(MapEvents.AjaxDataFailure, MapEvents.AjaxDataSuccess);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(MainEvents.Init)) {
            // do nothing, initialization is done in initialize()

        } else if (type.equals(MapEvents.LoadData)) {
            Log.d(TAG, "LoadData");
            final MapPanel panel = event.<MapPanel> getData("panel");
            final TreeModel sensor = event.<TreeModel> getData("sensor");
            final double startDate = event.getData("startDate");
            final double endDate = event.getData("endDate");
            final int page = 0;
            final SensorValueModel[] pagedValues = new SensorValueModel[0];

            requestData(panel, sensor, startDate, endDate, page, pagedValues);

        } else if (type.equals(MapEvents.AjaxDataFailure)) {
            Log.d(TAG, "AjaxDataFailure");
            final int code = event.getData("code");
            final MapPanel panel = event.getData("panel");
            onDataFailed(code, panel);

        } else if (type.equals(MapEvents.AjaxDataSuccess)) {
            Log.d(TAG, "AjaxDataSuccess");
            final MapPanel panel = event.<MapPanel> getData("panel");
            final String response = event.<String> getData("response");
            final TreeModel sensor = event.<TreeModel> getData("sensor");
            double startDate = event.<Double> getData("startDate");
            double endDate = event.<Double> getData("endDate");
            final int page = event.getData("page");
            final SensorValueModel[] pagedValues = event
                    .<SensorValueModel[]> getData("paged_values");

            final SensorValueModel[] newValues = parseDataResponse(response);
            onDataSucces(panel, sensor, startDate, endDate, page, pagedValues, newValues);

        } else if (type.equals(MapEvents.Show)) {
            forwardToView(this.mapView, event);

        } else {
            Log.w(TAG, "Unexpected event received");
        }
    }

    public void initialize() {
        super.initialize();
        this.mapView = new MapView(this);
        loadMapsApi();
    }

    /**
     * Loads the Google Maps API when the controller is initialized. If loading fails, a popup
     * window is shown.
     */
    private void loadMapsApi() {

        // Asynchronously load the Maps API.
        this.isApiLoaded = false;
        Maps.loadMapsApi(Constants.MAPS_API_KEY, "2", false, new Runnable() {

            @Override
            public void run() {
                Log.d(TAG, "Google Maps API loaded...");
                isApiLoaded = true;
            }
        });

        // double check that the API has been loaded within 10 seconds
        new Timer() {

            @Override
            public void run() {
                if (false == isApiLoaded) {
                    MessageBox.confirm(null, "Google Maps API not loaded, retry?",
                            new Listener<MessageBoxEvent>() {

                                @Override
                                public void handleEvent(MessageBoxEvent be) {
                                    final Button b = be.getButtonClicked();
                                    if ("ok".equalsIgnoreCase(b.getText())) {
                                        loadMapsApi();
                                    }
                                }
                            });
                }
            }
        }.schedule(1000 * 10);
    }

    private void onDataFailed(int code, MapPanel panel) {
        AppEvent event = new AppEvent(MapEvents.LoadFailure);
        event.setData("panel", panel);
        forwardToView(this.mapView, event);
    }

    private void onDataSucces(MapPanel panel, TreeModel sensor, double startDate, double endDate,
            int page, SensorValueModel[] pagedValues, SensorValueModel[] newValues) {

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

            // forward the data to the MapView
            AppEvent addEvent = new AppEvent(MapEvents.AddData);
            addEvent.setData("data", allValues);
            addEvent.setData("sensor", sensor);
            addEvent.setData("panel", panel);
            forwardToView(this.mapView, addEvent);

            // TODO handle multiple sensor data requests
            AppEvent finishEvent = new AppEvent(MapEvents.LoadSuccess);
            finishEvent.setData("panel", panel);
            forwardToView(this.mapView, finishEvent);
        } else {
            // exactly 1000 values? see if there are more pages
            pagedValues = allValues;
            page++;

            requestData(panel, sensor, startDate, endDate, page, pagedValues);
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

    /**
     * Set the event with the required parameters for the AJAX request and dispatch the event. The
     * AJAX Controller will be listening to this event and it will get this request. Then, it will
     * make the request and redirect the result to this Controller by using the Show event type.
     * 
     * @param event
     */
    private void requestData(MapPanel panel, TreeModel sensor, double startDate, double endDate,
            int page, SensorValueModel[] pagedValues) {

        Log.d(TAG, "requestData");
        Log.d(TAG, "panel " + panel);
        Log.d(TAG, "sensor " + sensor);
        Log.d(TAG, "startDate " + startDate + ", endDate " + endDate);
        Log.d(TAG, "page " + page);
        Log.d(TAG, "pagedValues " + pagedValues);

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
        final AppEvent onSuccess = new AppEvent(MapEvents.AjaxDataSuccess);
        onSuccess.setData("panel", panel);
        onSuccess.setData("sensor", sensor);
        onSuccess.setData("startDate", startDate);
        onSuccess.setData("endDate", endDate);
        onSuccess.setData("page", page);
        onSuccess.setData("paged_values", pagedValues);
        final AppEvent onFailure = new AppEvent(MapEvents.AjaxDataFailure);
        onFailure.setData("panel", panel);

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
