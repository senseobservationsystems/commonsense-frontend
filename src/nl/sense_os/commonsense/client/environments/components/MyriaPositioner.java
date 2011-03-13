package nl.sense_os.commonsense.client.environments.components;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.ModelStringProvider;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.dnd.DND.Operation;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.store.TreeStoreModel;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.AbsoluteData;
import com.extjs.gxt.ui.client.widget.layout.AbsoluteLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.services.SensorsProxyAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.TagModel;
import nl.sense_os.commonsense.shared.building.FloorModel;

public class MyriaPositioner extends ContentPanel {

    private static final String TAG = MyriaPositioner.class.getName();
    private LayoutContainer imageContainer;
    @SuppressWarnings("unused")
    private final SensorsProxyAsync sensorDataService;
    final TreeStore<TreeModel> store = new TreeStore<TreeModel>();

    private TreePanel<TreeModel> tagTree;

    public MyriaPositioner() {
        sensorDataService = Registry.<SensorsProxyAsync> get(Constants.REG_SENSORS_PROXY);

        // building selection panel for west part of widget
        Component tagPanel = createTagPanel();
        tagPanel.setItemId("west_panel");

        // tool bar
        ToolBar toolBar = createToolBar();
        this.setTopComponent(toolBar);

        // add panels to the layout
        this.setHeaderVisible(false);
        this.setLayout(new BorderLayout());
        this.setStyleAttribute("backgroundColor", "white");
        this.add(tagPanel, new BorderLayoutData(LayoutRegion.WEST));
    }
    
    /**
     * Creates a tree of TagModels, which are fetched asynchronously. The TagModels represent users,
     * devices or sensor types.
     * 
     * @return the tree
     */
    private ContentPanel createTagPanel() {

        // request tags to populate the tree
        getTags();

        // trees store
        store.setKeyProvider(new ModelKeyProvider<TreeModel>() {

            @Override
            public String getKey(TreeModel model) {
                int tagType = model.<Integer> get("tagType");
                if (tagType == TagModel.TYPE_DEVICE) {
                    return "device " + model.<String> get("uuid");
                } else if (tagType == TagModel.TYPE_SENSOR) {
                    return "sensor " + model.<String> get("id");
                } else {
                    Log.e(TAG, "unexpected tag type in ModelKeyProvider");
                    return model.toString();
                }
            }
        });

        this.tagTree = new TreePanel<TreeModel>(store);
        this.tagTree.setBorders(false);
        this.tagTree.setStateful(true);
        this.tagTree.setId("idNecessaryForStatefulSetting");
        this.tagTree.setLabelProvider(new ModelStringProvider<TreeModel>() {

            @Override
            public String getStringValue(TreeModel model, String property) {
                int tagType = model.<Integer> get("tagType");
                if (tagType == TagModel.TYPE_DEVICE) {
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
        this.tagTree.getStyle().setLeafIcon(IconHelper.create("gxt/images/default/tree/leaf.gif"));

        final ContentPanel panel = new ContentPanel(new FitLayout());
        panel.setHeading("Tag tree");
        panel.setCollapsible(true);
        panel.add(this.tagTree);

        return panel;
    }
    
    private ToolBar createToolBar() {
        final ToolBar bar = new ToolBar();
        final Button saveBtn = new Button("Save", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                onSave();
            }
        });
        final Button cancelBtn = new Button("Cancel", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                onCancel();
            }
        });
        bar.add(saveBtn);
        bar.add(cancelBtn);

        return bar;
    }

    private void getTags() {
        SensorsProxyAsync service = Registry.<SensorsProxyAsync> get(Constants.REG_SENSORS_PROXY);
        String sessionId = Registry.get(Constants.REG_SESSION_ID);
        AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

            @Override
            public void onFailure(Throwable caught) {
                Log.e(TAG, "Failed getting tags: " + caught.getMessage());
            }

            @Override
            public void onSuccess(List<TreeModel> result) {
                store.removeAll();
                store.add(result, true);
            }
        };
        service.getMySensors(sessionId, callback);
    }

    private void onCancel() {
        fireEvent(Events.CancelEdit);
    }

    private void onSave() {
        fireEvent(Events.Complete);
    }

    private void onTagsDropped(DNDEvent event) {
        final ArrayList<TreeStoreModel> data = event.<ArrayList<TreeStoreModel>> getData();

        int x = event.getClientX() - imageContainer.getAbsoluteLeft();
        int y = event.getClientY() - imageContainer.getAbsoluteTop();
        Log.d(TAG, data.size() + " tags dropped at: (" + x + ", " + y + ")");
        
        Image node = new Image("/img/staticNodeIcon.png");
        imageContainer.add(node, new AbsoluteData(x, y));
        imageContainer.layout();
        
        // TODO post MyriaNode location as sensor value
    }

    public void setFloor(FloorModel floor) {

        // update the image
        if (null != imageContainer) {
            remove(imageContainer);
        }
        final Image image = new Image(floor.getUrl());
        imageContainer = new LayoutContainer(new AbsoluteLayout());
        imageContainer.add(image, new AbsoluteData());
        final ContentPanel imagePanel = new ContentPanel(new CenterLayout());
        imagePanel.setHeaderVisible(false);
        imagePanel.setItemId("center_panel");
        imagePanel.add(imageContainer);
        image.addLoadHandler(new LoadHandler() {

            @Override
            public void onLoad(LoadEvent event) {
                int panelWidth = imagePanel.getWidth();
                int panelHeight = imagePanel.getHeight();
                int imgWidth = image.getWidth();
                int imgHeight = image.getHeight();

                float clientScale = panelWidth / panelHeight;
                float imgScale = imgWidth / imgHeight;

                if (imgScale > clientScale) {
                    if (imgWidth > panelWidth - 20) {
                        image.setWidth((panelWidth - 20) + "px");
                    }
                } else {
                    if (imgHeight > panelHeight - 20) {
                        image.setHeight((panelHeight - 20) + "px");
                    }
                }
                imageContainer.setWidth(image.getWidth());
                imageContainer.setHeight(image.getHeight());

                imagePanel.remove(imageContainer);
                imagePanel.add(imageContainer);
                imagePanel.layout();
            }
        });

        setupDragDrop();

        add(imagePanel, new BorderLayoutData(LayoutRegion.CENTER));
    }

    private void setupDragDrop() {
        TreePanelDragSource source = new TreePanelDragSource(this.tagTree);
        source.setTreeStoreState(true);
        source.addDNDListener(new DNDListener() {
            
            @Override
            public void dragDrop(DNDEvent e) {
                onTagsDropped(e);
            }
        });

        final DropTarget dropTarget = new DropTarget(this.imageContainer);
        dropTarget.setOperation(Operation.COPY);
    }
}
