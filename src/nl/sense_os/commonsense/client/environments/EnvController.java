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
import com.extjs.gxt.ui.client.mvc.View;

public class EnvController extends Controller {

    private static final String TAG = "EnvController";
    private View treeGrid;
    private View creator;

    public EnvController() {
        // events to update the list of groups
        registerEventTypes(MainEvents.Init);
        registerEventTypes(EnvEvents.ListNotUpdated, EnvEvents.ListRequested,
                EnvEvents.ListUpdated, EnvEvents.Working, EnvEvents.Done, EnvEvents.ShowGrid);
        registerEventTypes(EnvEvents.ShowCreator);
        registerEventTypes(VizEvents.Show);
        registerEventTypes(LoginEvents.LoggedOut);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(EnvEvents.ListRequested)) {
            Log.d(TAG, "ListRequested");
            requestList(event);

        } else if (type.equals(EnvEvents.ShowCreator)) {
            forwardToView(this.creator, event);

        } else {
            forwardToView(this.treeGrid, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.treeGrid = new EnvGrid(this);
        this.creator = new EnvCreator(this);
    }

    private void requestList(AppEvent event) {
        Log.w(TAG, "Environments list request is not implemented!");
        forwardToView(this.treeGrid,
                new AppEvent(EnvEvents.ListUpdated, new ArrayList<TreeModel>()));
    }
}