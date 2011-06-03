package nl.sense_os.commonsense.client.viz.data;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.viz.data.cache.Cache;
import nl.sense_os.commonsense.client.viz.data.timeseries.BackEndDataPoint;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;
import nl.sense_os.commonsense.client.viz.panels.VizPanel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.i18n.client.NumberFormat;

public class DataController extends Controller {

    private static final Logger LOGGER = Logger.getLogger(DataController.class.getName());
    private View progressDialog;
    private static final int PER_PAGE = 1000; // max: 1000

    public DataController() {

        // LOGGER.setLevel(Level.ALL);

        registerEventTypes(DataEvents.DataRequest, DataEvents.RefreshRequest);
        registerEventTypes(DataEvents.AjaxDataFailure, DataEvents.AjaxDataSuccess);
        registerEventTypes(LoginEvents.LoggedOut);
        registerEventTypes(DataEvents.LatestValuesRequest, DataEvents.LatestValueAjaxSuccess,
                DataEvents.LatestValueAjaxFailure);
    }

    private void getLatestValues(List<SensorModel> sensors, int index, VizPanel panel) {
        if (index < sensors.size()) {

            SensorModel sensor = sensors.get(index);

            final String method = "GET";
            String url = Urls.DATA.replace("<id>", "" + sensor.getId()) + "?last=1";
            if (-1 != sensor.getAlias()) {
                url += "&alias=" + sensor.getAlias();
            }
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(DataEvents.LatestValueAjaxSuccess);
            onSuccess.setData("sensors", sensors);
            onSuccess.setData("index", index);
            onSuccess.setData("panel", panel);
            final AppEvent onFailure = new AppEvent(DataEvents.LatestValueAjaxFailure);
            onFailure.setData("sensors", sensors);
            onFailure.setData("index", index);
            onFailure.setData("panel", panel);

            // send request to AjaxController
            final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
            ajaxRequest.setData("method", method);
            ajaxRequest.setData("url", url);
            ajaxRequest.setData("session_id", sessionId);
            ajaxRequest.setData("onSuccess", onSuccess);
            ajaxRequest.setData("onFailure", onFailure);
            Dispatcher.forwardEvent(ajaxRequest);

        } else {
            // hoooray we're done!
            onLatestValuesComplete(sensors, panel);
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(DataEvents.DataRequest)) {
            LOGGER.finest("DataRequest");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final long start = event.getData("startTime");
            final long end = event.getData("endTime");
            final VizPanel vizPanel = event.getData("vizPanel");

            onDataRequest(start, end, sensors, vizPanel);

        } else if (type.equals(DataEvents.RefreshRequest)) {
            LOGGER.finest("RefreshRequest");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final long start = event.getData("start");
            final VizPanel vizPanel = event.getData("vizPanel");

            onRefreshRequest(sensors, start, vizPanel);

        } else if (type.equals(DataEvents.AjaxDataFailure)) {
            LOGGER.warning("AjaxDataFailure");
            final int code = event.getData("code");

            onDataFailed(code);

        } else if (type.equals(DataEvents.AjaxDataSuccess)) {
            LOGGER.finest("AjaxDataSuccess");
            final String response = event.<String> getData("response");
            final long start = event.getData("start");
            final long end = event.getData("end");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final int sensorIndex = event.getData("sensorIndex");
            final long sensorChunkStart = event.getData("sensorChunkStart");
            final int sensorProgress = event.getData("sensorProgress");
            final int sensorTotal = event.getData("sensorTotal");
            final VizPanel vizPanel = event.<VizPanel> getData("vizPanel");

            onDataReceived(response, start, end, sensors, sensorIndex, sensorChunkStart,
                    sensorProgress, sensorTotal, vizPanel);
        } else

        /*
         * Request for latest sensor value
         */
        if (type.equals(DataEvents.LatestValuesRequest)) {
            LOGGER.finest("LatestValuesRequest");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final VizPanel vizPanel = event.getData("vizPanel");
            onLatestValuesRequest(sensors, vizPanel);

        } else if (type.equals(DataEvents.LatestValueAjaxSuccess)) {
            LOGGER.finest("LatestValueAjaxSuccess");
            final String response = event.<String> getData("response");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final int index = event.getData("index");
            final VizPanel panel = event.<VizPanel> getData("panel");
            onLatestValueSuccess(response, sensors, index, panel);

        } else if (type.equals(DataEvents.LatestValueAjaxFailure)) {
            LOGGER.warning("LatestValueAjaxFailure");
            // final int code = event.getData("code");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final int index = event.getData("index");
            final VizPanel panel = event.<VizPanel> getData("panel");
            onLatestValueFailure(sensors, index, panel);

        } else

        if (type.equals(LoginEvents.LoggedOut)) {
            Cache.clear();
        } else

        /*
         * Something is wrong
         */
        {
            LOGGER.warning("Unexpected event received!");
        }
    }

