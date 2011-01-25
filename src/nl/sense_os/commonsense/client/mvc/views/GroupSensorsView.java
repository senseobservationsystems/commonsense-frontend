package nl.sense_os.commonsense.client.mvc.views;

import java.util.List;

import nl.sense_os.commonsense.client.mvc.events.GroupSensorsEvents;
import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.SensorComparator;
import nl.sense_os.commonsense.shared.TagModel;

import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.ModelStringProvider;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class GroupSensorsView extends View {

    private static final String TAG = "GroupSensorsView";
    private ContentPanel panel;
    private TreeStore<TreeModel> store;
    private ToolButton refreshButton;
    private TreePanel<TreeModel> tree;

    public GroupSensorsView(Controller c) {
        super(c);
    }
    
    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(GroupSensorsEvents.ShowGroupSensors)) {
            onShow(event);
        } else if (type.equals(GroupSensorsEvents.GroupSensorsNotUpdated)) {
            Log.w(TAG, "GroupSensorsNotUpdated");
            onListUpdate(event);
        } else if (type.equals(GroupSensorsEvents.GroupSensorsUpdated)) {
            Log.d(TAG, "GroupSensorsUpdated");
            onListNotUpdated(event);
        } else if (type.equals(GroupSensorsEvents.GroupSensorsBusy)) {
            Log.d(TAG, "GroupSensorsBusy");
            setBusy(true);
        } else if (type.equals(LoginEvents.LoggedOut)) {
            // Log.d(TAG, "LoggedOut");
            onLoggedOut(event);
        } else {
            Log.e(TAG, "Unexpected event type: " + type);
        }
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

    private void onLoggedOut(AppEvent event) {
        this.store.removeAll();
    }

    private void onShow(AppEvent event) {
        ContentPanel parent = event.<ContentPanel> getData();
        if (null != parent) {
            parent.add(this.panel);
            parent.layout();
        } else {
            Log.e(TAG, "Failed to show group sensors panel: parent=null");
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.panel = new ContentPanel(new FitLayout());
        this.panel.setHeading("Group sensors");
        this.panel.setAnimCollapse(false);

        initTree();
        initHeaderTool();
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
                    return "sensor " + model.<String> get("id");
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
                    if (name.equals(deviceType)) {
                        return name;
                    }
                    return name + " (" + deviceType + ")";
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

    private void initHeaderTool() {
        this.refreshButton = new ToolButton("x-tool-refresh");
        this.refreshButton.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                Dispatcher.forwardEvent(GroupSensorsEvents.GroupSensorsRequested);
            }
        });
        this.panel.getHeader().addTool(this.refreshButton);
    }

    private void setBusy(boolean busy) {
        String icon = busy ? "gxt/images/gxt/icons/loading.gif" : "";
        this.panel.getHeader().setIcon(IconHelper.create(icon));
    }
}
