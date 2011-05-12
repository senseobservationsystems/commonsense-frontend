package nl.sense_os.commonsense.client.env.list;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.json.parsers.EnvironmentParser;
import nl.sense_os.commonsense.client.env.create.EnvCreateEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.viz.tabs.VizEvents;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.EnvironmentModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EnvController extends Controller {

    private static final String TAG = "EnvController";
    private View grid;

    public EnvController() {
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
        final String url = Constants.URL_ENVIRONMENTS + "/" + environment.getId();
        final String sessionId = Registry.get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(EnvEvents.DeleteAjaxSuccess);
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
            // Log.d(TAG, "LoadRequest");
            final AsyncCallback<List<EnvironmentModel>> callback = event
                    .<AsyncCallback<List<EnvironmentModel>>> getData();
            requestList(callback);

        } else if (type.equals(EnvEvents.ListAjaxSuccess)) {
            // Log.d(TAG, "FullDetailsAjaxSuccess");
            final String response = event.getData("response");
            final AsyncCallback<List<EnvironmentModel>> callback = event
                    .<AsyncCallback<List<EnvironmentModel>>> getData("callback");
            onListSuccess(response, callback);

        } else if (type.equals(EnvEvents.ListAjaxFailure)) {
            Log.w(TAG, "FullDetailsAjaxFailure");
            final AsyncCallback<List<EnvironmentModel>> callback = event
                    .<AsyncCallback<List<EnvironmentModel>>> getData("callback");
            onListFailure(callback);

        } else

        if (type.equals(EnvEvents.DeleteRequest)) {
            // Log.d(TAG, "DeleteRequest");
            final EnvironmentModel environment = event.getData("environment");
            delete(environment);

        } else if (type.equals(EnvEvents.DeleteAjaxSuccess)) {
            // Log.d(TAG, "DeleteAjaxSuccess");
            // final String response = event.getData("response");
            onDeleteSuccess();

        } else if (type.equals(EnvEvents.DeleteAjaxFailure)) {
            Log.w(TAG, "DeleteAjaxFailure");
            // final int code = event.getData("code");
            onDeleteFailure();

        } else

        {
            forwardToView(this.grid, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.grid = new EnvGrid(this);
    }

    private void onDeleteFailure() {
        forwardToView(this.grid, new AppEvent(EnvEvents.DeleteFailure));
    }

    private void onDeleteSuccess() {
        Dispatcher.forwardEvent(EnvEvents.DeleteSuccess);
    }

    private void onListFailure(AsyncCallback<List<EnvironmentModel>> callback) {
        forwardToView(this.grid, new AppEvent(EnvEvents.Done));
        if (null != callback) {
            callback.onFailure(null);
        }
    }

    private void onListSuccess(String response, AsyncCallback<List<EnvironmentModel>> callback) {
        List<EnvironmentModel> environments = new ArrayList<EnvironmentModel>();
        EnvironmentParser.parseList(response, environments);
        forwardToView(this.grid, new AppEvent(EnvEvents.Done));
        Dispatcher.forwardEvent(EnvEvents.ListUpdated);
        if (null != callback) {
            callback.onSuccess(environments);
        }
    }

    private void requestList(AsyncCallback<List<EnvironmentModel>> callback) {

        forwardToView(this.grid, new AppEvent(EnvEvents.Working));

        // prepare request properties
        final String method = "GET";
        final String url = Constants.URL_ENVIRONMENTS;
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
