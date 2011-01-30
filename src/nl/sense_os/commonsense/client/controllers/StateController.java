package nl.sense_os.commonsense.client.controllers;

import java.util.List;

import nl.sense_os.commonsense.client.events.LoginEvents;
import nl.sense_os.commonsense.client.events.MainEvents;
import nl.sense_os.commonsense.client.events.StateEvents;
import nl.sense_os.commonsense.client.services.TagsServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.views.StateCreator;
import nl.sense_os.commonsense.client.views.StateGrid;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StateController extends Controller {

    private static final String TAG = "StateController";
    private StateGrid gridView;
    private StateCreator creatorView;

    public StateController() {
        // events to update the list of groups
        registerEventTypes(StateEvents.ListNotUpdated, StateEvents.ListRequested,
                StateEvents.ListUpdated, StateEvents.Working, StateEvents.ShowGrid);
        registerEventTypes(StateEvents.ShowCreator, StateEvents.CreateRequested,
                StateEvents.CreateComplete, StateEvents.CreateFailed, StateEvents.CreateCancelled);
        registerEventTypes(StateEvents.RemoveRequested, StateEvents.RemoveComplete,
                StateEvents.RemoveFailed);
        registerEventTypes(MainEvents.ShowVisualization);
        registerEventTypes(LoginEvents.LoggedIn, LoginEvents.LoggedOut);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(StateEvents.ListRequested)) {
            Log.d(TAG, "ListRequested");
            onListRequest(event);
        } else if (type.equals(StateEvents.ListAvailableRequested)) {
            Log.d(TAG, "ListAvailableRequested");
            onListAvailableRequest(event);
        } else if (type.equals(StateEvents.CreateRequested)) {
            Log.d(TAG, "CreateRequested");
            onCreateRequest(event);
        } else if (type.equals(StateEvents.RemoveRequested)) {
            Log.d(TAG, "RemoveRequested");
            onRemoveRequest(event);
        } else if (type.equals(StateEvents.ShowCreator) || type.equals(StateEvents.CreateComplete)
                || type.equals(StateEvents.CreateFailed)
                || type.equals(StateEvents.CreateCancelled)) {
            forwardToView(this.creatorView, event);
        } else {
            forwardToView(this.gridView, event);
        }
    }

    private void onRemoveRequest(AppEvent event) {
        TagsServiceAsync service = Registry.<TagsServiceAsync> get(Constants.REG_TAGS_SVC);
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        String sensorId = event.<String> getData("sensorId");
        String serviceId = event.<String> getData("serviceId");
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(StateEvents.RemoveFailed, caught);
            }

            @Override
            public void onSuccess(Void result) {
                Dispatcher.forwardEvent(StateEvents.RemoveComplete);
            }
        };
        service.disconnectService(sessionId, sensorId, serviceId, callback);
    }

    private void onCreateRequest(AppEvent event) {
        Dispatcher.forwardEvent(StateEvents.CreateFailed);
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.gridView = new StateGrid(this);
        this.creatorView = new StateCreator(this);
    }

    private void onListRequest(AppEvent event) {
        TagsServiceAsync service = Registry.<TagsServiceAsync> get(Constants.REG_TAGS_SVC);
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(StateEvents.ListNotUpdated, caught);
            }

            @Override
            public void onSuccess(List<TreeModel> result) {
                Registry.register(Constants.REG_SERVICES, result);
                Dispatcher.forwardEvent(StateEvents.ListUpdated, result);
            }
        };
        service.getMyServices(sessionId, callback);
        Dispatcher.forwardEvent(StateEvents.Working);
    }

    private void onListAvailableRequest(AppEvent event) {
        TagsServiceAsync service = Registry.<TagsServiceAsync> get(Constants.REG_TAGS_SVC);
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(StateEvents.ListAvailableNotUpdated, caught);
            }

            @Override
            public void onSuccess(List<TreeModel> result) {
                Registry.register(Constants.REG_SERVICES, result);
                Dispatcher.forwardEvent(StateEvents.ListAvailableUpdated, result);
            }
        };
        service.getAvailableServices(sessionId, callback);
    }
}
