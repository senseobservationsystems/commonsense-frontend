package nl.sense_os.commonsense.client.environments;

import java.util.ArrayList;

import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.VizEvents;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;

public class BuildingController extends Controller {

    private static final String TAG = "BuildingController";
    private BuildingGrid treeGrid;

    public BuildingController() {
        // events to update the list of groups
        registerEventTypes(MainEvents.Init);
        registerEventTypes(BuildingEvents.ListNotUpdated, BuildingEvents.ListRequested,
                BuildingEvents.ListUpdated, BuildingEvents.Working, BuildingEvents.ShowGrid);
        registerEventTypes(VizEvents.Show);
        registerEventTypes(LoginEvents.LoggedOut);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(BuildingEvents.ListRequested)) {
            Log.d(TAG, "ListRequested");
            requestList(event);

        } else {
            forwardToView(this.treeGrid, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.treeGrid = new BuildingGrid(this);
    }

    private void requestList(AppEvent event) {
        Log.w(TAG, "Environments list request is not implemented!");
        forwardToView(this.treeGrid, new AppEvent(BuildingEvents.ListUpdated,
                new ArrayList<TreeModel>()));
    }
}
