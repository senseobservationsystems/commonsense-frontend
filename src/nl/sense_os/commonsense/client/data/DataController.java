package nl.sense_os.commonsense.client.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.json.overlays.DataPoint;
import nl.sense_os.commonsense.client.json.overlays.SensorDataResponse;
import nl.sense_os.commonsense.client.json.parsers.SensorValueParser;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.components.VizPanel;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.sensorvalues.SensorValueModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.i18n.client.NumberFormat;

public class DataController extends Controller {

    private class CacheEntry {
        public long start;
        public long end;
        public SensorValueModel[] values;

        public CacheEntry(long start, long end, SensorValueModel[] values) {
            this.start = start;
            this.end = end;
            this.values = values;
        }
    }

    private static final String TAG = "DataController";
    private View progressDialog;
    private final Map<String, CacheEntry> cache = new HashMap<String, CacheEntry>();

    private static final int PER_PAGE = 1000; // max: 1000

    public DataController() {
        registerEventTypes(DataEvents.DataRequested);
        registerEventTypes(DataEvents.AjaxDataFailure, DataEvents.AjaxDataSuccess);
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(DataEvents.DataRequested)) {
            // Log.d(TAG, "DataRequested");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final long startTime = event.getData("startTime");
            final long endTime = event.getData("endTime");
            final VizPanel vizPanel = event.getData("vizPanel");

            onDataRequest(sensors, startTime, endTime, vizPanel);

        } else if (type.equals(DataEvents.AjaxDataFailure)) {
            Log.w(TAG, "AjaxDataFailure");
            final int code = event.getData("code");

            onDataFailed(code);

        } else if (type.equals(DataEvents.AjaxDataSuccess)) {
            // Log.d(TAG, "AjaxDataSuccess");
            final String response = event.<String> getData("response");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final int index = event.getData("index");
            final long startTime = event.getData("startTime");
            final long endTime = event.getData("endTime");
            int page = event.getData("page");
            final VizPanel vizPanel = event.<VizPanel> getData("vizPanel");
            final Map<SensorModel, SensorValueModel[]> pagedValues = event
                    .<Map<SensorModel, SensorValueModel[]>> getData("paged_values");

            onDataReceived(response, sensors, index, startTime, endTime, pagedValues, page,
                    vizPanel);
        }
    }

    private void hideProgress() {
        forwardToView(this.progressDialog, new AppEvent(DataEvents.HideProgress));
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.progressDialog = new ProgressDialog(this);
    }

    private void onDataComplete(long start, long end, Map<SensorModel, SensorValueModel[]> values,
            VizPanel vizPanel) {
        // Log.d(TAG, "onDataComplete...");

        hideProgress();

        // cache result
        for (Entry<SensorModel, SensorValueModel[]> data : values.entrySet()) {
            if (data.getValue().length > 0) {
                CacheEntry cacheData = new CacheEntry(start, end, data.getValue());
                cache.put(data.getKey().<String> get(SensorModel.ID), cacheData);
            }
        }

        // pass data on to visualization
        vizPanel.addData(values);
    }

    private void onDataFailed(int code) {
        hideProgress();
        MessageBox.alert(null, "Data request failed! Please try again.", null);
    }

    private void onDataReceived(String response, List<SensorModel> sensors, int index,
            long startTime, long endTime, Map<SensorModel, SensorValueModel[]> pagedValues,
            int page, VizPanel vizPanel) {
        // Log.d(TAG, "onDataReceived...");

        // TODO update progress before parsing data

        // parse the incoming data
        int total = parseDataResponse(response, sensors, index, pagedValues, page);

        // update UI after parsing data
        final int offset = page * PER_PAGE;
        final int increment = PER_PAGE;
        final int progress = Math.min(offset + increment, total);
        updateSubProgress(progress, total);

        // check if there are more pages to request for this sensor
        if (PER_PAGE * (page + 1) >= total) {
            // completed all pages for this sensor
            index++;
            page = 0;
            updateMainProgress(Math.min(index, sensors.size()), sensors.size());
        } else {
            // request next page
            page++;
        }

        // check if there are still sensors left to do
        if (index < sensors.size()) {
            requestData(sensors, index, startTime, endTime, page, pagedValues, vizPanel);
        } else {
            // completed all pages for all sensors
            onDataComplete(startTime, endTime, pagedValues, vizPanel);
        }
    }

    private void onDataRequest(List<SensorModel> sensors, long startTime, long endTime,
            VizPanel vizPanel) {
        final int page = 0;
        final int index = 0;
        final Map<SensorModel, SensorValueModel[]> pagedValues = new HashMap<SensorModel, SensorValueModel[]>();

        showProgress(sensors.size());
        requestData(sensors, index, startTime, endTime, page, pagedValues, vizPanel);
    }

    private int parseDataResponse(String response, List<SensorModel> sensors, int index,
            Map<SensorModel, SensorValueModel[]> pagedValues, int page) {
        // Log.d(TAG, "parseDataResponse...");

        int total = 0;

        try {
            // Overlay the response JSON object for easy access in Java
            final SensorDataResponse jsResponse = JsonUtils.<SensorDataResponse> safeEval(response);

            // get JsArray with data points
            final JsArray<DataPoint> data = jsResponse.getData();
            total = jsResponse.getTotal();

            // get paged values for the current sensor, so we can add the new data to the array
            final SensorModel sensor = sensors.get(index);
            SensorValueModel[] result = pagedValues.get(sensor);

            // increase size of the array if there are already pages stored
            int offset = 0;
            if (result == null) {
                result = new SensorValueModel[data.length()];
            } else {
                offset = result.length;
                SensorValueModel[] copy = new SensorValueModel[result.length + data.length()];
                System.arraycopy(result, 0, copy, 0, result.length);
                result = copy;
            }

            // get information on how to parse the data
            final String dataType = sensor.get(SensorModel.DATA_TYPE);

            DataPoint dataPoint;
            SensorValueModel value;
            for (int i = 0; i < data.length(); i++) {

                dataPoint = data.get(i);

                if (dataType.equals("json")) {
                    value = SensorValueParser.parseJsonValue(dataPoint);
                    if (null != value) {
                        result[offset + i] = value;
                    }

                } else if (dataType.equals("float")) {
                    value = SensorValueParser.parseFloatValue(dataPoint);
                    if (null != value) {
                        result[offset + i] = value;
                    }

                } else if (dataType.equals("bool")) {
                    value = SensorValueParser.parseBoolValue(dataPoint);
                    if (null != value) {
                        result[offset + i] = value;
                    }

                } else if (dataType.equals("string")) {
                    value = SensorValueParser.parseStringValue(dataPoint);
                    if (null != value) {
                        result[offset + i] = value;
                    }

                } else {
                    Log.w(TAG, "Missing data type information!");
                    value = SensorValueParser.parseStringValue(dataPoint);
                    if (null != value) {
                        result[offset + i] = value;
                    }
                }
            }

            pagedValues.put(sensor, result);

        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException parsing sensor data: " + e.getMessage());
            return -1;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException parsing sensor data: " + e.getMessage());
            return -1;
        }

        return total;
    }

    private void requestData(List<SensorModel> sensors, int sensorindex, long startTime,
            long endTime, int pageIndex, Map<SensorModel, SensorValueModel[]> pagedValues,
            VizPanel vizPanel) {
        // Log.d(TAG, "requestData...");

        if (sensorindex < sensors.size()) {

            SensorModel sensor = sensors.get(sensorindex);

            // check if the sensor has cached data
            long realStartTime = startTime;
            if (pageIndex == 0) {
                String cacheKey = sensor.get(SensorModel.ID);
                CacheEntry cacheEntry = cache.get(cacheKey);
                if (null != cacheEntry) {
                    if (cacheEntry.start <= startTime) {
                        Log.d(TAG, "Using cached data!");
                        SensorValueModel[] cachedValues = cacheEntry.values;
                        realStartTime = cachedValues[cachedValues.length - 1].getTimestamp()
                                .getTime();
                        pagedValues.put(sensor, cachedValues);
                        cache.remove(cacheKey);
                    }
                }
            }

            final String method = "GET";
            String url = Constants.URL_DATA.replace("<id>", sensor.<String> get(SensorModel.ID));
            url += "?page=" + pageIndex;
            url += "&per_page=" + PER_PAGE;
            url += "&start_date=" + NumberFormat.getFormat("#.000").format(realStartTime / 1000d);
            url += "&end_date=" + NumberFormat.getFormat("#.000").format(endTime / 1000d);
            if (null != sensor.get("alias")) {
                url += "&alias=" + sensor.get("alias");
            }
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(DataEvents.AjaxDataSuccess);
            onSuccess.setData("sensors", sensors);
            onSuccess.setData("index", sensorindex);
            onSuccess.setData("startTime", startTime);
            onSuccess.setData("endTime", endTime);
            onSuccess.setData("page", pageIndex);
            onSuccess.setData("paged_values", pagedValues);
            onSuccess.setData("vizPanel", vizPanel);
            final AppEvent onFailure = new AppEvent(DataEvents.AjaxDataFailure);

            // send request to AjaxController
            final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
            ajaxRequest.setData("method", method);
            ajaxRequest.setData("url", url);
            ajaxRequest.setData("session_id", sessionId);
            ajaxRequest.setData("onSuccess", onSuccess);
            ajaxRequest.setData("onFailure", onFailure);
            Dispatcher.forwardEvent(ajaxRequest);

        } else {
            // should not happen, but just in case...
            onDataComplete(startTime, endTime, pagedValues, vizPanel);
        }
    }

    private void showProgress(int tasks) {
        AppEvent showProgress = new AppEvent(DataEvents.ShowProgress);
        showProgress.setData("tasks", tasks);
        forwardToView(progressDialog, showProgress);
    }

    private void updateMainProgress(int progress, int total) {
        // Log.d(TAG, "updateMainProgress...");
        AppEvent update = new AppEvent(DataEvents.UpdateMainProgress);
        update.setData("progress", progress);
        update.setData("total", total);
        forwardToView(progressDialog, update);
    }

    private void updateSubProgress(double progress, double total) {
        // Log.d(TAG, "updateSubProgress...");
        AppEvent update = new AppEvent(DataEvents.UpdateDataProgress);
        update.setData("progress", progress);
        update.setData("total", total);
        forwardToView(progressDialog, update);
    }

}
