package nl.sense_os.commonsense.client.mvc.controllers;

import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.StateEvents;
import nl.sense_os.commonsense.client.mvc.views.StateGrid;
import nl.sense_os.commonsense.client.services.TagsServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public class StateController extends Controller {

    private static final String TAG = "StateController";
    private StateGrid gridView;

    public StateController() {
        // events to update the list of groups
        registerEventTypes(StateEvents.ListNotUpdated, StateEvents.ListRequested,
                StateEvents.ListUpdated, StateEvents.Working, StateEvents.ShowGrid);

        registerEventTypes(LoginEvents.LoggedOut);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(StateEvents.ListRequested)) {
            Log.d(TAG, "ListRequested");
            onListRequest(event);
        } else {
            forwardToView(this.gridView, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.gridView = new StateGrid(this);
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
        service.getServices(sessionId, callback);
        Dispatcher.forwardEvent(StateEvents.Working);
    }
}
