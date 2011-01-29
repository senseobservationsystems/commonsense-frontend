package nl.sense_os.commonsense.client.mvc.controllers;

import nl.sense_os.commonsense.client.mvc.events.BuildingEvents;
import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.MainEvents;
import nl.sense_os.commonsense.client.mvc.views.BuildingGrid;
import nl.sense_os.commonsense.client.utility.Log;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;

public class BuildingController extends Controller {

    private static final String TAG = "BuildingController";
    private BuildingGrid gridView;

    public BuildingController() {
        // events to update the list of groups
        registerEventTypes(BuildingEvents.ListNotUpdated, BuildingEvents.ListRequested,
                BuildingEvents.ListUpdated, BuildingEvents.Working, BuildingEvents.ShowGrid);
        registerEventTypes(MainEvents.ShowVisualization);
        registerEventTypes(LoginEvents.LoggedIn, LoginEvents.LoggedOut);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(BuildingEvents.ListRequested)) {
            Log.d(TAG, "ListRequested");
            onListRequest(event);
        } else {
            forwardToView(this.gridView, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.gridView = new BuildingGrid(this);
    }

    private void onListRequest(AppEvent event) {
        // TODO
    }
}
