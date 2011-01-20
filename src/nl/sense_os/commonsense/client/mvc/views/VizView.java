package nl.sense_os.commonsense.client.mvc.views;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;

import java.util.List;

import nl.sense_os.commonsense.client.components.Visualization;
import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.MainEvents;
import nl.sense_os.commonsense.client.mvc.events.VizEvents;
import nl.sense_os.commonsense.client.utility.Log;

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
            onInit(event);
        } else if (eventType.equals(LoginEvents.LoggedIn)) {
            onLoggedIn(event);
        } else if (eventType.equals(LoginEvents.LoggedOut)) {
            onLoggedOut(event);
        } else if (eventType.equals(VizEvents.TagsNotUpdated)) {
            onTagsNotUpdated(event);
        } else if (eventType.equals(VizEvents.TagsUpdated)) {
            onTagsUpdated(event);
        } else if (eventType.equals(VizEvents.GroupsNotUpdated)) {
            onGroupsNotUpdated(event);
        } else if (eventType.equals(VizEvents.GroupsUpdated)) {
            onGroupsUpdated(event);
        } else {
            Log.e(TAG, "Unexpected event type: " + eventType);
        }
    }

    private void onTagsUpdated(AppEvent event) {
        Log.d(TAG, "onTagsUpdated");
        
        List<TreeModel> tags = event.getData();
        this.vizPanel.store.removeAll();
        this.vizPanel.store.add(tags, true);
    }

    private void onTagsNotUpdated(AppEvent event) {
        Log.e(TAG, "onTagsNotUpdated");
        
        this.vizPanel.store.removeAll();
    }

    private void onGroupsUpdated(AppEvent event) {
        Log.d(TAG, "onGroupsUpdated");
        
        Log.d(TAG, "groups response: " + event.<String> getData());
    }

    private void onGroupsNotUpdated(AppEvent event) {
        Log.e(TAG, "onGroupsNotUpdated");
        
    }

    private void onLoggedOut(AppEvent event) {
        Log.d(TAG, "onLoggedOut");

        this.vizPanel.setLoggedIn(false);
        this.vizPanel.store.removeAll();
    }

    private void onLoggedIn(AppEvent event) {
        Log.d(TAG, "onLoggedIn");

        this.vizPanel.setLoggedIn(true);
        Dispatcher.forwardEvent(VizEvents.TagsRequested);
        Dispatcher.forwardEvent(VizEvents.GroupsRequested);
    }

    private void onInit(AppEvent event) {
        Log.d(TAG, "onInit");

        Dispatcher.forwardEvent(VizEvents.VizReady, this.vizPanel);
    }
}
