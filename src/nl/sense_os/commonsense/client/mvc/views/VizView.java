package nl.sense_os.commonsense.client.mvc.views;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.user.client.rpc.StatusCodeException;

import java.util.List;

import nl.sense_os.commonsense.client.components.Visualization;
import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.MainEvents;
import nl.sense_os.commonsense.client.mvc.events.VizEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.sensorvalues.TaggedDataModel;

public class VizView extends View {

    private static final String TAG = "VizView";
    private Visualization vizPanel = new Visualization();

    public VizView(Controller controller) {
        super(controller);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType eventType = event.getType();

        if (eventType.equals(MainEvents.Init)) {
            Log.d(TAG, "onInit");
            onInit(event);
        } else if (eventType.equals(LoginEvents.LoggedIn)) {
            Log.d(TAG, "onLoggedIn");
            onLoggedIn(event);
        } else if (eventType.equals(LoginEvents.LoggedOut)) {
            Log.d(TAG, "onLoggedOut");
            onLoggedOut(event);
        } else if (eventType.equals(VizEvents.TagsNotUpdated)) {
            Log.w(TAG, "onTagsNotUpdated");
            onTagsNotUpdated(event);
        } else if (eventType.equals(VizEvents.TagsUpdated)) {
            Log.d(TAG, "onTagsUpdated");
            onTagsUpdated(event);
        } else if (eventType.equals(VizEvents.DataNotReceived)) {
            Log.w(TAG, "onDataNotReceived");
            onDataNotReceived(event);
        } else if (eventType.equals(VizEvents.DataReceived)) {
            Log.d(TAG, "onDataReceived");
            onDataReceived(event);
        } else if (eventType.equals(VizEvents.GroupsNotUpdated)) {
            Log.w(TAG, "onGroupsNotUpdated");
            onGroupsNotUpdated(event);
        } else if (eventType.equals(VizEvents.GroupsUpdated)) {
            Log.d(TAG, "onGroupsUpdated");
            onGroupsUpdated(event);
        } else {
            Log.e(TAG, "Unexpected event type: " + eventType);
        }
    }

    private void onDataNotReceived(AppEvent event) {        
        this.vizPanel.onRequestFailed();
    }

    private void onDataReceived(AppEvent event) {        
        TaggedDataModel data = event.<TaggedDataModel> getData();
        this.vizPanel.onSensorValuesReceived(data);
    }

    private void onTagsUpdated(AppEvent event) {        
        List<TreeModel> tags = event.<List<TreeModel>> getData();
        this.vizPanel.tagStore.removeAll();
        this.vizPanel.tagStore.add(tags, true);
    }

    private void onTagsNotUpdated(AppEvent event) {
        Throwable caught = event.<Throwable> getData();
        if (caught != null) {
            caught.printStackTrace();
        }
        
        this.vizPanel.tagStore.removeAll();
    }

    private void onGroupsUpdated(AppEvent event) {        
        List<TreeModel> groups = event.<List<TreeModel>> getData();
        this.vizPanel.groupStore.removeAll();
        this.vizPanel.groupStore.add(groups, true);
    }

    private void onGroupsNotUpdated(AppEvent event) {
        Throwable caught = event.<Throwable> getData();
        if (caught != null) {
            caught.printStackTrace();
        }
        
        this.vizPanel.groupStore.removeAll();
    }

    private void onLoggedOut(AppEvent event) {
        this.vizPanel.tagStore.removeAll();
    }

    private void onLoggedIn(AppEvent event) {
        Dispatcher.forwardEvent(VizEvents.TagsRequested);
        Dispatcher.forwardEvent(VizEvents.GroupsRequested);
    }

    private void onInit(AppEvent event) {
        Dispatcher.forwardEvent(VizEvents.VizReady, this.vizPanel);
    }
}
