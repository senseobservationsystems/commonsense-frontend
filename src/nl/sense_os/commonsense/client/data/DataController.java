package nl.sense_os.commonsense.client.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.json.overlays.AbstractDataPoint;
import nl.sense_os.commonsense.client.json.overlays.JsoBoolDataPoint;
import nl.sense_os.commonsense.client.json.overlays.JsoDataPoint;
import nl.sense_os.commonsense.client.json.overlays.JsoFloatDataPoint;
import nl.sense_os.commonsense.client.json.overlays.JsoJsonDataPoint;
import nl.sense_os.commonsense.client.json.overlays.SensorDataResponse;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.components.VizPanel;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.SensorModel;

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
        @SuppressWarnings("unused")
        public long end;
        public AbstractDataPoint[] values;

        public CacheEntry(long start, long end, AbstractDataPoint[] values) {
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
        registerEventTypes(DataEvents.DataRequest, DataEvents.RefreshRequest);
        registerEventTypes(DataEvents.AjaxDataFailure, DataEvents.AjaxDataSuccess);
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(DataEvents.DataRequest)) {
            // Log.d(TAG, "DataRequest");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final long start = event.getData("startTime");
            final long end = event.getData("endTime");
            final VizPanel vizPanel = event.getData("vizPanel");

            onDataRequest(start, end, sensors, vizPanel);

        } else if (type.equals(DataEvents.RefreshRequest)) {
            // Log.d(TAG, "DataRequest");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final VizPanel vizPanel = event.getData("vizPanel");

            onRefreshRequest(sensors, vizPanel);

        } else if (type.equals(DataEvents.AjaxDataFailure)) {
            Log.w(TAG, "AjaxDataFailure");
            final int code = event.getData("code");

            onDataFailed(code);

        } else if (type.equals(DataEvents.AjaxDataSuccess)) {
            // Log.d(TAG, "AjaxDataSuccess");
            final String response = event.<String> getData("response");
            final long start = event.getData("start");
            final long end = event.getData("end");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final int sensorIndex = event.getData("sensorIndex");
            final int pageIndex = event.getData("pageIndex");
            final Map<SensorModel, AbstractDataPoint[]> pagedValues = event
                    .<Map<SensorModel, AbstractDataPoint[]>> getData("pagedValues");
            final VizPanel vizPanel = event.<VizPanel> getData("vizPanel");

            onDataReceived(response, start, end, sensors, sensorIndex, pageIndex, pagedValues,
                    vizPanel);
        }
    }

    private void onRefreshRequest(List<SensorModel> sensors, VizPanel vizPanel) {

        // get the oldest cached entry for each sensor
        // not very important to get the optimal value: cache will be checked again in requestData()
        long start = System.currentTimeMillis();
        for (SensorModel sensor : sensors) {
            CacheEntry cachedEntry = cache.get(sensor.get(SensorModel.ID));
            AbstractDataPoint[] cachedValues = cachedEntry.values;
            long lastCache = cachedValues[cachedValues.length - 1].getTimestamp().getTime();
            if (lastCache < start) {
                start = lastCache;
            }
        }
        onDataRequest(start, System.currentTimeMillis(), sensors, vizPanel);
    }

    private void hideProgress() {
        forwardToView(this.progressDialog, new AppEvent(DataEvents.HideProgress));
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.progressDialog = new ProgressDialog(this);
    }

    private void onDataComplete(long start, long end, Map<SensorModel, AbstractDataPoint[]> values,
            VizPanel vizPanel) {
        // Log.d(TAG, "onDataComplete...");

        hideProgress();

        // cache result
        for (Entry<SensorModel, AbstractDataPoint[]> data : values.entrySet()) {
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

    private void onDataReceived(String response, long start, long end, List<SensorModel> sensors,
            int sensorIndex, int pageIndex, Map<SensorModel, AbstractDataPoint[]> pagedValues,
            VizPanel vizPanel) {
        // Log.d(TAG, "onDataReceived...");

        updateSubProgress(-1, -1, "Parsing received data chunk...");

        // parse the incoming data
        int total = parseDataResponse(response, sensors, sensorIndex, pagedValues);

        // update UI after parsing data
        final int offset = pageIndex * PER_PAGE;
        final int increment = PER_PAGE;
        final int progress = Math.min(offset + increment, total);
        updateSubProgress(progress, total, "Requesting data chunk...");

        // check if there are more pages to request for this sensor
        if (PER_PAGE * (pageIndex + 1) >= total) {
            // completed all pages for this sensor
            sensorIndex++;
            pageIndex = 0;
            updateMainProgress(Math.min(sensorIndex, sensors.size()), sensors.size());
        } else {
            // request next page
            pageIndex++;
        }

        // check if there are still sensors left to do
        if (sensorIndex < sensors.size()) {
            requestData(start, end, sensors, sensorIndex, pageIndex, pagedValues, vizPanel);
        } else {
            // completed all pages for all sensors
            onDataComplete(start, end, pagedValues, vizPanel);
        }
    }

    private void onDataRequest(long start, long end, List<SensorModel> sensors, VizPanel vizPanel) {
        final int page = 0;
        final int index = 0;
        final Map<SensorModel, AbstractDataPoint[]> pagedValues = new HashMap<SensorModel, AbstractDataPoint[]>();

        showProgress(sensors.size());
        requestData(start, end, sensors, index, page, pagedValues, vizPanel);
    }

    private int parseDataResponse(String response, List<SensorModel> sensors, int sensorIndex,
            Map<SensorModel, AbstractDataPoint[]> pagedValues) {
        // Log.d(TAG, "parseDataResponse...");

        int total = 0;

        try {
            // Overlay the response JSON object for easy access in Java
            final SensorDataResponse jsResponse = JsonUtils.<SensorDataResponse> safeEval(response);

            // get JsArray with data points
            final JsArray<JsoDataPoint> data = jsResponse.getData();
            total = jsResponse.getTotal();

            // get paged values for the current sensor, so we can add the new data to the array
            final SensorModel sensor = sensors.get(sensorIndex);
            AbstractDataPoint[] result = pagedValues.get(sensor);

            // increase size of the array if there are already pages stored
            int offset = 0;
            if (result == null) {
                result = new AbstractDataPoint[data.length()];
            } else {
                offset = result.length;
                AbstractDataPoint[] copy = new AbstractDataPoint[result.length + data.length()];
                System.arraycopy(result, 0, copy, 0, result.length);
                result = copy;
            }

            // get information on how to parse the data
            final String dataType = sensor.get(SensorModel.DATA_TYPE);

            JsoDataPoint dataPoint;
            for (int i = 0; i < data.length(); i++) {

                dataPoint = data.get(i);

                if (dataType.equals("json")) {
                    result[offset + i] = (JsoJsonDataPoint) dataPoint;

                } else if (dataType.equals("float")) {
                    result[offset + i] = (JsoFloatDataPoint) dataPoint;

                } else if (dataType.equals("bool")) {
                    result[offset + i] = (JsoBoolDataPoint) dataPoint;

                } else if (dataType.equals("string")) {
                    result[offset + i] = dataPoint;

                } else {
                    Log.w(TAG, "Missing data type information!");
                    result[offset + i] = dataPoint;
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

    private void requestData(long start, long end, List<SensorModel> sensors, int sensorIndex,
            int pageIndex, Map<SensorModel, AbstractDataPoint[]> pagedValues, VizPanel vizPanel) {
        // Log.d(TAG, "requestData...");

        if (sensorIndex < sensors.size()) {

            SensorModel sensor = sensors.get(sensorIndex);

            // check if the sensor has cached data
            long realStart = start;
            if (pageIndex == 0) {
                String cacheKey = sensor.get(SensorModel.ID);
                CacheEntry cacheEntry = cache.get(cacheKey);
                if (null != cacheEntry) {
                    if (cacheEntry.start <= start) {
                        // Log.d(TAG, "Using cached data!");
                        AbstractDataPoint[] cachedValues = cacheEntry.values;
                        realStart = cachedValues[cachedValues.length - 1].getTimestamp().getTime();
                        pagedValues.put(sensor, cachedValues);
                        cache.remove(cacheKey);
                    }
                }
            }

            final String method = "GET";
            String url = Constants.URL_DATA.replace("<id>", sensor.<String> get(SensorModel.ID));
            url += "?page=" + pageIndex;
            url += "&per_page=" + PER_PAGE;
            url += "&start_date=" + NumberFormat.getFormat("#.000").format(realStart / 1000d);
            url += "&end_date=" + NumberFormat.getFormat("#.000").format(end / 1000d);
            if (null != sensor.get("alias")) {
                url += "&alias=" + sensor.get("alias");
            }
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(DataEvents.AjaxDataSuccess);
            onSuccess.setData("start", start);
            onSuccess.setData("end", end);
            onSuccess.setData("sensors", sensors);
            onSuccess.setData("sensorIndex", sensorIndex);
            onSuccess.setData("pageIndex", pageIndex);
            onSuccess.setData("pagedValues", pagedValues);
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
            onDataComplete(start, end, pagedValues, vizPanel);
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

    private void updateSubProgress(double progress, double total, String text) {
        // Log.d(TAG, "updateSubProgress...");
        AppEvent update = new AppEvent(DataEvents.UpdateDataProgress);
        update.setData("progress", progress);
        update.setData("total", total);
        update.setData("text", text);
        forwardToView(progressDialog, update);
    }

}
