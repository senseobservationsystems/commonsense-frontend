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
import com.google.gwt.http.client.UrlBuilder;
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
            final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
            urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensor.getId() + "/data.json");
            urlBuilder.setParameter("last", "1");
            if (-1 != sensor.getAlias()) {
                urlBuilder.setParameter("alias", "" + sensor.getAlias());
            }
            final String url = urlBuilder.buildString();
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);

            // prepare request callback
            RequestCallback reqCallback = new RequestCallback() {

                @Override
                public void onError(Request request, Throwable exception) {
                    LOG.warning("GET last data onError callback: " + exception.getMessage());
                    onLatestValueFailure(0, panel);
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    LOG.finest("GET last data response received: " + response.getStatusText());
                    int statusCode = response.getStatusCode();
                    if (Response.SC_OK == statusCode) {
                        onLatestValueSuccess(response.getText(), sensors, index, panel);
                    } else {
                        LOG.warning("GET last data returned incorrect status: " + statusCode);
                        onLatestValueFailure(statusCode, panel);
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
                onLatestValueFailure(0, panel);
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
            DataRequestEvent dataRequest = (DataRequestEvent) event;
            final List<SensorModel> sensors = dataRequest.getSensors();
            final long start = dataRequest.getStart();
            final long end = dataRequest.getEnd();
            final VizPanel vizPanel = dataRequest.getPanel();
            final boolean showProgress = dataRequest.isShowProgress();
            final boolean subsample = dataRequest.isSubsample();

            onDataRequest(start, end, sensors, subsample, showProgress, vizPanel);

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

    /**
     * Handles the callbacks when data requests are completed. Hides the progress dialog and passes
     * the data on to the panel that requested the data.
     * 
     * @param start
     *            Start of time range of the finished requests.
     * @param end
     *            End of time range of the finished requests.
     * @param sensors
     *            List of sensors that the data was retrieved for.
     * @param vizPanel
     *            Panel that requested the data.
     */
    private void onDataComplete(long start, long end, List<SensorModel> sensors, VizPanel vizPanel) {
        LOG.fine("onDataComplete...");

        hideProgress();

        // pass data on to visualization
        JsArray<Timeseries> data = Cache.request(sensors, start, end);
        vizPanel.addData(data);
    }

    /**
     * Handles the event that a data request fails. If progress was being shown, it notifies the
     * user that we failed.
     * 
     * @param code
     *            Response code of the failed request.
     * @param showProgress
     *            boolean to indicate whether to notify the user.
     */
    private void onDataFailed(int code, boolean showProgress) {
        if (showProgress) {
            hideProgress();
            MessageBox.alert(null, "Data request failed! Please try again.", null);
        }
    }

    /**
     * Handles successful requests for subsampled data. Parses the response, stores it and moves on
     * to the next sensor in the list.
     * 
     * @param response
     *            Response from CommonSense back end, should contain sensor data points.
     * @param start
     *            Start of the requested time range.
     * @param end
     *            End of the requested time range, or -1 for no end time.
     * @param sensors
     *            List of sensors that we are requesting data for.
     * @param sensorIndex
     *            Index of the sensor that the data belongs to.
     * @param vizPanel
     *            Panel that requested the data.
     * @param showProgress
     *            Boolean to indicate whether to update the user of the progress.
     */
    private void onReqSubsampledSuccess(String response, long start, long end,
            List<SensorModel> sensors, int sensorIndex, VizPanel vizPanel, boolean showProgress) {

        // parse the incoming data
        GetSensorDataResponseJso jsoResponse = GetSensorDataResponseJso.create(response);

        // store data in cache
        SensorModel sensor = sensors.get(sensorIndex);
        Cache.store(sensor, start, end, jsoResponse.getData());

        // update progress indicator
        sensorIndex++;
        if (showProgress) {
            updateProgress(Math.min(sensorIndex, sensors.size()), sensors.size());
        }

        // check if there are still sensors left to do
        if (sensorIndex < sensors.size()) {
            reqDataSubsampled(start, end, sensors, sensorIndex, vizPanel, showProgress);
        } else {
            // completed all pages for all sensors
            onDataComplete(start, end, sensors, vizPanel);
        }
    }

    /**
     * Handles successful requests for paged data. Parses the response, stores it and moves on to
     * the next page, or the next sensor in the list.
     * 
     * @param response
     *            Response from CommonSense back end, should contain sensor data points.
     * @param start
     *            Start of the requested time range.
     * @param end
     *            End of the requested time range, or -1 for no end time.
     * @param sensors
     *            List of sensors that we are requesting data for.
     * @param sensorIndex
     *            Index of the sensor that the data belongs to.
     * @param pageIndex
     *            Index of the current page of data for the current sensor.
     * @param total
     *            Total amount of data that should be retrieved for the current sensor. This count
     *            is returned along with the first page of data.
     * @param vizPanel
     *            Panel that requested the data.
     * @param showProgress
     *            Boolean to indicate whether to update the user of the progress.
     */
    private void onReqPagedSuccess(String response, long start, long end,
            List<SensorModel> sensors, int sensorIndex, int pageIndex, int total,
            VizPanel vizPanel, boolean showProgress) {

        // parse the incoming data
        GetSensorDataResponseJso jsoResponse = GetSensorDataResponseJso.create(response);

        // store data in cache
        SensorModel sensor = sensors.get(sensorIndex);
        JsArray<BackEndDataPoint> data = jsoResponse.getData();
        Cache.store(sensor, start, end, data);

        // the first page also contains a total count, otherwise reuse the total from earlier pages
        if (pageIndex == 0) {
            total = jsoResponse.getTotal();
        }

        // check if we need to fetch additional pages
        if (pageIndex * PER_PAGE + data.length() < total && data.length() > 0) {
            reqDataPaged(start, end, sensors, sensorIndex, pageIndex + 1, total, vizPanel,
                    showProgress);
        } else {
            updateProgress(Math.min(sensorIndex + 1, sensors.size()), sensors.size());
            reqDataPaged(start, end, sensors, sensorIndex + 1, 0, 0, vizPanel, showProgress);
        }
    }

    /**
     * Handles request for data from the user. Shows the progress dialog and starts requesting the
     * data.
     * 
     * @param start
     *            Start of the requested time range.
     * @param end
     *            End of the requested time range, or -1 for no end time.
     * @param sensors
     *            List of sensors that we are requesting data for.
     * @param subsample
     *            Boolean to indicate whether to use subsampling or paging to request the data.
     * @param showProgress
     *            Boolean to indicate whether to inform the user of progress.
     * @param vizPanel
     *            Panel that the request originated from.
     */
    private void onDataRequest(long start, long end, List<SensorModel> sensors, boolean subsample,
            boolean showProgress, VizPanel vizPanel) {
        final int sensorIndex = 0;

        if (showProgress) {
            showProgress(sensors.size());
        }

        LOG.fine("request start: "
                + DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL).format(new Date(start)));

        if (subsample) {
            reqDataSubsampled(start, end, sensors, sensorIndex, vizPanel, showProgress);
        } else {
            reqDataPaged(start, end, sensors, sensorIndex, 0, 0, vizPanel, showProgress);
        }
    }

    /**
     * Handles a failed request for the latest data.
     * 
     * @param code
     *            Response code of the failed request.
     * @param vizPanel
     *            Panel that requested the data.
     */
    private void onLatestValueFailure(int code, VizPanel vizPanel) {
        // does nothing
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

    /**
     * Requests data from a list of sensors, between a given start and end date. CommonSense will
     * page the data to ensure that we get all data.
     * 
     * @param start
     *            Start time of the time range, in milliseconds.
     * @param end
     *            End time of the time range, in milliseconds. Set to -1 to leave the end data
     *            unspecified.
     * @param sensors
     *            List of sensors to request data for. The data is fetched for one sensor at a time.
     * @param sensorIndex
     *            Index of the current sensor in the list.
     * @param pageIndex
     *            Index of the current page.
     * @param sensorTotal
     *            Total amount of data points to retrieve for the current sensor. This number is
     *            supplied by CommonSense with the first page of data.
     * @param vizPanel
     *            Panel that requested the data.
     * @param showProgress
     *            Set to true to display a progress dialog.
     */
    private void reqDataPaged(final long start, final long end, final List<SensorModel> sensors,
            final int sensorIndex, final int pageIndex, final int sensorTotal,
            final VizPanel vizPanel, final boolean showProgress) {
        LOG.fine("Request paged data...");

        if (sensorIndex < sensors.size()) {

            final SensorModel sensor = sensors.get(sensorIndex);

            // remove preexisting data from the cache, because reusing it is too complicated
            if (pageIndex == 0) {
                Cache.remove(sensor);
            }

            final Method method = RequestBuilder.GET;
            final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
            urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensor.getId() + "/data.json");

            // paging parameters
            urlBuilder.setParameter("per_page", "" + PER_PAGE);
            urlBuilder.setParameter("page", "" + pageIndex);

            // only need a total count for the first page request
            if (0 == pageIndex) {
                urlBuilder.setParameter("total", "1");
            }

            // start date parameter
            final String startDate = NumberFormat.getFormat("#.000").format(start / 1000d);
            urlBuilder.setParameter("start_date", startDate);

            // end date is optional
            if (end != -1) {
                final String endDate = NumberFormat.getFormat("#.000").format(end / 1000d);
                urlBuilder.setParameter("end_date", endDate);
            }

            // prepare request callback
            RequestCallback reqCallback = new RequestCallback() {

                @Override
                public void onError(Request request, Throwable exception) {
                    LOG.warning("GET data (paged) onError callback: " + exception.getMessage());
                    onDataFailed(0, showProgress);
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    LOG.finest("GET data (paged) response received: " + response.getStatusText());
                    int statusCode = response.getStatusCode();
                    if (Response.SC_OK == statusCode) {
                        onReqPagedSuccess(response.getText(), start, end, sensors, sensorIndex,
                                pageIndex, sensorTotal, vizPanel, showProgress);
                    } else {
                        LOG.warning("GET data (paged) returned incorrect status: " + statusCode);
                        onDataFailed(statusCode, showProgress);
                    }
                }
            };

            // send request
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            RequestBuilder builder = new RequestBuilder(method, urlBuilder.buildString());
            builder.setHeader("X-SESSION_ID", sessionId);
            try {
                builder.sendRequest(null, reqCallback);
            } catch (RequestException e) {
                LOG.warning("GET data (paged) request threw exception: " + e.getMessage());
                onDataFailed(0, showProgress);
            }

        } else {
            // should not happen, but just in case...
            onDataComplete(start, end, sensors, vizPanel);
        }
    }

    /**
     * Requests one page of data points from a list of sensors, between a given start and end date.
     * CommonSense will subsample the data to ensure that all data fits on that one page (1000
     * points).
     * 
     * @param start
     *            Start time of the time range, in milliseconds.
     * @param end
     *            End time of the time range, in milliseconds. Set to -1 to leave the end data
     *            unspecified.
     * @param sensors
     *            List of sensors to request data for. The data is fetched for one sensor at a time.
     * @param sensorIndex
     *            Index of the current sensor in the list.
     * @param vizPanel
     *            Panel that requested the data.
     * @param showProgress
     *            Set to true to display a progress dialog.
     */
    private void reqDataSubsampled(long start, final long end, final List<SensorModel> sensors,
            final int sensorIndex, final VizPanel vizPanel, final boolean showProgress) {
        LOG.fine("Request subsampled data...");

        if (sensorIndex < sensors.size()) {

            final SensorModel sensor = sensors.get(sensorIndex);

            // remove data from the cache, because using it is too complicated for our tiny brains
            Cache.remove(sensor);
            final long realStart = start;

            final Method method = RequestBuilder.GET;
            final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
            urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensor.getId() + "/data.json");
            urlBuilder.setParameter("per_page", "" + PER_PAGE);

            // start date parameter
            final String startDate = NumberFormat.getFormat("#.000").format(realStart / 1000d);
            urlBuilder.setParameter("start_date", startDate);

            // end date is optional
            if (end != -1) {
                final String endDate = NumberFormat.getFormat("#.000").format(end / 1000d);
                urlBuilder.setParameter("end_date", endDate);
            }

            // set subsample interval to get max 1000 points
            long realEnd = end == -1 ? System.currentTimeMillis() : end;
            final String interval = "" + Math.ceil(((realEnd - realStart) / 1000000d));
            urlBuilder.setParameter("interval", interval);

            // use alias if necessary
            if (-1 != sensor.getAlias()) {
                urlBuilder.setParameter("alias", "" + sensor.getAlias());
            }

            final String sessionId = Registry.get(Constants.REG_SESSION_ID);

            // prepare request callback
            RequestCallback reqCallback = new RequestCallback() {

                @Override
                public void onError(Request request, Throwable exception) {
                    LOG.warning("GET subsampled data onError callback: " + exception.getMessage());
                    onDataFailed(0, showProgress);
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    LOG.finest("GET data (subsampled) response received: "
                            + response.getStatusText());
                    int statusCode = response.getStatusCode();
                    if (Response.SC_OK == statusCode) {
                        onReqSubsampledSuccess(response.getText(), realStart, end, sensors,
                                sensorIndex, vizPanel, showProgress);
                    } else {
                        LOG.warning("GET data (subsampled) returned incorrect status: "
                                + statusCode);
                        onDataFailed(statusCode, showProgress);
                    }
                }
            };

            // send request
            RequestBuilder builder = new RequestBuilder(method, urlBuilder.buildString());
            builder.setHeader("X-SESSION_ID", sessionId);
            try {
                builder.sendRequest(null, reqCallback);
            } catch (RequestException e) {
                LOG.warning("GET data (subsampled) request threw exception: " + e.getMessage());
                onDataFailed(0, showProgress);
            }

        } else {
            // should not happen, but just in case...
            onDataComplete(start, end, sensors, vizPanel);
        }
    }

    /**
     * Shows a dialog to track the progress of a set of data requests.
     * 
     * @param tasks
     *            The total number of data requests that will have to be done.
     */
    private void showProgress(int tasks) {
        AppEvent showProgress = new AppEvent(DataEvents.ShowProgress);
        showProgress.setData("tasks", tasks);
        forwardToView(progressDialog, showProgress);
    }

    /**
     * Updates the progress dialog.
     * 
     * @param progress
     *            Number of completed tasks.
     * @param total
     *            Total number of tasks.
     * @see #showProgress(int)
     */
    private void updateProgress(int progress, int total) {
        LOG.finest("Update progress...");
        AppEvent update = new AppEvent(DataEvents.UpdateMainProgress);
        update.setData("progress", progress);
        update.setData("total", total);
        forwardToView(progressDialog, update);
    }
}
