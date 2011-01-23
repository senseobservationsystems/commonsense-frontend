package nl.sense_os.commonsense.client.mvc.views;

import java.util.Comparator;
import java.util.List;

import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.TagsEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.TagModel;

import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.ModelStringProvider;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
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
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class TagsView extends View {

    private static final String TAG = "TagsView";
    private ContentPanel panel;
    private TreeStore<TreeModel> store;
    private TreePanel<TreeModel> tree;

    public TagsView(Controller controller) {
        super(controller);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(TagsEvents.ShowTags)) {
            onShow(event);
        } else if (type.equals(TagsEvents.TagsNotUpdated)) {
            Log.w(TAG, "TagsNotUpdated");
            onTagsNotUpdated(event);
        } else if (type.equals(TagsEvents.TagsUpdated)) {
            Log.d(TAG, "TagsUpdated");
            onTagsUpdated(event);
        } else if (type.equals(LoginEvents.LoggedOut)) {
            // Log.d(TAG, "LoggedOut");
            onLoggedOut(event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        // trees store
        store = new TreeStore<TreeModel>();
        store.setKeyProvider(new ModelKeyProvider<TreeModel>() {

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
                    Log.e(TAG, "unexpected tag type in ModelKeyProvider");
                    return model.toString();
                }
            }
        });
        Comparator<Object> comparator = new Comparator<Object>() {

            @Override
            public int compare(Object obj1, Object obj2) {
                try {
                    TreeModel o1 = (TreeModel) obj1;
                    TreeModel o2 = (TreeModel) obj2;
                    int type1 = o1.<Integer> get("tagType");
                    int type2 = o2.<Integer> get("tagType");
                    if (type1 == type2 && type1 == TagModel.TYPE_SENSOR) {
                        String name1 = o1.<String> get("name");
                        String name2 = o2.<String> get("name");
                        return name1.compareToIgnoreCase(name2);
                    }
                    return 0;
                } catch (ClassCastException e) {
                    return 0;
                }
            }
        };
        StoreSorter<TreeModel> sorter = new StoreSorter<TreeModel>(comparator);
        store.setStoreSorter(sorter);

        this.tree = new TreePanel<TreeModel>(store);
        this.tree.setBorders(false);
        this.tree.setStateful(true);
        this.tree.setId("tagTree");
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

        ToolButton refresh = new ToolButton("x-tool-refresh");
        refresh.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                Dispatcher.get().dispatch(TagsEvents.TagsRequested);
            }
        });

        this.panel = new ContentPanel(new FitLayout());
        this.panel.setHeading("Devices and sensors");
        this.panel.setCollapsible(true);
        this.panel.add(this.tree);
        this.panel.getHeader().addTool(refresh);
        this.panel.setAnimCollapse(false);

        setupDragDrop();
        setupContextMenu();
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
            Log.e(TAG, "Failed to show tags panel. parent=null");
        }
    }

    private void onTagsNotUpdated(AppEvent event) {
        // Throwable caught = event.<Throwable> getData();
        // if (caught != null) {
        // caught.printStackTrace();
        // }
        this.store.removeAll();
    }

    private void onTagsUpdated(AppEvent event) {
        List<TreeModel> tags = event.<List<TreeModel>> getData();
        this.store.removeAll();
        this.store.add(tags, true);
    }

    /**
     * Sets up the tag tree panel for drag and drop of the tags.
     */
    private void setupDragDrop() {

        TreePanelDragSource source = new TreePanelDragSource(this.tree);
        source.setTreeStoreState(true);
    }
    
    private void setupContextMenu() {
        Menu contextMenu = new Menu();
        
        MenuItem foo = new MenuItem("foo", new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                Log.d(TAG, "foo");                
            }
        });
        contextMenu.add(foo);
        
        MenuItem bar = new MenuItem("bar", new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                Log.d(TAG, "bar");                
            }
        });
        
        contextMenu.add(bar);
        
        this.tree.setContextMenu(contextMenu);
    }
}
