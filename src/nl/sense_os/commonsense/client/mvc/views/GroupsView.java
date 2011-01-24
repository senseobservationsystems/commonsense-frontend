package nl.sense_os.commonsense.client.mvc.views;

import java.util.Arrays;
import java.util.List;

import nl.sense_os.commonsense.client.mvc.events.GroupsEvents;
import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.utility.Log;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;

public class GroupsView extends View {

    protected static final String TAG = "GroupView";
    private ContentPanel panel;
    private TreeStore<TreeModel> store;
    private TreeGrid<TreeModel> grid;

    public GroupsView(Controller controller) {
        super(controller);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(GroupsEvents.ShowGroups)) {
            onShow(event);
        } else if (type.equals(GroupsEvents.GroupsNotUpdated)) {
            Log.w(TAG, "GroupsNotUpdated");
            onGroupsNotUpdated(event);
        } else if (type.equals(GroupsEvents.GroupsUpdated)) {
            Log.d(TAG, "GroupsUpdated");
            onGroupsUpdated(event);
        } else if (type.equals(LoginEvents.LoggedOut)) {
            // Log.d(TAG, "LoggedOut");
            onLoggedOut(event);
        }
    }

    private void onGroupsUpdated(AppEvent event) {
        List<TreeModel> groups = event.<List<TreeModel>> getData();
        this.store.removeAll();
        this.store.add(groups, true);
    }

    private void onGroupsNotUpdated(AppEvent event) {
        // Throwable caught = event.<Throwable> getData();
        this.store.removeAll();
    }

    private void onLoggedOut(AppEvent event) {
        this.store.removeAll();
    }

    private void onShow(AppEvent event) {
        ContentPanel parent = event.<ContentPanel> getData();
        if (null != parent) {
            parent.add(this.panel);
            parent.layout();
        } else {
            Log.e(TAG, "Failed to show groups panel. parent=null");
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.store = new TreeStore<TreeModel>();

        ColumnConfig id = new ColumnConfig("id", "Id", 25);
        id.setRenderer(new TreeGridCellRenderer<TreeModel>());
        ColumnConfig email = new ColumnConfig("email", "Email", 100);
        ColumnConfig name = new ColumnConfig("name", "Name", 100);
        ColumnModel cm = new ColumnModel(Arrays.asList(id, email, name));

        this.grid = new TreeGrid<TreeModel>(this.store, cm);

        ToolButton refresh = new ToolButton("x-tool-refresh");
        refresh.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                Dispatcher.get().dispatch(GroupsEvents.GroupsRequested);
            }
        });

        Button join = new Button("Join", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Log.d(TAG, "Join group");
            }
        });
        Button create = new Button("Create", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Log.d(TAG, "Create group");
            }
        });
        Button invite = new Button("Invite", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Log.d(TAG, "Invite group members");
            }
        });

        ToolBar toolBar = new ToolBar();
        toolBar.add(join);
        toolBar.add(create);
        toolBar.add(invite);

        panel = new ContentPanel(new FitLayout());
        panel.setHeading("Groups and sharing");
        panel.add(this.grid);
        panel.getHeader().addTool(refresh);
        panel.setAnimCollapse(false);
        panel.setTopComponent(toolBar);
    }
}
