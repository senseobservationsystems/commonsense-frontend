package nl.sense_os.commonsense.client.env.create;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import nl.sense_os.commonsense.client.utility.SensorIconProvider;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.dnd.GridDragSource;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.Polygon;

public class EnvCreatorMapPanel extends ContentPanel {

    @SuppressWarnings("unused")
    private static final String TAG = "EnvCreatorMapPanel";
    private ButtonBar buttons;
    private Button fwdButton;
    private Button backButton;
    private Button cancelButton;
    private EnvMap map;
    private ContentPanel mapControls;
    private GroupingStore<SensorModel> store;
    private Grid<SensorModel> grid;
    private boolean isDropperActive;
    private ContentPanel explanation;
    private LayoutContainer outlineExpl;
    private LayoutContainer dropperExpl;

    public EnvCreatorMapPanel() {
        this.setLayout(new BorderLayout());
        this.setHeaderVisible(false);

        initMap();
        initControls();
        initExplanation();

        this.add(this.map, new BorderLayoutData(LayoutRegion.CENTER));
        this.add(this.mapControls, new BorderLayoutData(LayoutRegion.WEST, .33f, 275, 2000));
        this.add(this.explanation, new BorderLayoutData(LayoutRegion.NORTH, 25));

        // add bottom buttons
        initButtons();
        this.setBottomComponent(this.buttons);

        setupDragDrop();
    }

    private ColumnModel createColumnModel() {
        ColumnConfig id = new ColumnConfig(SensorModel.ID, "ID", 50);
        id.setHidden(true);
        ColumnConfig type = new ColumnConfig(SensorModel.TYPE, "Type", 50);
        ColumnConfig name = new ColumnConfig(SensorModel.NAME, "Name", 200);
        ColumnConfig devType = new ColumnConfig(SensorModel.DEVICE_TYPE, "Physical sensor", 200);
        devType.setRenderer(new GridCellRenderer<SensorModel>() {

            @Override
            public Object render(SensorModel model, String property, ColumnData config,
                    int rowIndex, int colIndex, ListStore<SensorModel> store, Grid<SensorModel> grid) {
                if (!model.getDeviceType().equals(model.getName())) {
                    return model.getDeviceType();
                } else {
                    return "";
                }
            }
        });
        ColumnConfig devId = new ColumnConfig(SensorModel.DEVICE_ID, "Device ID", 50);
        devId.setHidden(true);
        ColumnConfig device = new ColumnConfig(SensorModel.DEVICE_DEVTYPE, "Device", 200);
        type.setRenderer(new GridCellRenderer<SensorModel>() {

            @Override
            public Object render(SensorModel model, String property, ColumnData config,
                    int rowIndex, int colIndex, ListStore<SensorModel> store, Grid<SensorModel> grid) {
                SensorIconProvider<SensorModel> provider = new SensorIconProvider<SensorModel>();
                provider.getIcon(model).getHTML();
                return provider.getIcon(model).getHTML();
            }
        });
        ColumnConfig dataType = new ColumnConfig(SensorModel.DATA_TYPE, "Data type", 100);
        dataType.setHidden(true);
        ColumnConfig owner = new ColumnConfig(SensorModel.OWNER, "Owner", 100);

        ColumnModel cm = new ColumnModel(Arrays.asList(type, id, name, devType, devId, device,
                dataType, owner));

        return cm;
    }

