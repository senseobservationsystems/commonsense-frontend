package nl.sense_os.commonsense.client.groups.list;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.models.GroupModel;
import nl.sense_os.commonsense.client.common.models.UserModel;
import nl.sense_os.commonsense.client.common.utility.SenseIconProvider;
import nl.sense_os.commonsense.client.common.utility.SenseKeyProvider;
import nl.sense_os.commonsense.client.common.utility.SensorComparator;
import nl.sense_os.commonsense.client.groups.create.GroupCreateEvents;
import nl.sense_os.commonsense.client.groups.invite.InviteEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.viz.tabs.VizEvents;

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

    private static final Logger LOG = Logger.getLogger(GroupGrid.class.getName());
    private Button createButton;
    private TreeGrid<UserModel> grid;
    private Button inviteButton;
    private Button joinButton;
    private Button leaveButton;
    private ContentPanel panel;
    private TreeStore<UserModel> store;
    private TreeLoader<UserModel> loader;
    private ToolBar filterBar;
    private StoreFilterField<UserModel> filter;
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
            // LOG.fine( "ShowGrid");
            final LayoutContainer parent = event.getData("parent");
            showPanel(parent);

        } else if (type.equals(GroupEvents.ListUpdated)) {
            // LOG.fine( "TreeUpdated");
            setBusy(false);

        } else if (type.equals(VizEvents.Show)) {
            // LOG.fine( "Show Visualization");
            refreshLoader(false);

        } else if (type.equals(GroupEvents.Working)) {
            // LOG.fine( "Working");
            setBusy(true);

        } else if (type.equals(GroupCreateEvents.CreateComplete)
                || type.equals(GroupEvents.LeaveComplete)
                || type.equals(InviteEvents.InviteComplete)) {
            // LOG.fine( "InviteComplete");
            onListDirty();

        } else if (type.equals(GroupEvents.LeaveFailed)) {
            LOG.warning("LeaveFailed");
            onLeaveFailed(event);

        } else {
            LOG.severe("Unexpected event type: " + type);
        }
    }

    private void initFilter() {
        filterBar = new ToolBar();
        filterBar.add(new LabelToolItem("Filter: "));
        filter = new StoreFilterField<UserModel>() {

            @Override
            protected boolean doSelect(Store<UserModel> store, UserModel parent, UserModel record,
                    String property, String filter) {
                // only match leaf nodes
                if (record.getChildCount() > 0) {
                    return false;
                }
                String name = record.getName() + " " + record.getSurname() + " "
                        + record.getUsername();
                name = name.toLowerCase();
                if (name.contains(filter.toLowerCase())) {
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
        DataProxy<List<UserModel>> proxy = new DataProxy<List<UserModel>>() {

            @Override
            public void load(DataReader<List<UserModel>> reader, Object loadConfig,
                    AsyncCallback<List<UserModel>> callback) {

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
        loader = new BaseTreeLoader<UserModel>(proxy) {

            @Override
            public boolean hasChildren(UserModel parent) {
                return parent instanceof GroupModel;
            };
        };

        // tree store
        store = new TreeStore<UserModel>(loader);
        store.setKeyProvider(new SenseKeyProvider<UserModel>());
        store.setStoreSorter(new StoreSorter<UserModel>(new SensorComparator<UserModel>()));

        ColumnConfig id = new ColumnConfig(UserModel.ID, "ID", 50);
        id.setHidden(true);
        ColumnConfig name = new ColumnConfig(UserModel.NAME, "Name", 125);
        name.setRenderer(new TreeGridCellRenderer<TreeModel>());
        ColumnConfig surname = new ColumnConfig(UserModel.SURNAME, "Surname", 125);
        ColumnConfig username = new ColumnConfig(UserModel.USERNAME, "Username", 100);
        ColumnModel cm = new ColumnModel(Arrays.asList(id, name, surname, username));

        grid = new TreeGrid<UserModel>(store, cm);
        grid.setAutoLoad(true);
        grid.setLoadMask(true);
        grid.setId("groupGrid");
        grid.setStateful(true);
        grid.setAutoExpandColumn(UserModel.USERNAME);
        grid.setIconProvider(new SenseIconProvider<UserModel>());
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
        panel.getHeader().addTool(refresh);
    }

    @Override
    protected void initialize() {
        super.initialize();

        panel = new ContentPanel(new FitLayout());
        panel.setHeading("Manage group memberships");
        panel.setAnimCollapse(false);

        // track whether the panel is expanded
        panel.addListener(Events.Expand, new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                refreshLoader(false);
            }
        });

        initGrid();
        initFilter();
        initToolBar();

        initHeaderTool();
        panel.setTopComponent(toolBar);
        ContentPanel content = new ContentPanel(new FitLayout());
        content.setBodyBorder(false);
        content.setHeaderVisible(false);
        content.setTopComponent(filterBar);
        content.add(grid);
        panel.add(content);
    }

    private void initToolBar() {

        final SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Button source = ce.getButton();
                if (source.equals(createButton)) {
                    Dispatcher.forwardEvent(GroupCreateEvents.ShowCreator);
                } else if (source.equals(leaveButton)) {
                    onLeaveClick();
                } else if (source.equals(joinButton)) {
                    LOG.fine("Join group");
                } else if (source.equals(inviteButton)) {
                    onInviteClick();
                }
            }
        };

        createButton = new Button("Create", l);

        joinButton = new Button("Join", l);
        joinButton.disable();

        leaveButton = new Button("Leave", l);
        leaveButton.disable();

        inviteButton = new Button("Invite", l);
        inviteButton.disable();

        // handle selections
        TreeGridSelectionModel<UserModel> selectionModel = new TreeGridSelectionModel<UserModel>();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<UserModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<UserModel> se) {
                UserModel selection = se.getSelectedItem();
                if (null != selection) {
                    leaveButton.enable();
                    inviteButton.enable();
                } else {
                    leaveButton.disable();
                    inviteButton.disable();
                }
            }
        });
        grid.setSelectionModel(selectionModel);

        // create tool bar
        toolBar = new ToolBar();
        toolBar.add(joinButton);
        toolBar.add(createButton);
        toolBar.add(inviteButton);
        toolBar.add(leaveButton);
    }

    private void leaveGroup() {
        UserModel group = grid.getSelectionModel().getSelectedItem();
        while (!(group instanceof GroupModel)) {
            group = (UserModel) group.getParent();
        }
        int groupId = group.getId();
        fireEvent(new AppEvent(GroupEvents.LeaveRequested, groupId));
    }

    private void onInviteClick() {
        UserModel selected = grid.getSelectionModel().getSelectedItem();
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
        if (force || store.getChildCount() == 0) {
            loader.load();
        }
    }

    private void setBusy(boolean busy) {
        if (busy) {
            panel.getHeader().setIcon(SenseIconProvider.ICON_LOADING);
        } else {
            panel.getHeader().setIcon(IconHelper.create(""));
        }
    }

    private void showPanel(LayoutContainer parent) {
        if (null != parent) {
            parent.add(panel);
            parent.layout();
        } else {
            LOG.severe("Failed to show groups panel: parent=null");
        }
    }
}
