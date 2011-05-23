package nl.sense_os.commonsense.client.groups.list;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.groups.create.GroupCreateEvents;
import nl.sense_os.commonsense.client.groups.invite.InviteEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.utility.SenseIconProvider;
import nl.sense_os.commonsense.client.utility.SenseKeyProvider;
import nl.sense_os.commonsense.client.utility.SensorComparator;
import nl.sense_os.commonsense.client.viz.tabs.VizEvents;
import nl.sense_os.commonsense.shared.models.GroupModel;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.TreeLoader;
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
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
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
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupGrid extends View {

    protected static final Logger logger = Logger.getLogger("GroupGrid");
    private Button createButton;
    private TreeGrid<TreeModel> grid;
    private Button inviteButton;
    private Button joinButton;
    private Button leaveButton;
    private ContentPanel panel;
    private TreeStore<TreeModel> store;
    private TreeLoader<TreeModel> loader;
    private ToolBar filterBar;
    private StoreFilterField<TreeModel> filter;
    private ToolBar toolBar;

    public GroupGrid(Controller controller) {
        super(controller);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(MainEvents.Init)) {
            // do nothing, initialization is done in initialize()

        } else if (type.equals(GroupEvents.ShowGrid)) {
            // logger.fine( "ShowGrid");
            final LayoutContainer parent = event.getData("parent");
            showPanel(parent);

        } else if (type.equals(GroupEvents.ListUpdated)) {
            // logger.fine( "TreeUpdated");
            setBusy(false);

        } else if (type.equals(VizEvents.Show)) {
            // logger.fine( "Show Visualization");
            refreshLoader(false);

        } else if (type.equals(GroupEvents.Working)) {
            // logger.fine( "Working");
            setBusy(true);

        } else if (type.equals(GroupCreateEvents.CreateComplete)
                || type.equals(GroupEvents.LeaveComplete)
                || type.equals(InviteEvents.InviteComplete)) {
            // logger.fine( "InviteComplete");
            onListDirty();

        } else if (type.equals(GroupEvents.LeaveFailed)) {
            logger.warning("LeaveFailed");
            onLeaveFailed(event);

        } else {
            logger.severe("Unexpected event type: " + type);
        }
    }

    private void initFilter() {
        filterBar = new ToolBar();
        filterBar.add(new LabelToolItem("Filter: "));
        filter = new StoreFilterField<TreeModel>() {

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

        // TODO fix filtering
        filter.setEnabled(false);
    }

    private void initGrid() {

        // proxy
        DataProxy<List<TreeModel>> proxy = new DataProxy<List<TreeModel>>() {

            @Override
            public void load(DataReader<List<TreeModel>> reader, Object loadConfig,
                    AsyncCallback<List<TreeModel>> callback) {

                if (panel.isExpanded()) {
                    AppEvent loadRequest = new AppEvent(GroupEvents.LoadRequest);
                    loadRequest.setData("loadConfig", loadConfig);
                    loadRequest.setData("callback", callback);
                    fireEvent(loadRequest);
                } else {
                    callback.onFailure(null);
                }
            }
        };

        // tree loader
        this.loader = new BaseTreeLoader<TreeModel>(proxy) {

            @Override
            public boolean hasChildren(TreeModel parent) {
                return (parent instanceof GroupModel);
            };
        };

        // tree store
        this.store = new TreeStore<TreeModel>(this.loader);
        this.store.setKeyProvider(new SenseKeyProvider<TreeModel>());
        this.store.setStoreSorter(new StoreSorter<TreeModel>(new SensorComparator()));

        ColumnConfig email = new ColumnConfig("email", "Email", 100);
        ColumnConfig name = new ColumnConfig("text", "Name", 100);
        name.setRenderer(new TreeGridCellRenderer<TreeModel>());
        ColumnModel cm = new ColumnModel(Arrays.asList(name, email));

        this.grid = new TreeGrid<TreeModel>(this.store, cm);
        this.grid.setAutoLoad(true);
        this.grid.setLoadMask(true);
        this.grid.setId("groupGrid");
        this.grid.setStateful(true);
        this.grid.setAutoExpandColumn("text");
        this.grid.setIconProvider(new SenseIconProvider<TreeModel>());
    }

    private void initHeaderTool() {
        ToolButton refresh = new ToolButton("x-tool-refresh");
        refresh.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                refreshLoader(true);
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
        this.panel.addListener(Events.Expand, new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                refreshLoader(false);
            }
        });

        initGrid();
        initFilter();
        initToolBar();

        initHeaderTool();
        this.panel.setTopComponent(toolBar);
        ContentPanel content = new ContentPanel(new FitLayout());
        content.setBodyBorder(false);
        content.setHeaderVisible(false);
        content.setTopComponent(filterBar);
        content.add(this.grid);
        this.panel.add(content);
    }

    private void initToolBar() {

        final SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Button source = ce.getButton();
                if (source.equals(createButton)) {
                    fireEvent(GroupCreateEvents.ShowCreator);
                } else if (source.equals(leaveButton)) {
                    onLeaveClick();
                } else if (source.equals(joinButton)) {
                    logger.fine("Join group");
                } else if (source.equals(inviteButton)) {
                    onInviteClick();
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

        // handle selections
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

        // create tool bar
        this.toolBar = new ToolBar();
        toolBar.add(this.joinButton);
        toolBar.add(this.createButton);
        toolBar.add(this.inviteButton);
        toolBar.add(this.leaveButton);
    }

    private void leaveGroup() {
        TreeModel group = grid.getSelectionModel().getSelectedItem();
        while (null != group.getParent()) {
            group = group.getParent();
        }
        String groupId = group.get("id");
        fireEvent(new AppEvent(GroupEvents.LeaveRequested, groupId));
    }

    private void onInviteClick() {
        TreeModel selected = grid.getSelectionModel().getSelectedItem();
        GroupModel group = null;
        if (selected instanceof GroupModel) {
            group = (GroupModel) selected;
        } else if (selected.getParent() instanceof GroupModel) {
            group = (GroupModel) selected.getParent();
        } else {
            MessageBox.alert(null, "Cannot invite user to group: no group selected.", null);
            return;
        }

        AppEvent invite = new AppEvent(InviteEvents.ShowInviter);
        invite.setData("group", group);
        Dispatcher.forwardEvent(invite);
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

    private void onListDirty() {
        new Timer() {

            @Override
            public void run() {
                refreshLoader(true);
            }
        }.schedule(100);
    }

    private void refreshLoader(boolean force) {
        if (force || this.store.getChildCount() == 0) {
            loader.load();
        }
    }

    private void setBusy(boolean busy) {
        if (busy) {
            this.panel.getHeader().setIcon(SenseIconProvider.ICON_LOADING);
        } else {
            this.panel.getHeader().setIcon(IconHelper.create(""));
        }
    }

    private void showPanel(LayoutContainer parent) {
        if (null != parent) {
            parent.add(this.panel);
            parent.layout();
        } else {
            logger.severe("Failed to show groups panel: parent=null");
        }
    }
}
