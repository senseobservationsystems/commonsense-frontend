package nl.sense_os.commonsense.client.viz.data;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
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
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;

public class DataController extends Controller {

    private static final Logger LOG = Logger.getLogger(DataController.class.getName());
    private View progressDialog;
    private static final int PER_PAGE = 1000; // max: 1000

    public DataController() {

        LOG.setLevel(Level.WARNING);

        registerEventTypes(DataEvents.DataRequest);
        registerEventTypes(LoginEvents.LoggedOut);
        registerEventTypes(DataEvents.LatestValuesRequest);
    }

    private void getLatestValues(final List<SensorModel> sensors, final int index,
            final VizPanel panel) {
        if (index < sensors.size()) {

            SensorModel sensor = sensors.get(index);

            final Method method = RequestBuilder.GET;
            String url = Urls.SENSORS + "/" + sensor.getId() + "/data.json" + "?last=1";
            if (-1 != sensor.getAlias()) {
                url += "&alias=" + sensor.getAlias();
            }
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);

            // prepare request callback
            RequestCallback reqCallback = new RequestCallback() {

                @Override
                public void onError(Request request, Throwable exception) {
                    LOG.warning("GET last data onError callback: " + exception.getMessage());
                    onLatestValueFailure(sensors, index, panel);
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    LOG.finest("GET last data response received: " + response.getStatusText());
                    int statusCode = response.getStatusCode();
                    if (Response.SC_OK == statusCode) {
                        onLatestValueSuccess(response.getText(), sensors, index, panel);
                    } else {
                        LOG.warning("GET last data returned incorrect status: " + statusCode);
                        onLatestValueFailure(sensors, index, panel);
                    }
                }
            };

            // send request
            RequestBuilder builder = new RequestBuilder(method, url);
            builder.setHeader("X-SESSION_ID", sessionId);
            try {
                builder.sendRequest(null, reqCallback);
            } catch (RequestException e) {
                LOG.warning("GET slast data request threw exception: " + e.getMessage());
                onLatestValueFailure(sensors, index, panel);
            }

        } else {
            // hoooray we're done!
            onLatestValuesComplete(sensors, panel);
        }
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(DataEvents.DataRequest)) {
            LOG.finest("DataRequest");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final long start = event.getData("startTime");
            final long end = event.getData("endTime");
            final VizPanel vizPanel = event.getData("vizPanel");
            final boolean showProgress = event.getData("showProgress");

            onDataRequest(start, end, sensors, vizPanel, showProgress);

        } else

        /*
         * Request for latest sensor value
         */
        if (type.equals(DataEvents.LatestValuesRequest)) {
            LOG.finest("LatestValuesRequest");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final VizPanel vizPanel = event.getData("vizPanel");
            onLatestValuesRequest(sensors, vizPanel);

        } else

        if (type.equals(LoginEvents.LoggedOut)) {
            Cache.clear();
        } else

        /*
         * Something is wrong
         */
        {
            LOG.warning("Unexpected event received!");
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
        LOG.fine("onDataComplete...");

        hideProgress();

        // pass data on to visualization
        JsArray<Timeseries> data = Cache.request(sensors, start, end);
        vizPanel.addData(data);
    }

    private void onDataFailed(int code, boolean showProgress) {
        if (showProgress) {
            hideProgress();
            MessageBox.alert(null, "Data request failed! Please try again.", null);
        }
    }

    private void onDataReceived(String response, long start, long end, List<SensorModel> sensors,
            int sensorIndex, long sensorChunkStart, int sensorProgress, int sensorTotal,
            VizPanel vizPanel, boolean showProgress) {

        // parse the incoming data
        GetSensorDataResponseJso jsoResponse = GetSensorDataResponseJso.create(response);

        // store data in cache
        SensorModel sensor = sensors.get(sensorIndex);
        Cache.store(sensor, start, end, jsoResponse.getData());
        // get the date of the last datapoint
        JsArray<BackEndDataPoint> data = jsoResponse.getData();

        // save the total number of points if this is the first request for this sensor
        if (sensorProgress == 0) {
            sensorTotal = jsoResponse.getTotal(); // store the total count
        }
        if (sensorTotal == -1) {
            sensorTotal = data.length();
        }

        // update progress
        sensorProgress = sensorProgress + data.length();

        // update start of next chunk
        if (data.length() > 0) {
            BackEndDataPoint last = data.get(data.length() - 1);
            double lastDate = Double.parseDouble(last.getDate());
            sensorChunkStart = Math.round(lastDate * 1000) + 1;
        }

        // check if there are more pages to request for this sensor
        if (sensorProgress >= sensorTotal) {
            // completed all pages for this sensor
            sensorIndex++;
            sensorTotal = 0;
            sensorChunkStart = 0;
            sensorProgress = 0;

            if (showProgress) {
                updateProgress(Math.min(sensorIndex, sensors.size()), sensors.size());
            }
        } else {
            // continue with next chunk
            LOG.warning("Next chunk?! sensorProgress=" + sensorProgress + ", sensorTotal="
                    + sensorTotal);
        }

        // check if there are still sensors left to do
        if (sensorIndex < sensors.size()) {
            requestData(start, end, sensors, sensorIndex, sensorChunkStart, sensorProgress,
                    sensorTotal, vizPanel, showProgress);
        } else {
            // completed all pages for all sensors
            onDataComplete(start, end, sensors, vizPanel);
        }
    }

    private void onDataRequest(long start, long end, List<SensorModel> sensors, VizPanel vizPanel,
            boolean showProgress) {
        final int sensorIndex = 0;
        final int sensorProgress = 0;
        final int sensorTotal = 0;
        final long sensorChunkStart = start;

        if (showProgress) {
            showProgress(sensors.size());
        }

        LOG.fine("request start: "
                + DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL).format(new Date(start)));

        requestData(start, end, sensors, sensorIndex, sensorChunkStart, sensorProgress,
                sensorTotal, vizPanel, showProgress);
    }

    private void onLatestValueFailure(List<SensorModel> sensors, int index, VizPanel vizPanel) {
        // TODO Auto-generated method stub

    }

    private void onLatestValuesComplete(List<SensorModel> sensors, VizPanel panel) {
        LOG.finest("Latest values complete...");
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
        Cache.store(sensors.get(index), 0, 0, jso.getData());

        index++;
        getLatestValues(sensors, index, panel);
    }

    private void requestData(long start, final long end, final List<SensorModel> sensors,
            final int sensorIndex, final long sensorChunkStart, final int sensorProgress,
            final int sensorTotal, final VizPanel vizPanel, final boolean showProgress) {
        LOG.fine("requestData...");

        if (sensorIndex < sensors.size()) {

            SensorModel sensor = sensors.get(sensorIndex);

            // remove data from the cache, because using it is too complicated for our tiny brains
            Cache.remove(sensor);
            final long realStart = start;

            final Method method = RequestBuilder.GET;
            String url = Urls.SENSORS + "/" + sensor.getId() + "/data.json";

            url += "?per_page=" + PER_PAGE;
            url += "&start_date=" + NumberFormat.getFormat("#.000").format(realStart / 1000d);
            String totalStr = "";

            // request interpolation if the time range is >= 1 hour
            long endTime = end == -1 ? System.currentTimeMillis() : end;
            if ((endTime - realStart) >= 3600 * 1000) {
                url += "&interval=" + Math.ceil(((double) (endTime - realStart) / 1000000d));
                totalStr = ""; // with interval the max can be calculated no need for total
            } else {
                totalStr = "&total=1";
            }

            // use alias if necessary
            if (-1 != sensor.getAlias()) {
                url += "&alias=" + sensor.getAlias();
            }

            // there should only be one page per request
            if (sensorTotal == 0) {
                if (end != -1) {
                    url += "&end_date=" + NumberFormat.getFormat("#.000").format(end / 1000d);
                }
                url += totalStr;
            } else {
                LOG.severe("Requesting second page of data?! sensorTotal=" + sensorTotal);
            }

            final String sessionId = Registry.get(Constants.REG_SESSION_ID);

            // prepare request callback
            RequestCallback reqCallback = new RequestCallback() {

                @Override
                public void onError(Request request, Throwable exception) {
                    LOG.warning("GET data onError callback: " + exception.getMessage());
                    onDataFailed(0, showProgress);
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    LOG.finest("GET data response received: " + response.getStatusText());
                    int statusCode = response.getStatusCode();
                    if (Response.SC_OK == statusCode) {
                        onDataReceived(response.getText(), realStart, end, sensors, sensorIndex,
                                sensorChunkStart, sensorProgress, sensorTotal, vizPanel,
                                showProgress);
                    } else {
                        LOG.warning("GET data returned incorrect status: " + statusCode);
                        onDataFailed(0, showProgress);
                    }
                }
            };

            // send request
            RequestBuilder builder = new RequestBuilder(method, url);
            builder.setHeader("X-SESSION_ID", sessionId);
            try {
                builder.sendRequest(null, reqCallback);
            } catch (RequestException e) {
                LOG.warning("GET data request threw exception: " + e.getMessage());
                onDataFailed(0, showProgress);
            }

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

    private void updateProgress(int progress, int total) {
        LOG.finest("Update progress...");
        AppEvent update = new AppEvent(DataEvents.UpdateMainProgress);
        update.setData("progress", progress);
        update.setData("total", total);
        forwardToView(progressDialog, update);
    }
}