    /**
     * Hides the progress bar View.
     */
    private void hideProgress() {
        forwardToView(this.progressDialog, new AppEvent(DataEvents.HideProgress));
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.progressDialog = new ProgressDialog(this);
    }

    private void onDataComplete(long start, long end, List<SensorModel> sensors, VizPanel vizPanel) {
        LOGGER.fine("onDataComplete...");

        hideProgress();

        // pass data on to visualization
        JsArray<Timeseries> data = Cache.request(sensors, start, end);

        LOGGER.fine("Got timeseries data out of cache...");

        vizPanel.addData(data);
    }

    private void onDataFailed(int code) {
        hideProgress();
        MessageBox.alert(null, "Data request failed! Please try again.", null);
    }

    private void onDataReceived(String response, long start, long end, List<SensorModel> sensors,
            int sensorIndex, long sensorChunkStart, int sensorProgress, int sensorTotal,
            VizPanel vizPanel) {

        // update UI before parsing data
        updateSubProgress(-1, -1, "Parsing received data chunk...");

        // parse the incoming data
        GetSensorDataResponseJso jsoResponse = GetSensorDataResponseJso.create(response);

        if (sensorProgress == 0) {
            sensorTotal = jsoResponse.getTotal(); // store the total count
        }

        // store data in cache
        SensorModel sensor = sensors.get(sensorIndex);
        Cache.store(sensor, start, end, jsoResponse.getData());
        // get the date of the last datapoint
        JsArray<BackEndDataPoint> data = jsoResponse.getData();

        // update progress
        sensorProgress = sensorProgress + data.length();

        // update start of next chunk
        if (data.length() > 0) {
            BackEndDataPoint last = data.get(data.length() - 1);
            double lastDate = Double.parseDouble(last.getDate());
            sensorChunkStart = Math.round(lastDate * 1000) + 1;

            // if interval is used no total will be given
            if (sensorTotal == -1)
                sensorTotal = data.length();
        }

        // update UI after parsing data
        updateSubProgress(sensorProgress, sensorTotal, "Requesting data chunk...from:" + start
                + " total:" + sensorTotal + " got:" + sensorProgress);

        // check if there are more pages to request for this sensor
        if (sensorProgress >= sensorTotal) {
            // completed all pages for this sensor
            sensorIndex++;
            sensorTotal = 0;
            sensorChunkStart = 0;
            sensorProgress = 0;
            updateMainProgress(Math.min(sensorIndex, sensors.size()), sensors.size());
        } else {
            // continue with next chunk
        }

        // check if there are still sensors left to do
        if (sensorIndex < sensors.size()) {
            requestData(start, end, sensors, sensorIndex, sensorChunkStart, sensorProgress,
                    sensorTotal, vizPanel);
        } else {
            // completed all pages for all sensors
            onDataComplete(start, end, sensors, vizPanel);
        }
    }

    private void onDataRequest(long start, long end, List<SensorModel> sensors, VizPanel vizPanel) {
        final int sensorIndex = 0;
        final int sensorProgress = 0, sensorTotal = 0;
        final long sensorChunkStart = start;
        showProgress(sensors.size());
        requestData(start, end, sensors, sensorIndex, sensorChunkStart, sensorProgress,
                sensorTotal, vizPanel);
    }

    private void onLatestValueFailure(List<SensorModel> sensors, int index, VizPanel vizPanel) {
        // TODO Auto-generated method stub

    }

