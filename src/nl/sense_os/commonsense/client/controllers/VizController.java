package nl.sense_os.commonsense.client.controllers;

import nl.sense_os.commonsense.client.controllers.cors.DataJsniRequests;
import nl.sense_os.commonsense.client.events.LoginEvents;
import nl.sense_os.commonsense.client.events.MainEvents;
import nl.sense_os.commonsense.client.events.StateEvents;
import nl.sense_os.commonsense.client.events.VizEvents;
import nl.sense_os.commonsense.client.map.MapEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.views.VizMap;
import nl.sense_os.commonsense.client.views.VizTypeChooser;
import nl.sense_os.commonsense.client.views.VizView;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.sensorvalues.SensorValueModel;
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
import com.google.gwt.user.client.Timer;
import com.google.gwt.visualization.client.VisualizationUtils;

public class VizController extends Controller {

    private static final String TAG = "VizController";
    private View vizView;
    private View typeChooser;
    private View mapView;
    private boolean isVizApiLoaded;

    public VizController() {
        registerEventTypes(MainEvents.ShowVisualization);
        registerEventTypes(LoginEvents.LoggedIn, LoginEvents.LoggedOut);
        registerEventTypes(VizEvents.DataRequested, VizEvents.DataNotReceived, VizEvents.DataReceived);
        registerEventTypes(VizEvents.ShowTypeChoice, VizEvents.TypeChoiceCancelled);
        registerEventTypes(VizEvents.ShowLineChart, VizEvents.ShowTable, VizEvents.ShowNetwork);
        registerEventTypes(VizEvents.ShowMap, VizEvents.MapReady);
        registerEventTypes(StateEvents.FeedbackReady, StateEvents.FeedbackComplete, StateEvents.FeedbackCancelled);
        
        registerEventTypes(MapEvents.MapReady);
        
        loadVizApi();
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        
        if (type.equals(VizEvents.DataRequested)) {
            Log.d(TAG, "DataRequested");
            requestData(event);
            
        } else if (type.equals(VizEvents.ShowTypeChoice)
                || type.equals(VizEvents.TypeChoiceCancelled)) {
            forwardToView(this.typeChooser, event);

        // @@ TODO: remove this!!!            
        } else if (type.equals(VizEvents.ShowMap)) {
            forwardToView(this.mapView, event);
            
        } else if (type.equals(MapEvents.MapReady)) {
        	forwardToView(this.mapView, event);        	
        	
        } else {
            forwardToView(this.vizView, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.vizView = new VizView(this);
        this.typeChooser = new VizTypeChooser(this);
        this.mapView = new VizMap(this);
    }

    private void loadVizApi() {

        // Load the visualization API
        this.isVizApiLoaded = false;
        final Runnable vizCallback = new Runnable() {

            @Override
            public void run() {
                // Log.d(TAG, "onLoadVisualizationApi");
                isVizApiLoaded = true;
            }
        };
        VisualizationUtils.loadVisualizationApi(vizCallback, Timeline.PACKAGE);

        // double check that the API has been loaded
        Timer timer = new Timer() {

            @Override
            public void run() {
                if (false == isVizApiLoaded) {
                    MessageBox.alert("CommonSense",
                            "Google visualization API not loaded, please retry.",
                            new Listener<MessageBoxEvent>() {

                                @Override
                                public void handleEvent(MessageBoxEvent be) {
                                    loadVizApi();
                                }
                            });
                }
            }
        };
        timer.schedule(1000 * 10);
    }

    public void onDataAuthError() {
        Dispatcher.forwardEvent(VizEvents.DataNotReceived);
    }

    public void onDataFailed(TreeModel tag) {
        Dispatcher.forwardEvent(VizEvents.DataNotReceived);
    }

    public void onDataReceived(TaggedDataModel data) {
        Dispatcher.forwardEvent(VizEvents.DataReceived, data);
    }

    private void requestData(AppEvent event) {
    	Log.d(TAG, "requestData");
    	
        SensorValueModel[] pagedValues = new SensorValueModel[0];
        int page = 0;

        TreeModel sensor = event.getData("tag");
        String url = Constants.URL_DATA.replace("<id>", "" + sensor.<String> get("id"));
        url += "?page=" + page;
        url += "&per_page=" + 1000;
        url += "&start_date=" + event.<Double> getData("startDate");
        url += "&end_date=" + event.<Double> getData("endDate");

        String alias = sensor.get("alias");
        if (null != alias) {
            url += "&alias=" + alias;
        }
        String sessionId = Registry.get(Constants.REG_SESSION_ID);

        sensor.set("retryCount", 0);
        DataJsniRequests.requestData(url, sessionId, sensor, page, pagedValues, this);
    }
}
