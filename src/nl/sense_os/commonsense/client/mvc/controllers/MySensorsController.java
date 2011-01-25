package nl.sense_os.commonsense.client.mvc.controllers;

import java.util.List;

import nl.sense_os.commonsense.client.mvc.events.GroupEvents;
import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.MySensorsEvents;
import nl.sense_os.commonsense.client.mvc.views.MySensorsView;
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

public class MySensorsController extends Controller {

    private static final String TAG = "MySensorsController";
    private MySensorsView treeView;

    public MySensorsController() {
        registerEventTypes(MySensorsEvents.ShowMySensors, MySensorsEvents.MySensorsNotUpdated,
                MySensorsEvents.MySensorsRequested, MySensorsEvents.MySensorsUpdated,
                MySensorsEvents.MySensorsBusy);
        registerEventTypes(LoginEvents.LoggedOut);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(MySensorsEvents.MySensorsRequested)) {
            Log.d(TAG, "MySensorsRequested");
            onMySensorsRequest(event);
        } else {
            forwardToView(this.treeView, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.treeView = new MySensorsView(this);
    }

    private void onMySensorsRequest(AppEvent event) {
        TagsServiceAsync service = Registry.<TagsServiceAsync> get(Constants.REG_TAGS_SVC);
        String sessionId = Registry.get(Constants.REG_SESSION_ID);
        AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(MySensorsEvents.MySensorsNotUpdated, caught);
            }

            @Override
            public void onSuccess(List<TreeModel> result) {
                Registry.register(Constants.REG_MY_SENSORS, result);
                Dispatcher.forwardEvent(MySensorsEvents.MySensorsUpdated, result);
            }
        };
        service.getMySensors(sessionId, callback);
        Dispatcher.forwardEvent(MySensorsEvents.MySensorsBusy);
    }
}
