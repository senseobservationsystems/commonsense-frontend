package nl.sense_os.commonsense.client.env.list;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.models.EnvironmentModel;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.env.create.EnvCreateEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.viz.tabs.VizEvents;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EnvController extends Controller {

    private static final Logger LOG = Logger.getLogger(EnvController.class.getName());
    private View grid;

    public EnvController() {

        LOG.setLevel(Level.WARNING);

        // events to update the list of groups
        registerEventTypes(MainEvents.Init);
        registerEventTypes(VizEvents.Show);
        registerEventTypes(LoginEvents.LoggedOut);
        registerEventTypes(EnvEvents.ShowGrid);

        registerEventTypes(EnvEvents.ListRequested, EnvEvents.ListUpdated,
                EnvEvents.ListAjaxSuccess, EnvEvents.ListAjaxFailure);

        registerEventTypes(EnvEvents.DeleteRequest, EnvEvents.DeleteAjaxSuccess,
                EnvEvents.DeleteAjaxFailure, EnvEvents.DeleteSuccess);

        registerEventTypes(EnvCreateEvents.CreateSuccess);
    }

    private void delete(EnvironmentModel environment) {

        // prepare request properties
        final String method = "DELETE";
        final String url = Urls.ENVIRONMENTS + "/" + environment.getId() + ".json";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(EnvEvents.DeleteAjaxSuccess);
        onSuccess.setData("environment", environment);
        final AppEvent onFailure = new AppEvent(EnvEvents.DeleteAjaxSuccess);

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);

        Dispatcher.forwardEvent(ajaxRequest);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(EnvEvents.ListRequested)) {
            LOG.fine("LoadRequest");
            final AsyncCallback<List<EnvironmentModel>> callback = event
                    .<AsyncCallback<List<EnvironmentModel>>> getData();
            requestList(callback);

        } else if (type.equals(EnvEvents.ListAjaxSuccess)) {
            LOG.fine("ListAjaxSuccess");
            final String response = event.getData("response");
            final AsyncCallback<List<EnvironmentModel>> callback = event
                    .<AsyncCallback<List<EnvironmentModel>>> getData("callback");
            onListSuccess(response, callback);

        } else if (type.equals(EnvEvents.ListAjaxFailure)) {
            LOG.warning("ListAjaxFailure");
            final AsyncCallback<List<EnvironmentModel>> callback = event
                    .<AsyncCallback<List<EnvironmentModel>>> getData("callback");
            onListFailure(callback);

        } else

        if (type.equals(EnvEvents.DeleteRequest)) {
            LOG.fine("DeleteRequest");
            final EnvironmentModel environment = event.getData("environment");
            delete(environment);

        } else if (type.equals(EnvEvents.DeleteAjaxSuccess)) {
            LOG.fine("DeleteAjaxSuccess");
            // final String response = event.getData("response");
            final EnvironmentModel environment = event.getData("environment");
            onDeleteSuccess(environment);

        } else if (type.equals(EnvEvents.DeleteAjaxFailure)) {
            LOG.warning("DeleteAjaxFailure");
            // final int code = event.getData("code");
            onDeleteFailure();

        } else

        {
            forwardToView(grid, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        grid = new EnvGrid(this);
        Registry.register(Constants.REG_ENVIRONMENT_LIST, new ArrayList<EnvironmentModel>());
    }

    private void onDeleteFailure() {
        forwardToView(grid, new AppEvent(EnvEvents.DeleteFailure));
    }

    private void onDeleteSuccess(EnvironmentModel environment) {

        // update sensor library
        List<SensorModel> library = Registry.<List<SensorModel>> get(Constants.REG_SENSOR_LIST);
        for (SensorModel sensor : library) {
            if (sensor.getEnvironment() != null && sensor.getEnvironment().equals(environment)) {
                sensor.remove(SensorModel.ENVIRONMENT);
            }
        }

        // update global environment list
        Registry.<List<EnvironmentModel>> get(Constants.REG_ENVIRONMENT_LIST).remove(environment);

        Dispatcher.forwardEvent(EnvEvents.DeleteSuccess);
    }

    private void onListFailure(AsyncCallback<List<EnvironmentModel>> callback) {
        forwardToView(grid, new AppEvent(EnvEvents.Done));
        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void onListSuccess(String response, AsyncCallback<List<EnvironmentModel>> callback) {

        // parse the list of environments from the response
        List<EnvironmentModel> environments = new ArrayList<EnvironmentModel>();
        if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
            GetEnvironmentsResponseJso jso = JsonUtils.unsafeEval(response);
            environments = jso.getEnvironments();
        }

        Registry.<List<EnvironmentModel>> get(Constants.REG_ENVIRONMENT_LIST).addAll(environments);

        forwardToView(grid, new AppEvent(EnvEvents.Done));
        Dispatcher.forwardEvent(EnvEvents.ListUpdated);
        if (null != callback) {
            callback.onSuccess(environments);
        }
    }

    private void requestList(AsyncCallback<List<EnvironmentModel>> callback) {

        forwardToView(grid, new AppEvent(EnvEvents.Working));
        Registry.<List<EnvironmentModel>> get(Constants.REG_ENVIRONMENT_LIST).clear();

        // prepare request properties
        final String method = "GET";
        final String url = Urls.ENVIRONMENTS + ".json";
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(EnvEvents.ListAjaxSuccess);
        onSuccess.setData("callback", callback);
        final AppEvent onFailure = new AppEvent(EnvEvents.ListAjaxFailure);
        onFailure.setData("callback", callback);

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);

        Dispatcher.forwardEvent(ajaxRequest);
    }
}
