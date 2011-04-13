package nl.sense_os.commonsense.client.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.i18n.client.NumberFormat;

public class DataController extends Controller {

    private static final String TAG = "DataController";
    private View progressDialog;

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
            final int index = 0;
            final long startTime = event.getData("startTime");
            final long endTime = event.getData("endTime");
            final VizPanel vizPanel = event.getData("vizPanel");
            final int page = 0;
            final Map<SensorModel, SensorValueModel[]> pagedValues = new HashMap<SensorModel, SensorValueModel[]>();

            showProgress(sensors.size());
            requestData(sensors, index, startTime, endTime, page, pagedValues, vizPanel);

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

    private void onDataComplete(Map<SensorModel, SensorValueModel[]> values, VizPanel vizPanel) {

        hideProgress();

        // pass data on to visualization
        vizPanel.addData(values);
    }

    private void onDataFailed(int code) {
        // TODO give the user feedback on failed request
        Log.e(TAG, "Data retrieval failed! If you read this: please inform the user.");
    }

    private void onDataReceived(String response, List<SensorModel> sensors, int index,
            long startTime, long endTime, Map<SensorModel, SensorValueModel[]> pagedValues,
            int page, VizPanel vizPanel) {
        Log.d(TAG, "onDataReceived...");

        final SensorModel sensor = sensors.get(index);
        SensorValueModel[] currentPagedValues = pagedValues.get(sensor);

        // update UI after reception of data (if possible)
        if (page > 0) {
            final int offset = page * PER_PAGE;
            final int increment = (PER_PAGE >> 1);
            final int total = currentPagedValues.length; // total > 0 to prevent NaN
            final int progress = Math.min(offset + increment,
                    Math.max(total - increment, total >> 1));
            updateSubProgress(progress, total);
        }

        // parse the incoming data
        parseDataResponse(response, sensors, index, pagedValues, page);

        // update UI after parsing data
        currentPagedValues = pagedValues.get(sensor);
        final int offset = page * PER_PAGE;
        final int increment = PER_PAGE;
        final int total = currentPagedValues.length;
        final int progress = Math.min(offset + increment, total);
        updateSubProgress(progress, total);

        // check if there are more pages to request for this sensor
        if (null != currentPagedValues && PER_PAGE * (page + 1) >= currentPagedValues.length) {
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
            onDataComplete(pagedValues, vizPanel);
        }
    }

    private void parseDataResponse(String response, List<SensorModel> sensors, int index,
            Map<SensorModel, SensorValueModel[]> pagedValues, int page) {
        Log.d(TAG, "parseDataResponse...");

        try {
            // Overlay the response JSON object for easy access in Java
            final SensorDataResponse jsResponse = JsonUtils.<SensorDataResponse> safeEval(response);

            // get JsArray with data points
            final JsArray<DataPoint> data = jsResponse.getData();
            final int total = jsResponse.getTotal();

            // get paged values for the current sensor, so we can add the new data to the array
            final SensorModel sensor = sensors.get(index);
            SensorValueModel[] result = pagedValues.get(sensor);
            if (page == 0 || result == null) {
                result = new SensorValueModel[total];
            }

            // get information on how to parse the data
            final String dataType = sensor.get(SensorModel.DATA_TYPE);

            DataPoint dataPoint;
            SensorValueModel value;
            final int offset = page * PER_PAGE;
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
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException parsing sensor data: " + e.getMessage());
        }
    }

    private void requestData(List<SensorModel> sensors, int index, long startTime, long endTime,
            int page, Map<SensorModel, SensorValueModel[]> pagedValues, VizPanel vizPanel) {
        Log.d(TAG, "requestData...");

        if (index < sensors.size()) {

            SensorModel sensor = sensors.get(index);

            final String method = "GET";
            String url = Constants.URL_DATA.replace("<id>", sensor.<String> get(SensorModel.ID));
            url += "?page=" + page;
            url += "&per_page=" + PER_PAGE;
            url += "&start_date=" + NumberFormat.getFormat("#.000").format(startTime / 1000d);
            url += "&end_date=" + NumberFormat.getFormat("#.000").format(endTime / 1000d);
            if (null != sensor.get("alias")) {
                url += "&alias=" + sensor.get("alias");
            }
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(DataEvents.AjaxDataSuccess);
            onSuccess.setData("sensors", sensors);
            onSuccess.setData("index", index);
            onSuccess.setData("startTime", startTime);
            onSuccess.setData("endTime", endTime);
            onSuccess.setData("page", page);
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
            onDataComplete(pagedValues, vizPanel);
        }
    }

    private void showProgress(int tasks) {
        AppEvent showProgress = new AppEvent(DataEvents.ShowProgress);
        showProgress.setData("tasks", tasks);
        forwardToView(progressDialog, showProgress);
    }

    private void updateMainProgress(int progress, int total) {
        Log.d(TAG, "updateMainProgress...");
        AppEvent update = new AppEvent(DataEvents.UpdateMainProgress);
        update.setData("progress", progress);
        update.setData("total", total);
        forwardToView(progressDialog, update);
    }

    private void updateSubProgress(double progress, double total) {
        Log.d(TAG, "updateSubProgress...");
        AppEvent update = new AppEvent(DataEvents.UpdateDataProgress);
        update.setData("progress", progress);
        update.setData("total", total);
        forwardToView(progressDialog, update);
    }

}
