package nl.sense_os.commonsense.client.mvc.views;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.ModelStringProvider;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanelSelectionModel;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import java.util.List;

import nl.sense_os.commonsense.client.mvc.events.GroupSensorsEvents;
import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.MainEvents;
import nl.sense_os.commonsense.client.mvc.events.VizEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.SensorComparator;
import nl.sense_os.commonsense.shared.TagModel;

public class GroupSensorsTree extends View {

    private static final String TAG = "GroupSensorsTree";
    private Button eventsButton;
    private ContentPanel panel;
    private ToolButton refreshButton;
    private TreeStore<TreeModel> store;
    private TreePanel<TreeModel> tree;
    private Button vizButton;

    public GroupSensorsTree(Controller c) {
        super(c);
    }
    
    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(GroupSensorsEvents.ShowTree)) {
            onShow(event);
        } else if (type.equals(GroupSensorsEvents.ListNotUpdated)) {
            Log.w(TAG, "ListNotUpdated");
            onListNotUpdated(event);
        } else if (type.equals(GroupSensorsEvents.ListUpdated)) {
            Log.d(TAG, "ListUpdated");
            onListUpdate(event);
        } else if (type.equals(GroupSensorsEvents.Working)) {
            Log.d(TAG, "Working");
            setBusy(true);
        } else if (type.equals(MainEvents.ShowVisualization)) {
            // Log.d(TAG, "ShowVisualization");
            // do nothing
        } else if (type.equals(LoginEvents.LoggedOut)) {
            // Log.d(TAG, "LoggedOut");
            onLoggedOut(event);
        } else if (type.equals(LoginEvents.LoggedIn)) {
            // Log.d(TAG, "LoggedIn");
            onLoggedIn(event);
        } else {
            Log.e(TAG, "Unexpected event type: " + type);
        }
    }

    private void initHeaderTool() {
        this.refreshButton = new ToolButton("x-tool-refresh");
        this.refreshButton.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                Dispatcher.forwardEvent(GroupSensorsEvents.ListRequested);
            }
        });
        this.panel.getHeader().addTool(this.refreshButton);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.panel = new ContentPanel(new FitLayout());
        this.panel.setHeading("My group sensors");
        this.panel.setAnimCollapse(false);

        initTree();
        initHeaderTool();
        initToolBar();
        
        setupDragDrop();
    }
    
    private void initToolBar() {

        // listen to toolbar button clicks
        final SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Button source = ce.getButton();
                if (source.equals(vizButton)) {
                    onVizClick();
                } else if (source.equals(eventsButton)) {
                    onEventsClick();
                } else {
                    Log.w(TAG, "Unexpected button pressed");
                }
            }
        };

        // initialize the buttons
        this.vizButton = new Button("Visualize", l);
        this.vizButton.disable();

        this.eventsButton = new Button("Events", l);
        this.eventsButton.disable();

        // listen to selection of tree items to enable/disable buttons
        TreePanelSelectionModel<TreeModel> selectionModel = new TreePanelSelectionModel<TreeModel>();
        selectionModel.setSelectionMode(SelectionMode.MULTI);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<TreeModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<TreeModel> se) {
                List<TreeModel> selection = se.getSelection();
                if (selection.size() > 0) {
                    vizButton.enable();
                    eventsButton.enable();
                } else {
                    vizButton.disable();
                    eventsButton.disable();
                }
            }
        });
        this.tree.setSelectionModel(selectionModel);

        // create tool bar
        final ToolBar toolBar = new ToolBar();
        toolBar.add(this.vizButton);
        toolBar.add(this.eventsButton);

        // add to panel
        this.panel.setTopComponent(toolBar);
    }

    private void initTree() {
        // trees store
        this.store = new TreeStore<TreeModel>();
        this.store.setKeyProvider(new ModelKeyProvider<TreeModel>() {

            @Override
            public String getKey(TreeModel model) {
                int tagType = model.<Integer> get("tagType");
                if (tagType == TagModel.TYPE_GROUP) {
                    return "group " + model.<String> get("name");
                } else if (tagType == TagModel.TYPE_DEVICE) {
                    return "device " + model.<String> get("uuid");
                } else if (tagType == TagModel.TYPE_SENSOR) {
                    return "sensor " + model.<String> get("id") + model.<String> get("owner_id");
                } else {
                    Log.e(TAG, "Unexpected tag type in ModelKeyProvider");
                    return model.toString();
                }
            }
        });

        // sort tree
        this.store.setStoreSorter(new StoreSorter<TreeModel>(new SensorComparator()));

        this.tree = new TreePanel<TreeModel>(store);
        this.tree.setBorders(false);
        this.tree.setStateful(true);
        this.tree.setId("groupSensorsTree");
        this.tree.setLabelProvider(new ModelStringProvider<TreeModel>() {

            @Override
            public String getStringValue(TreeModel model, String property) {
                int tagType = model.<Integer> get("tagType");
                if (tagType == TagModel.TYPE_GROUP) {
                    return model.<String> get("name");
                } else if (tagType == TagModel.TYPE_DEVICE) {
                    return model.<String> get("type");
                } else if (tagType == TagModel.TYPE_SENSOR) {
                    String name = model.<String> get("name");
                    String deviceType = model.<String> get("device_type");
                    String baseLabel = model.<String> get("owner_id") + ". " + name;
                    if (name.equals(deviceType)) {
                        return baseLabel;
                    }
                    return baseLabel + " (" + deviceType + ")";
                } else {
                    Log.e(TAG, "unexpected tag type in ModelStringProvider");
                    return model.toString();
                }
            }
        });
        this.tree.setIconProvider(new ModelIconProvider<TreeModel>() {

            @Override
            public AbstractImagePrototype getIcon(TreeModel model) {
                int tagType = model.<Integer> get("tagType");
                if (tagType == TagModel.TYPE_GROUP) {
                    return IconHelper.create("gxt/images/gxt/icons/folder.gif");
                } else if (tagType == TagModel.TYPE_DEVICE) {
                    return IconHelper.create("gxt/images/gxt/icons/folder.gif");
                } else if (tagType == TagModel.TYPE_SENSOR) {
                    return IconHelper.create("gxt/images/gxt/icons/tabs.gif");
                } else {
                    Log.e(TAG, "unexpected tag type in ModelIconProvider");
                    return IconHelper.create("gxt/images/gxt/icons/done.gif");
                }
            }
        });

        this.panel.add(this.tree);
    }

    protected void onEventsClick() {
        // TODO Auto-generated method stub

    }

    private void onListNotUpdated(AppEvent event) {
        // Throwable caught = event.<Throwable> getData();
        // if (caught != null) {
        // caught.printStackTrace();
        // }
        setBusy(false);
        this.store.removeAll();
    }

    private void onListUpdate(AppEvent event) {
        List<TreeModel> sensors = event.<List<TreeModel>> getData();
        setBusy(false);
        this.store.removeAll();
        this.store.add(sensors, true);
    }

    private void onLoggedIn(AppEvent event) {
        // do nothing, the group sensors store will be updated when the list of groups is ready
    }

    private void onLoggedOut(AppEvent event) {
        this.store.removeAll();
    }

    private void onShow(AppEvent event) {
        ContentPanel parent = event.<ContentPanel> getData();
        if (null != parent) {
            parent.add(this.panel);
            parent.layout();
            
            // Dispatcher.forwardEvent(GroupSensorEvents.ListRequested);
        } else {
            Log.e(TAG, "Failed to show group sensors panel: parent=null");
        }
    }

    private void onVizClick() {
        List<TreeModel> selection = tree.getSelectionModel().getSelection();
        for (TreeModel sensor : selection) {
            
            TreeModel parent = sensor.getParent();
            if (null != parent) {
                sensor.set("alias", parent.<String> get("id"));
            }
        }
        // TODO get child sensors of selected users, groups and devices 
        Dispatcher.forwardEvent(VizEvents.ShowTypeChoice, selection);                
    }

    private void setBusy(boolean busy) {
        String icon = busy ? "gxt/images/gxt/icons/loading.gif" : "";
        this.panel.getHeader().setIcon(IconHelper.create(icon));
    }

    /**
     * Sets up the tag tree panel for drag and drop of the tags.
     */
    private void setupDragDrop() {
        TreePanelDragSource source = new TreePanelDragSource(this.tree);
        source.setTreeStoreState(true);
    }
}