    private void onLatestValuesComplete(List<SensorModel> sensors, VizPanel panel) {
        LOGGER.finest("Latest values complete...");
        panel.addData(Cache.request(sensors, 0, System.currentTimeMillis()));
    }

    private void onLatestValuesRequest(List<SensorModel> sensors, VizPanel panel) {

        for (SensorModel sensor : sensors) {
            Cache.remove(sensor);
        }
        int index = 0;
        getLatestValues(sensors, index, panel);
    }

    private void onLatestValueSuccess(String response, List<SensorModel> sensors, int index,
            VizPanel panel) {

        GetSensorDataResponseJso jso = GetSensorDataResponseJso.create(response);
        if (jso.getTotal() == 1) {
            Cache.store(sensors.get(index), 0, 0, jso.getData());
        } else if (jso.getTotal() != 0) {
            LOGGER.warning("Unexpected total: " + jso.getTotal());
        }

        index++;
        getLatestValues(sensors, index, panel);
    }

    private void onRefreshRequest(List<SensorModel> sensors, long start, VizPanel vizPanel) {

        onDataRequest(start, System.currentTimeMillis(), sensors, vizPanel);
    }

    private void requestData(long start, long end, List<SensorModel> sensors, int sensorIndex,
            long sensorChunkStart, int sensorProgress, int sensorTotal, VizPanel vizPanel) {
        // logger.fine( "requestData...");

        if (sensorIndex < sensors.size()) {

            SensorModel sensor = sensors.get(sensorIndex);

            // check if the sensor has cached data
            long realStart = start;
            if (sensorTotal == 0) {
                JsArray<Timeseries> cacheContent = Cache.request(Arrays.asList(sensor), start, end);
                for (int i = 0; i < cacheContent.length(); i++) {
                    Timeseries timeseries = cacheContent.get(i);
                    if (timeseries.getStart() <= realStart) {
                        realStart = timeseries.getEnd();
                        LOGGER.fine("Using data from cache to limit request period");
                    } else {
                        Cache.remove(sensor);
                    }
                }
            }

            final String method = "GET";
            String url = Urls.DATA.replace("<id>", "" + sensor.getId());

            url += "?per_page=" + PER_PAGE;
            url += "&start_date=" + NumberFormat.getFormat("#.000").format(realStart / 1000d);
            String totalStr = "&total=1";
            
            if ((end - start) / 1000 >= 3600) { // only get 1000 points when the time range is >= 1 hour
                url += "&interval="+Math.ceil(((double)(end - start) / 1000000d));
                totalStr = ""; // with interval the max can be calculated no need for total
            }            
           
            if (-1 != sensor.getAlias()) {
            	url += "&alias=" + sensor.getAlias();
            }
            if (sensorTotal == 0) {
                url += "&end_date=" + NumberFormat.getFormat("#.000").format(end / 1000d);
                url += totalStr;
            }
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(DataEvents.AjaxDataSuccess);
            onSuccess.setData("start", start);
            onSuccess.setData("end", end);
            onSuccess.setData("sensors", sensors);
            onSuccess.setData("sensorIndex", sensorIndex);
            onSuccess.setData("sensorChunkStart", sensorChunkStart);
            onSuccess.setData("sensorProgress", sensorProgress);
            onSuccess.setData("sensorTotal", sensorTotal);
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
            onDataComplete(start, end, sensors, vizPanel);
        }
    }

    private void showProgress(int tasks) {
        AppEvent showProgress = new AppEvent(DataEvents.ShowProgress);
        showProgress.setData("tasks", tasks);
        forwardToView(progressDialog, showProgress);
    }

    private void updateMainProgress(int progress, int total) {
        // logger.fine( "updateMainProgress...");
        AppEvent update = new AppEvent(DataEvents.UpdateMainProgress);
        update.setData("progress", progress);
        update.setData("total", total);
        forwardToView(progressDialog, update);
    }

    private void updateSubProgress(double progress, double total, String text) {
        // logger.fine( "updateSubProgress...");
        AppEvent update = new AppEvent(DataEvents.UpdateDataProgress);
        update.setData("progress", progress);
        update.setData("total", total);
        update.setData("text", text);
        forwardToView(progressDialog, update);
    }

}
