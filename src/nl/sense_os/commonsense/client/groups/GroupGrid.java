package nl.sense_os.commonsense.client.groups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.SensorComparator;
import nl.sense_os.commonsense.client.utility.SensorIconProvider;
import nl.sense_os.commonsense.client.utility.SensorKeyProvider;
import nl.sense_os.commonsense.client.visualization.VizEvents;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupGrid extends View {

    protected static final String TAG = "GroupGrid";
    private Button createButton;
    private TreeGrid<TreeModel> grid;
    private Button inviteButton;
    private Button joinButton;
    private Button leaveButton;
    private ContentPanel panel;
    private boolean isCollapsed = false;
    private TreeStore<TreeModel> store;
    private BaseTreeLoader<TreeModel> loader;

    public GroupGrid(Controller controller) {
        super(controller);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(MainEvents.Init)) {
            // do nothing, initialization is done in initialize()

        } else if (type.equals(GroupEvents.ShowGrid)) {
            Log.d(TAG, "Show");
            onShow(event);

        } else if (type.equals(GroupEvents.ListUpdated)) {
            // Log.d(TAG, "ListUpdated");
            setBusy(false);

        } else if (type.equals(VizEvents.Show)) {
            Log.d(TAG, "ShowVisualization");
            refreshLoader();

        } else if (type.equals(LoginEvents.LoggedOut)) {
            // Log.d(TAG, "LoggedOut");
            onLoggedOut(event);

        } else if (type.equals(GroupEvents.Working)) {
            // Log.d(TAG, "Working");
            setBusy(true);

        } else if (type.equals(GroupEvents.CreateComplete)) {
            Log.d(TAG, "CreateComplete");
            refreshLoader();

        } else if (type.equals(GroupEvents.LeaveComplete)) {
            Log.d(TAG, "LeaveComplete");
            onLeaveComplete(event);

        } else if (type.equals(GroupEvents.InviteComplete)) {
            Log.d(TAG, "InviteComplete");
            refreshLoader();

        } else if (type.equals(GroupEvents.LeaveFailed)) {
            Log.d(TAG, "LeaveFailed");
            onLeaveFailed(event);

        } else {
            Log.w(TAG, "Unexpected event type: " + type);
        }
    }

    private void initGrid() {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        DataProxy proxy = new DataProxy() {

            @Override
            public void load(DataReader reader, Object loadConfig, AsyncCallback callback) {
                // only load when the panel is not collapsed
                if (false == isCollapsed) {
                    if (null == loadConfig) {
                        fireEvent(new AppEvent(GroupEvents.ListRequested, callback));
                    } else if (loadConfig instanceof TreeModel) {
                        List<ModelData> childrenModels = ((TreeModel) loadConfig).getChildren();
                        callback.onSuccess(childrenModels);
                    } else {
                        callback.onSuccess(new ArrayList<TreeModel>());
                    }
                }
            }
        };
        this.loader = new BaseTreeLoader<TreeModel>(proxy);
        this.store = new TreeStore<TreeModel>(this.loader);
        this.store.setKeyProvider(new SensorKeyProvider());
        this.store.setStoreSorter(new StoreSorter<TreeModel>(new SensorComparator()));

        ColumnConfig email = new ColumnConfig("email", "Email", 100);
        ColumnConfig name = new ColumnConfig("text", "Name", 100);

        name.setRenderer(new TreeGridCellRenderer<TreeModel>());
        ColumnModel cm = new ColumnModel(Arrays.asList(name, email));

        this.grid = new TreeGrid<TreeModel>(this.store, cm);
        this.grid.setId("groupGrid");
        this.grid.setStateful(true);
        this.grid.setAutoExpandColumn("text");
        this.grid.setIconProvider(new SensorIconProvider());

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

        // toolbar with filter field
        ToolBar filterBar = new ToolBar();
        filterBar.add(new LabelToolItem("Filter: "));
        StoreFilterField<TreeModel> filter = new StoreFilterField<TreeModel>() {

            @Override
            protected boolean doSelect(Store<TreeModel> store, TreeModel parent, TreeModel record,
                    String property, String filter) {
                // only match leaf nodes
                if (record.getChildCount() > 0) {
                    return false;
                }
                String name = record.get("text");
                name = name.toLowerCase();
                if (name.startsWith(filter.toLowerCase())) {
                    return true;
                }
                return false;
            }

        };
        filter.bind(store);
        filterBar.add(filter);

        ContentPanel content = new ContentPanel(new FitLayout());
        content.setBodyBorder(false);
        content.setHeaderVisible(false);
        content.setTopComponent(filterBar);
        content.add(this.grid);

        this.panel.add(content);
    }

    private void initHeaderTool() {
        ToolButton refresh = new ToolButton("x-tool-refresh");
        refresh.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                refreshLoader();
            }
        });

        // add to panel
        this.panel.getHeader().addTool(refresh);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.panel = new ContentPanel(new FitLayout());
        this.panel.setHeading("Manage group memberships");

        // track whether the panel is expanded
        Listener<ComponentEvent> collapseListener = new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                EventType type = be.getType();
                if (type.equals(Events.Expand)) {
                    isCollapsed = false;
                    refreshLoader();
                } else if (type.equals(Events.Collapse)) {
                    isCollapsed = true;
                }
            }
        };
        panel.addListener(Events.Expand, collapseListener);
        panel.addListener(Events.Collapse, collapseListener);

        initGrid();
        initHeaderTool();
        initToolBar();
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

    protected void leaveGroup() {
        TreeModel group = grid.getSelectionModel().getSelectedItem();
        while (null != group.getParent()) {
            group = group.getParent();
        }
        String groupId = group.get("id");
        fireEvent(new AppEvent(GroupEvents.LeaveRequested, groupId));
    }

    private void onLeaveClick() {
        MessageBox.confirm(null, "Are you sure you want to leave this group?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        Button clicked = be.getButtonClicked();
                        if ("yes".equalsIgnoreCase(clicked.getText())) {
                            leaveGroup();
                        }
                    }
                });
    }

    private void onLeaveComplete(AppEvent event) {
        refreshLoader();
    }

    private void onLeaveFailed(AppEvent event) {
        MessageBox.confirm(null, "Failed to leave group, retry?", new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                Button clicked = be.getButtonClicked();
                if ("yes".equalsIgnoreCase(clicked.getText())) {
                    leaveGroup();
                }
            }
        });
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

    private void refreshLoader() {
        if (this.store.getChildCount() == 0) {
            loader.load();
        }
    }

    private void setBusy(boolean busy) {
        String icon = busy ? Constants.ICON_LOADING : "";
        this.panel.getHeader().setIcon(IconHelper.create(icon));
    }
}
