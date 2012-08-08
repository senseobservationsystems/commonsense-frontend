package nl.sense_os.commonsense.main.client.groups.list;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.ext.model.ExtGroup;
import nl.sense_os.commonsense.main.client.ext.model.ExtUser;
import nl.sense_os.commonsense.main.client.ext.util.SenseIconProvider;
import nl.sense_os.commonsense.main.client.ext.util.SenseKeyProvider;
import nl.sense_os.commonsense.main.client.ext.util.SensorComparator;
import nl.sense_os.commonsense.main.client.groups.create.GroupCreateEvents;
import nl.sense_os.commonsense.main.client.groups.invite.GroupInviteEvents;
import nl.sense_os.commonsense.main.client.groups.join.GroupJoinEvents;
import nl.sense_os.commonsense.main.client.groups.leave.GroupLeaveEvents;
import nl.sense_os.commonsense.main.client.main.MainEvents;
import nl.sense_os.commonsense.main.client.viz.tabs.VizEvents;

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
    private TreeGrid<ExtUser> grid;
    private Button addUserButton;
    private Button joinButton;
    private Button leaveButton;
    private ContentPanel panel;
    private TreeStore<ExtUser> store;
    private TreeLoader<ExtUser> loader;
    private ToolBar filterBar;
    private StoreFilterField<ExtUser> filter;
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
                || type.equals(GroupLeaveEvents.LeaveComplete)
                || type.equals(GroupInviteEvents.InviteComplete)
                || type.equals(GroupJoinEvents.JoinSuccess)) {
            // LOG.fine( "InviteComplete");
            onListDirty();

        } else {
            LOG.severe("Unexpected event type: " + type);
        }
    }

    private void initFilter() {
        filterBar = new ToolBar();
        filterBar.add(new LabelToolItem("Filter: "));
        filter = new StoreFilterField<ExtUser>() {

            @Override
            protected boolean doSelect(Store<ExtUser> store, ExtUser parent, ExtUser record,
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
        DataProxy<List<ExtUser>> proxy = new DataProxy<List<ExtUser>>() {

            @Override
            public void load(DataReader<List<ExtUser>> reader, Object loadConfig,
                    AsyncCallback<List<ExtUser>> callback) {

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
        loader = new BaseTreeLoader<ExtUser>(proxy) {

            @Override
            public boolean hasChildren(ExtUser parent) {
                return parent instanceof ExtGroup;
            };
        };

        // tree store
        store = new TreeStore<ExtUser>(loader);
        store.setKeyProvider(new SenseKeyProvider<ExtUser>());
        store.setStoreSorter(new StoreSorter<ExtUser>(new SensorComparator<ExtUser>()));

        ColumnConfig id = new ColumnConfig(ExtUser.ID, "ID", 50);
        id.setHidden(true);
        ColumnConfig name = new ColumnConfig(ExtUser.NAME, "Name", 125);
        name.setRenderer(new TreeGridCellRenderer<TreeModel>());
        ColumnConfig surname = new ColumnConfig(ExtUser.SURNAME, "Surname", 125);
        ColumnConfig description = new ColumnConfig(ExtGroup.DESCRIPTION, "Description", 125);
        ColumnConfig isPublic = new ColumnConfig(ExtGroup.PUBLIC, "Public", 75);
        isPublic.setHidden(true);
        ColumnConfig isHidden = new ColumnConfig(ExtGroup.HIDDEN, "Hidden", 75);
        isHidden.setHidden(true);
        ColumnConfig isAnon = new ColumnConfig(ExtGroup.ANONYMOUS, "Anonymous", 75);
        isAnon.setHidden(true);
        ColumnModel cm = new ColumnModel(Arrays.asList(id, name, surname, description, isPublic,
                isHidden, isAnon));

        grid = new TreeGrid<ExtUser>(store, cm);
        grid.setAutoLoad(true);
        grid.setLoadMask(true);
        grid.setId("groupGrid");
        grid.setStateful(true);
        grid.setAutoExpandColumn(ExtGroup.DESCRIPTION);
        grid.setIconProvider(new SenseIconProvider<ExtUser>());
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
                    Dispatcher.forwardEvent(GroupJoinEvents.Show);
                } else if (source.equals(addUserButton)) {
                    onAddUserClick();
                } else {
                    LOG.warning("Unexpected button pressed: " + source);
                }
            }
        };

        createButton = new Button("Create", l);

        joinButton = new Button("Join", l);

        leaveButton = new Button("Leave", l);
        leaveButton.disable();

        addUserButton = new Button("Add User", l);
        addUserButton.disable();

        // handle selections
        TreeGridSelectionModel<ExtUser> selectionModel = new TreeGridSelectionModel<ExtUser>();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<ExtUser>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<ExtUser> se) {
                ExtUser selection = se.getSelectedItem();
                if (null != selection) {
                    leaveButton.enable();
                    addUserButton.enable();
                } else {
                    leaveButton.disable();
                    addUserButton.disable();
                }
            }
        });
        grid.setSelectionModel(selectionModel);

        // create tool bar
        toolBar = new ToolBar();
        toolBar.add(joinButton);
        toolBar.add(createButton);
        toolBar.add(addUserButton);
        toolBar.add(leaveButton);
    }

    private void onAddUserClick() {
        ExtUser selected = grid.getSelectionModel().getSelectedItem();
        ExtGroup group = null;
        if (selected instanceof ExtGroup) {
            group = (ExtGroup) selected;
        } else if (selected.getParent() instanceof ExtGroup) {
            group = (ExtGroup) selected.getParent();
        } else {
            MessageBox.alert(null, "Cannot add user to group: no group selected.", null);
            return;
        }

        AppEvent invite = new AppEvent(GroupInviteEvents.ShowInviter);
        invite.setData("group", group);
        Dispatcher.forwardEvent(invite);
    }

    private void onLeaveClick() {
        ExtUser group = grid.getSelectionModel().getSelectedItem();
        while (!(group instanceof ExtGroup)) {
            group = (ExtUser) group.getParent();
        }
        AppEvent event = new AppEvent(GroupLeaveEvents.LeaveRequest);
        event.setData("group", group);
        Dispatcher.forwardEvent(event);
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
            panel.getHeader().setIconStyle("sense-btn-icon-loading");
        } else {
            panel.getHeader().setIconStyle("");
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
