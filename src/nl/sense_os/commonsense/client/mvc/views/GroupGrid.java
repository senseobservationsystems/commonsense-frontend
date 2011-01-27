package nl.sense_os.commonsense.client.mvc.views;

import nl.sense_os.commonsense.client.mvc.events.GroupEvents;
import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.utility.Log;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;

import java.util.Arrays;
import java.util.List;

public class GroupGrid extends View {

    protected static final String TAG = "GroupGrid";
    private ContentPanel panel;
    private TreeGrid<TreeModel> grid;
    private TreeStore<TreeModel> store;
    private Button createButton;
    private Button inviteButton;
    private Button joinButton;
    private Button leaveButton;

    public GroupGrid(Controller controller) {
        super(controller);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(GroupEvents.ShowGrid)) {
            onShow(event);
        } else if (type.equals(GroupEvents.ListNotUpdated)) {
            Log.w(TAG, "GroupListNotUpdated");
            onGroupsNotUpdated(event);
        } else if (type.equals(GroupEvents.ListUpdated)) {
            Log.d(TAG, "GroupListUpdated");
            onGroupsUpdated(event);
        } else if (type.equals(LoginEvents.LoggedOut)) {
            Log.d(TAG, "LoggedOut");
            onLoggedOut(event);
        } else if (type.equals(GroupEvents.Working)) {
            Log.d(TAG, "GroupsBusy");
            setBusyIcon(true);
        } else if (type.equals(GroupEvents.LeaveComplete)) {
            Log.d(TAG, "LeaveGroupComplete");
            onLeaveComplete(event);
        } else if (type.equals(GroupEvents.LeaveFailed)) {
            Log.d(TAG, "LeaveGroupFailed");
            onLeaveFailed(event);
        } else {
            Log.w(TAG, "Unexpected event type: " + type);
        }
    }

    private void initGrid() {
        this.store = new TreeStore<TreeModel>();

        ColumnConfig email = new ColumnConfig("email", "Email", 100);
        ColumnConfig name = new ColumnConfig("name", "Name", 100);

        name.setRenderer(new TreeGridCellRenderer<TreeModel>());
        ColumnModel cm = new ColumnModel(Arrays.asList(name, email));

        this.grid = new TreeGrid<TreeModel>(this.store, cm);
        this.grid.setId("groupGrid");
        this.grid.setStateful(true);

        TreeGridSelectionModel<TreeModel> selectionModel = new TreeGridSelectionModel<TreeModel>();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<TreeModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<TreeModel> se) {
                TreeModel selection = se.getSelectedItem();
                if (null != selection) {
                    leaveButton.enable();
                    inviteButton.enable();
                } else {
                    leaveButton.disable();
                    inviteButton.disable();
                }
            }
        });
        this.grid.setSelectionModel(selectionModel);

        // add grid to panel
        this.panel.add(this.grid);
    }

    private void initHeaderTool() {
        ToolButton refresh = new ToolButton("x-tool-refresh");
        refresh.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                Dispatcher.get().dispatch(GroupEvents.ListRequested);
            }
        });

        // add to panel
        this.panel.getHeader().addTool(refresh);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.panel = new ContentPanel(new FitLayout());
        this.panel.setHeading("Groups");
        this.panel.setAnimCollapse(false);

        initHeaderTool();
        initToolBar();
        initGrid();
    }

    private void initToolBar() {

        final SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Button source = ce.getButton();
                if (source.equals(createButton)) {
                    fireEvent(GroupEvents.ShowCreator);
                } else if (source.equals(leaveButton)) {
                    onLeaveClick();
                } else if (source.equals(joinButton)) {
                    Log.d(TAG, "Join group");
                } else if (source.equals(inviteButton)) {
                    AppEvent invite = new AppEvent(GroupEvents.ShowInviter);
                    invite.setData(grid.getSelectionModel().getSelectedItem());
                    fireEvent(invite);
                }
            }
        };

        this.createButton = new Button("Create", l);

        this.joinButton = new Button("Join", l);
        this.joinButton.disable();

        this.leaveButton = new Button("Leave", l);
        this.leaveButton.disable();

        this.inviteButton = new Button("Invite", l);
        this.inviteButton.disable();

        // create tool bar
        final ToolBar toolBar = new ToolBar();
        toolBar.add(this.joinButton);
        toolBar.add(this.createButton);
        toolBar.add(this.inviteButton);
        toolBar.add(this.leaveButton);

        // add to panel
        this.panel.setTopComponent(toolBar);
    }

    private void onGroupsNotUpdated(AppEvent event) {
        // Throwable caught = event.<Throwable> getData();
        setBusyIcon(false);
        this.store.removeAll();
    }

    private void onGroupsUpdated(AppEvent event) {
        List<TreeModel> groups = event.<List<TreeModel>> getData();
        setBusyIcon(false);
        this.store.removeAll();
        this.store.add(groups, true);
    }

    private void onLeaveClick() {
        MessageBox.confirm(null, "Are you sure you want to leave this group?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        Button clicked = be.getButtonClicked();
                        if ("yes".equalsIgnoreCase(clicked.getText())) {
                            TreeModel select = grid.getSelectionModel().getSelectedItem();
                            String groupId = select.get("id");
                            AppEvent leaveEvent = new AppEvent(GroupEvents.LeaveRequested, groupId);
                            fireEvent(leaveEvent);
                        }
                    }
                });
    }

    private void onLeaveComplete(AppEvent event) {
        fireEvent(new AppEvent(GroupEvents.ListRequested));
    }

    private void onLeaveFailed(AppEvent event) {
        MessageBox.alert("CommonSense", "Failed to leave group, please retry.", null);
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
            Log.e(TAG, "Failed to show groups panel: parent=null");
        }
    }

    private void setBusyIcon(boolean busy) {
        String icon = busy ? "gxt/images/gxt/icons/loading.gif" : "";
        this.panel.getHeader().setIcon(IconHelper.create(icon));
    }
}
