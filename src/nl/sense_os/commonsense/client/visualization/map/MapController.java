package nl.sense_os.commonsense.client.visualization.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;

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
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.user.client.Timer;

public class MapController extends Controller {

    private static final String TAG = "MapController";
    private View mapView;
    private boolean isApiLoaded;

    public MapController() {
        registerEventTypes(MainEvents.Init);
        registerEventTypes(MapEvents.LoadMap, MapEvents.Show);
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

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(MapEvents.LoadMap)) {
            requestData(event);

        } else if (type.equals(MapEvents.Show)) {
            forwardToView(this.mapView, event);

        } else {
            Log.w(TAG, "Unexpected event received");
        }
    }

    /**
     * Retrieve a list of sensors with their positions.
     * 
     * @return
     */
    private List<TreeModel> getSensorsData() {
        // @@ TODO: implement this!!!
        return new ArrayList<TreeModel>();
    }

    private void showMap() {
        // @@ TODO: implement this!!!
    }

    /**
     * Set the event with the required parameters for the AJAX request and dispatch the event. The
     * AJAX Controller will be listening to this event and it will get this request. Then, it will
     * make the request and redirect the result to this Controller by using the Show event type.
     * 
     * @param event
     */
    private void requestData(AppEvent event) {
        // @@ FIXME

        Log.d(TAG, "requestData");

        HashMap<String, Object> params = new HashMap<String, Object>();

        // SensorValueModel[] pagedValues = new SensorValueModel[0];
        int page = 0;

        TreeModel sensor = event.getData("tag");

        String url = Constants.URL_DATA.replace("<id>", "" + sensor.<String> get("id"));
        url += "?page=" + page;
        url += "&per_page=" + 1000;
        url += "&start_date=" + event.<Double> getData("startDate");
        url += "&end_date=" + event.<Double> getData("endDate");

        String owner = sensor.get("alias");
        if (null != owner) {
            url += "&alias=" + owner;
        }

        String sessionId = Registry.get(Constants.REG_SESSION_ID);

        params.put("id", sensor.<String> get("id"));
        params.put("url", url);
        params.put("session_id", sessionId);
        // Tell the AJAX Controller to dispatch this event after getting the response.
        params.put("forward_evt", MapEvents.Show);

        // DataJsniRequests.requestData(url, sessionId, sensor, page, pagedValues, this);

        // This is the way to tell the AJAX Controller to make a request.
        Dispatcher.forwardEvent(AjaxEvents.Request, params);
    }

    // @@ TODO: implement this!!!
    private JSONObject parseData(String response) {
        return new JSONObject();

        /*
         * SensorValueModel[] values = null; try { JSONObject obj =
         * JSONParser.parseStrict(response).isObject(); JSONArray data = obj.get("data").isArray();
         * 
         * Log.d(TAG, "Received " + data.size() + " sensor data points");
         * 
         * values = new SensorValueModel[data.size()]; JSONObject datapoint; double decimalTime;
         * Date timestamp; String rawValue; String cleanValue; SensorValueModel value; for (int i =
         * 0; i < data.size(); i++) {
         * 
         * datapoint = data.get(i).isObject();
         * 
         * // parse time decimalTime =
         * Double.parseDouble(datapoint.get("date").isString().stringValue()); timestamp = new
         * Date((long) (decimalTime * 1000));
         * 
         * // get value (always a String initially) rawValue =
         * datapoint.get("value").isString().stringValue(); cleanValue = rawValue.replaceAll("//",
         * "");
         * 
         * if ((cleanValue.charAt(0) == '{') && (cleanValue.charAt(cleanValue.length() - 1) == '}'))
         * { JSONObject jsonValue = JSONParser.parseStrict(cleanValue).isObject(); if (null !=
         * jsonValue) { // Log.d(TAG, "JsonValue");
         * 
         * HashMap<String, Object> fields = new HashMap<String, Object>(); for (String fieldKey :
         * jsonValue.keySet()) { JSONValue fieldValue = jsonValue.get(fieldKey);
         * 
         * JSONNumber numberField = fieldValue.isNumber(); if (null != numberField) {
         * fields.put(fieldKey, numberField.doubleValue()); continue; }
         * 
         * JSONString stringField = fieldValue.isString(); if (null != stringField) {
         * fields.put(fieldKey, stringField.stringValue()); continue; } fields.put(fieldKey,
         * fieldValue.toString()); } value = new JsonValueModel(timestamp, fields); values[i] =
         * value; continue; } }
         * 
         * try { double doubleValue = Double.parseDouble(cleanValue); // Log.d(TAG, "FloatValue");
         * value = new FloatValueModel(timestamp, doubleValue); values[i] = value; continue; } catch
         * (NumberFormatException e) { // do nothing }
         * 
         * boolean boolValue = Boolean.parseBoolean(cleanValue); if (!boolValue &&
         * cleanValue.equalsIgnoreCase("false")) { // Log.d(TAG, "BooleanValue"); value = new
         * BooleanValueModel(timestamp, boolValue); values[i] = value; continue; }
         * 
         * // Log.d(TAG, "StringValue"); value = new StringValueModel(timestamp, cleanValue);
         * values[i] = value; continue; }
         * 
         * } catch (NullPointerException e) { Log.e(TAG,
         * "NullPointerException parsing sensor data: " + e.getMessage());
         * handler.onDataFailed(tag); }
         * 
         * // append the parsed values to any previous pages of values SensorValueModel[] allValues
         * = values; if (pagedValues != null) { allValues = new SensorValueModel[pagedValues.length
         * + values.length]; System.arraycopy(pagedValues, 0, allValues, 0, pagedValues.length);
         * System.arraycopy(values, 0, allValues, pagedValues.length, values.length); }
         * 
         * if (values.length < 1000) { Log.d(TAG, "completed getting all pages of data");
         * 
         * TagModel mdl = new TagModel(tag.<String> get("name") + "/", 0, 0, TagModel.TYPE_SENSOR);
         * TaggedDataModel taggedData = new TaggedDataModel(mdl, allValues);
         * handler.onDataReceived(taggedData); } else { // exactly 1000 values? see if there are
         * more pages pagedValues = allValues; page++;
         * 
         * String sessionId = Registry.get(Constants.REG_SESSION_ID); url =
         * url.replaceAll("\\?page=\\d+&", "\\?page=" + page + "&"); Log.d(TAG, "new url: " + url);
         * DataJsniRequests.requestData(url, sessionId, tag, page, pagedValues, handler); }
         */
    }

}
