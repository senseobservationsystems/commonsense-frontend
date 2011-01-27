package nl.sense_os.commonsense.client.mvc.controllers;

import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.MySensorsEvents;
import nl.sense_os.commonsense.client.mvc.views.MySensorsShareDialog;
import nl.sense_os.commonsense.client.mvc.views.MySensorsTree;
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

public class MySensorsController extends Controller {

    private static final String TAG = "MySensorsController";
    private MySensorsTree treeView;
    private MySensorsShareDialog shareView;

    public MySensorsController() {
        registerEventTypes(MySensorsEvents.ShowTree, MySensorsEvents.ListNotUpdated,
                MySensorsEvents.ListRequested, MySensorsEvents.ListUpdated, MySensorsEvents.Working);
        registerEventTypes(MySensorsEvents.ShowShareDialog, MySensorsEvents.ShareRequested,
                MySensorsEvents.ShareComplete, MySensorsEvents.ShareCancelled,
                MySensorsEvents.ShareFailed);
        registerEventTypes(LoginEvents.LoggedOut);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(MySensorsEvents.ListRequested)) {
            // Log.d(TAG, "ListRequested");
            onListRequest(event);
        } else if (type.equals(MySensorsEvents.ShareRequested)) {
            // Log.d(TAG, "ShareRequested");
            onShareRequest(event);
        } else if (type.equals(MySensorsEvents.ShowTree)
                || type.equals(MySensorsEvents.ListUpdated)
                || type.equals(MySensorsEvents.ListNotUpdated)
                || type.equals(MySensorsEvents.Working)
                || type.equals(LoginEvents.LoggedOut)) {
            forwardToView(this.treeView, event);
        } else if (type.equals(MySensorsEvents.ShowShareDialog)
                || type.equals(MySensorsEvents.ShareComplete)
                || type.equals(MySensorsEvents.ShareFailed)
                || type.equals(MySensorsEvents.ShareCancelled)) {
            forwardToView(this.shareView, event);
        } else {
            Log.w(TAG, "Unexpected event type: " + type);
        }
    }

    private void onShareRequest(AppEvent event) {
        List<TreeModel> sensors = event.<List<TreeModel>> getData("sensors");
        TreeModel user = event.<TreeModel> getData("user");

        TagsServiceAsync service = Registry.<TagsServiceAsync> get(Constants.REG_TAGS_SVC);
        String sessionId = Registry.get(Constants.REG_SESSION_ID);
        AsyncCallback<Integer> callback = new AsyncCallback<Integer>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(MySensorsEvents.ShareFailed, caught);
            }

            @Override
            public void onSuccess(Integer result) {
                Dispatcher.forwardEvent(MySensorsEvents.ShareComplete, result);
            }
        };
        service.shareSensors(sessionId, sensors, user, callback);
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.treeView = new MySensorsTree(this);
        this.shareView = new MySensorsShareDialog(this);
    }

    private void onListRequest(AppEvent event) {
        TagsServiceAsync service = Registry.<TagsServiceAsync> get(Constants.REG_TAGS_SVC);
        String sessionId = Registry.get(Constants.REG_SESSION_ID);
        AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(MySensorsEvents.ListNotUpdated, caught);
            }

            @Override
            public void onSuccess(List<TreeModel> result) {
                Registry.register(Constants.REG_MY_SENSORS, result);
                Dispatcher.forwardEvent(MySensorsEvents.ListUpdated, result);
            }
        };
        service.getMySensors(sessionId, callback);
        Dispatcher.forwardEvent(MySensorsEvents.Working);
    }
}
