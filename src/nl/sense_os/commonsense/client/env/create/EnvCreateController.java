package nl.sense_os.commonsense.client.env.create;

import java.util.List;
import java.util.Map;

import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.EnvironmentModel;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.Polygon;

public class EnvCreateController extends Controller {

    private static final String TAG = "EnvCreateController";
    private View creator;

    public EnvCreateController() {
        registerEventTypes(EnvCreateEvents.ShowCreator);
        registerEventTypes(EnvCreateEvents.Forward, EnvCreateEvents.Back, EnvCreateEvents.Cancel);
        registerEventTypes(EnvCreateEvents.OutlineComplete);
        registerEventTypes(EnvCreateEvents.CreateRequest, EnvCreateEvents.CreateSuccess,
                EnvCreateEvents.CreateFailure, EnvCreateEvents.CreateAjaxSuccess,
                EnvCreateEvents.CreateAjaxFailure);
    }

    private void create(String name, int floors, Polygon outline,
            Map<Marker, List<SensorModel>> sensors) {

        // create GPS outline String
        String gpsOutline = "";
        for (int i = 0; i < outline.getVertexCount(); i++) {
            LatLng vertex = outline.getVertex(i);
            gpsOutline += vertex.toUrlValue() + ";";
        }
        gpsOutline = gpsOutline.substring(0, gpsOutline.length() - 1);

        // create GPS position String
        String position = outline.getBounds().getCenter().toUrlValue();

        // prepare request properties
        final String method = "POST";
        final String url = Constants.URL_ENVIRONMENTS + ".json";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(EnvCreateEvents.CreateAjaxSuccess);
        onSuccess.setData("sensors", sensors);
        final AppEvent onFailure = new AppEvent(EnvCreateEvents.CreateAjaxFailure);

        String body = "{\"environment\":{";
        body += "\"" + EnvironmentModel.NAME + "\":\"" + name + "\",";
        body += "\"" + EnvironmentModel.FLOORS + "\":" + floors + ",";
        body += "\"" + EnvironmentModel.OUTLINE + "\":\"" + gpsOutline + "\",";
        body += "\"" + EnvironmentModel.POSITION + "\":\"" + position + "\"}";
        body += "}";

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("body", body);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);

        Dispatcher.forwardEvent(ajaxRequest);
    }

    private void onCreateSuccess(String response, Map<Marker, List<SensorModel>> sensors) {
        Dispatcher.forwardEvent(EnvCreateEvents.CreateSuccess);
    }

    private void markPosition(Map<Marker, List<SensorModel>> sensors, int index) {
        // TODO
    }
    private void onCreateFailure() {
        forwardToView(this.creator, new AppEvent(EnvCreateEvents.CreateFailure));
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(EnvCreateEvents.CreateRequest)) {
            Log.d(TAG, "CreateRequest");
            final String name = event.<String> getData("name");
            final int floors = event.getData("floors");
            final Polygon outline = event.<Polygon> getData("outline");
            final Map<Marker, List<SensorModel>> sensors = event
                    .<Map<Marker, List<SensorModel>>> getData("sensors");
            create(name, floors, outline, sensors);

        } else if (type.equals(EnvCreateEvents.CreateAjaxSuccess)) {
            Log.d(TAG, "CreateAjaxSuccess");
            final String response = event.<String> getData("response");
            final Map<Marker, List<SensorModel>> sensors = event.getData("sensors");
            onCreateSuccess(response, sensors);

        } else if (type.equals(EnvCreateEvents.CreateAjaxFailure)) {
            Log.w(TAG, "CreateAjaxFailure");
            // final int code = event.getData("code");
            onCreateFailure();

        } else

        {
            forwardToView(this.creator, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.creator = new EnvCreator(this);
    }

}
