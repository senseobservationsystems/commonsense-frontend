package nl.sense_os.commonsense.client.viz.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.json.overlays.DataPoint;
import nl.sense_os.commonsense.client.common.json.overlays.SensorDataResponse;
import nl.sense_os.commonsense.client.common.json.overlays.Timeseries;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.viz.data.cache.Cache;
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

    private static final Logger logger = Logger.getLogger("DataController");
    private View progressDialog;
    private static final int PER_PAGE = 1000; // max: 1000
    private int totalData[];

    public DataController() {
        registerEventTypes(DataEvents.DataRequest, DataEvents.RefreshRequest);
        registerEventTypes(DataEvents.AjaxDataFailure, DataEvents.AjaxDataSuccess);
        registerEventTypes(LoginEvents.LoggedOut);
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(DataEvents.DataRequest)) {
            // logger.fine( "DataRequest");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final long start = event.getData("startTime");
            final long end = event.getData("endTime");
            final VizPanel vizPanel = event.getData("vizPanel");

            onDataRequest(start, end, sensors, vizPanel);

        } else if (type.equals(DataEvents.RefreshRequest)) {
            // logger.fine( "DataRequest");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final long start = event.getData("start");
            final VizPanel vizPanel = event.getData("vizPanel");

            onRefreshRequest(sensors, start, vizPanel);

        } else if (type.equals(DataEvents.AjaxDataFailure)) {
            logger.warning("AjaxDataFailure");
            final int code = event.getData("code");

            onDataFailed(code);

        } else if (type.equals(DataEvents.AjaxDataSuccess)) {
            // logger.fine( "AjaxDataSuccess");
            final String response = event.<String> getData("response");
            final long start = event.getData("start");
            final long end = event.getData("end");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final int sensorIndex = event.getData("sensorIndex");
            final int pageIndex = event.getData("pageIndex");
            final VizPanel vizPanel = event.<VizPanel> getData("vizPanel");

            onDataReceived(response, start, end, sensors, sensorIndex, pageIndex, vizPanel);
        } else if (type.equals(LoginEvents.LoggedOut)) {
            Cache.clear();
        } else {
            logger.warning("Unexpected event received!");
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
        // logger.fine( "onDataComplete...");

        hideProgress();

        // pass data on to visualization
        vizPanel.addData(Cache.request(sensors));
    }

    private void onDataFailed(int code) {
        hideProgress();
        MessageBox.alert(null, "Data request failed! Please try again.", null);
    }

    private void onDataReceived(String response, long start, long end, List<SensorModel> sensors,
            int sensorIndex, int pageIndex, VizPanel vizPanel) {
        // logger.fine( "onDataReceived...");

        updateSubProgress(-1, -1, "Parsing received data chunk...");

        // parse the incoming data
        SensorDataResponse jsoResponse = SensorDataResponse.create(response);        
        int total = jsoResponse.getTotal();
        
        if(pageIndex > 0)
        	total = totalData[sensorIndex];
        else
        	totalData[sensorIndex] = total; // store the total count
        
        // store data in cache
        SensorModel sensor = sensors.get(sensorIndex);
        Cache.store(sensor, start, end, jsoResponse.getData());
        // get the date of the last datapoint
        JsArray<DataPoint> data= jsoResponse.getData();        
        if(data.length() > 0)
        {
        	DataPoint last = data.get(data.length()-1);        
        	start = last.getTimestamp().getTime()+1;
        }
        // update UI after parsing data
        final int offset = pageIndex * PER_PAGE;
        final int increment = PER_PAGE;
        final int progress = Math.min(offset + increment, total);
        updateSubProgress(progress, total, "Requesting data chunk...from:"+start+" total:"+total+ " got:"+(PER_PAGE * (pageIndex + 1)));

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
        	 System.out.println("getting the rest pageIndex:"+pageIndex);
            requestData(start, end, sensors, sensorIndex, pageIndex, vizPanel);
        } else {
            // completed all pages for all sensors
        	 System.out.println("datacomplete");
            onDataComplete(start, end, sensors, vizPanel);
        }
    }

    private void onDataRequest(long start, long end, List<SensorModel> sensors, VizPanel vizPanel) {
        final int page = 0;
        final int index = 0;
        totalData = new int[sensors.size()];
        showProgress(sensors.size());
        requestData(start, end, sensors, index, page, vizPanel);
    }

    private void onRefreshRequest(List<SensorModel> sensors, long start, VizPanel vizPanel) {

        // get the oldest cached entry for each sensor
        // not very important to get the optimal value: cache will be checked again in requestData()
        onDataRequest(start, System.currentTimeMillis(), sensors, vizPanel);
    }

    private void requestData(long start, long end, List<SensorModel> sensors, int sensorIndex,
            int pageIndex, VizPanel vizPanel) {
        // logger.fine( "requestData...");

        if (sensorIndex < sensors.size()) {

            SensorModel sensor = sensors.get(sensorIndex);

            // check if the sensor has cached data
            long realStart = start;
            if (pageIndex == 0) {
                JsArray<Timeseries> cacheContent = Cache.request(Arrays.asList(sensor));
                for (int i = 0; i < cacheContent.length(); i++) {
                    Timeseries timeseries = cacheContent.get(i);
                    if (timeseries.getStart() <= realStart) {
                        realStart = timeseries.getEnd();
                        // logger.fine( "Changed realStart to " + realStart);
                    } else {
                        Cache.remove(sensor);
                    }
                }
            }

            final String method = "GET";
            String url = Urls.DATA.replace("<id>", sensor.getId());
            
            url += "?per_page=" + PER_PAGE;
            url += "&start_date=" + NumberFormat.getFormat("#.000").format(realStart / 1000d);
            if(pageIndex == 0)
            {
            	url += "&end_date=" + NumberFormat.getFormat("#.000").format(end / 1000d);            
            	url += "&total=1";
            }
            if((end-start)/1000 >= 2419200)
            {
            	url +="&interval=3600";
            }
            else if((end-start)/1000 >= 604800)
            {
            	url +="&interval=420";
            }
            if (null != sensor.get(SensorModel.ALIAS)) {
                url += "&alias=" + sensor.get(SensorModel.ALIAS);
            }
            final String sessionId = Registry.get(Constants.REG_SESSION_ID);
            final AppEvent onSuccess = new AppEvent(DataEvents.AjaxDataSuccess);
            onSuccess.setData("start", start);
            onSuccess.setData("end", end);
            onSuccess.setData("sensors", sensors);
            onSuccess.setData("sensorIndex", sensorIndex);
            onSuccess.setData("pageIndex", pageIndex);
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