    /**
     * Initializes the Back/Forward/Cancel buttons for the creator wizard. The buttons trigger
     * events that are handled by {@link EnvCreator}.
     */
    private void initButtons() {

        Button resetButton = new Button("Reset", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                reset();
            }
        });
        resetButton.setMinWidth(75);

        // forward button
        this.fwdButton = new Button("Next", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Dispatcher.forwardEvent(EnvCreateEvents.Forward);
            }
        });
        this.fwdButton.setMinWidth(75);
        this.fwdButton.setEnabled(false);

        // back button
        this.backButton = new Button("Back", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Dispatcher.forwardEvent(EnvCreateEvents.Back);
            }
        });
        this.backButton.setMinWidth(75);

        // cancel button
        this.cancelButton = new Button("Cancel", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Dispatcher.forwardEvent(EnvCreateEvents.Cancel);
            }
        });
        this.cancelButton.setMinWidth(75);

        this.buttons = new ButtonBar();
        this.buttons.setAlignment(HorizontalAlignment.RIGHT);
        this.buttons.add(resetButton);
        this.buttons.add(this.backButton);
        this.buttons.add(this.fwdButton);
        this.buttons.add(this.cancelButton);
    }

    private void initControls() {

        this.mapControls = new ContentPanel(new FitLayout());
        this.mapControls.setHeaderVisible(false);

        // controls for dropper stage
        initGrid();
        this.mapControls.add(this.grid);
    }

    private void initExplanation() {
        this.explanation = new ContentPanel(new CardLayout());
        this.explanation.setHeaderVisible(false);
        this.explanation.setStyleAttribute("backgroundColor", "white");

        this.outlineExpl = new LayoutContainer();
        this.outlineExpl.add(new Text("Click on the map to draw the outline for the environment."),
                new FlowData(new Margins(5)));
        this.dropperExpl = new LayoutContainer();
        this.dropperExpl.add(new Text(
                "Drag and drop sensors on the map to add them to the environment."), new FlowData(
                new Margins(5)));
        this.explanation.add(this.outlineExpl);
        this.explanation.add(this.dropperExpl);
    }

    private void initGrid() {
        // tree store
        this.store = new GroupingStore<SensorModel>();
        this.store.setKeyProvider(new ModelKeyProvider<SensorModel>() {

            @Override
            public String getKey(SensorModel model) {
                return model.getId() + model.getName() + model.getDeviceType() + model.getType();
            }

        });
        // this.store.setStoreSorter(new StoreSorter<SensorModel>(new SensorComparator()));
        this.store.groupBy(SensorModel.TYPE);
        this.store.setDefaultSort(SensorModel.TYPE, SortDir.DESC);
        this.store.setSortField(SensorModel.TYPE);

        // Column model
        ColumnModel cm = createColumnModel();

        GroupingView groupingView = new GroupingView();
        groupingView.setShowGroupedColumn(true);
        groupingView.setForceFit(true);
        groupingView.setGroupRenderer(new GridGroupRenderer() {
            public String render(GroupColumnData data) {
                if (data.field.equals(SensorModel.TYPE)) {
                    int group = Integer.parseInt(data.group);
                    String f = data.group;
                    switch (group) {
                        case 0 :
                            f = "Feeds";
                            break;
                        case 1 :
                            f = "Physical";
                            break;
                        case 2 :
                            f = "States";
                            break;
                        case 3 :
                            f = "Environment sensors";
                            break;
                        case 4 :
                            f = "Public sensors";
                            break;
                        default :
                            f = "Unsorted";
                    }
                    String l = data.models.size() == 1 ? "Sensor" : "Sensors";
                    return f + " (" + data.models.size() + " " + l + ")";
                } else {
                    if (data.group.equals("")) {
                        return "Ungrouped";
                    } else {
                        return data.group;
                    }
                }
            }
        });

        this.grid = new Grid<SensorModel>(this.store, cm);
        this.grid.setView(groupingView);
        this.grid.setBorders(false);
        this.grid.setStateful(true);
        this.grid.setLoadMask(true);
        this.grid.setId("mySensorsGrid");
    }

    private void initMap() {
        if (this.map != null) {
            this.remove(this.map);
        }
        this.map = new EnvMap();
    }

    public boolean isDropperActive() {
        return isDropperActive;
    }

    public boolean isOutlineActive() {
        return !isDropperActive;
    }

    public void reset() {

        List<SensorModel> sensors = Registry.<List<SensorModel>> get(Constants.REG_MY_SENSORS_LIST);
        this.store.removeAll();
        this.store.add(sensors);

        initMap();
        this.add(this.map, new BorderLayoutData(LayoutRegion.CENTER));
        // this.map.resetSensors();
        // this.map.resetOutline();
        this.fwdButton.setEnabled(false);
        showOutline();
    }

    public void setFwdEnabled(boolean enabled) {
        this.fwdButton.setEnabled(enabled);
    }

    private void setupDragDrop() {

        GridDragSource source = new GridDragSource(this.grid);
        source.setGroup("env-creator");
    }

    public void showDropper() {

        // ((BorderLayout) this.getLayout()).expand(LayoutRegion.WEST);
        // this.layout();
        this.grid.enable();

        ((CardLayout) this.explanation.getLayout()).setActiveItem(this.dropperExpl);
        layout();

        this.map.setOutlineEnabled(false);
        this.map.setDroppingEnabled(true);
        this.fwdButton.setText("Finish");

        this.isDropperActive = true;
    }

    public void showOutline() {
        // ((BorderLayout) this.getLayout()).collapse(LayoutRegion.WEST);
        // this.layout();
        this.grid.disable();

        ((CardLayout) this.explanation.getLayout()).setActiveItem(this.outlineExpl);
        layout();

        this.map.resetSensors();
        this.map.setOutlineEnabled(true);
        this.map.setDroppingEnabled(false);
        this.fwdButton.setText("Next");

        this.isDropperActive = false;
    }

    public Map<Marker, List<SensorModel>> getSensors() {
        return this.map.getSensors();
    }

    public Polygon getOutline() {
        return this.map.getOutline();
    }
}
