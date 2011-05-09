package nl.sense_os.commonsense.client.env.create;

import nl.sense_os.commonsense.client.utility.SensorIconProvider;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
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
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;

import java.util.Arrays;
import java.util.List;

public class EnvCreatorMapPanel extends ContentPanel {

    @SuppressWarnings("unused")
    private static final String TAG = "EnvCreatorMapPanel";
    private ButtonBar buttons;
    private Button fwdButton;
    private Button backButton;
    private Button cancelButton;
    private EnvMap map;
    private LayoutContainer mapControls;
    private ContentPanel outlineControls;
    private ContentPanel dropperControls;
    private ListLoader<ListLoadResult<SensorModel>> loader;
    private GroupingStore<SensorModel> store;
    private Grid<SensorModel> grid;

    public EnvCreatorMapPanel() {
        this.setLayout(new BorderLayout());
        this.setHeaderVisible(false);

        initMap();
        initControls();

        this.add(this.map, new BorderLayoutData(LayoutRegion.CENTER));
        BorderLayoutData westLayout = new BorderLayoutData(LayoutRegion.WEST);
        this.add(this.mapControls, westLayout);

        // add bottom buttons
        initButtons();
        this.setBottomComponent(this.buttons);
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

    private void initGrid() {
        // tree store
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
                    case 0:
                        f = "Feeds";
                        break;
                    case 1:
                        f = "Physical";
                        break;
                    case 2:
                        f = "States";
                        break;
                    case 3:
                        f = "Environment sensors";
                        break;
                    case 4:
                        f = "Public sensors";
                        break;
                    default:
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

        this.map = new EnvMap();
    }

    private void initControls() {

        this.mapControls = new LayoutContainer(new CardLayout());

        // controls for outline stage
        this.outlineControls = new ContentPanel(new ColumnLayout());
        this.outlineControls.setHeaderVisible(false);

        Button resetOutline = new Button("Reset", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                reset();
            }
        });
        resetOutline.setMinWidth(75);
        this.outlineControls.add(new Text("Draw the outline of the environment"));
        this.outlineControls.add(resetOutline);
        this.mapControls.add(this.outlineControls);

        // controls for dropper stage
        initGrid();
        this.dropperControls = new ContentPanel(new ColumnLayout());
        this.dropperControls.setHeaderVisible(false);
        this.dropperControls.add(new Text("Drop sensors on the map"));
        this.dropperControls.add(this.grid);
        this.mapControls.add(this.dropperControls);
    }

    /**
     * Initializes the Back/Forward/Cancel buttons for the creator wizard. The buttons trigger
     * events that are handled by {@link EnvCreator}.
     */
    private void initButtons() {

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
        this.buttons.add(this.backButton);
        this.buttons.add(this.fwdButton);
        this.buttons.add(this.cancelButton);
    }

    public void showDropper() {

        List<SensorModel> sensors = Registry.<List<SensorModel>> get(Constants.REG_MY_SENSORS_LIST);
        this.store.removeAll();
        this.store.add(sensors);

        ((CardLayout) this.mapControls.getLayout()).setActiveItem(this.dropperControls);
        this.map.setOutlineEnabled(false);
        this.map.setDroppingEnabled(true);
        this.fwdButton.setText("Finish");
    }

    public void showOutline() {
        ((CardLayout) this.mapControls.getLayout()).setActiveItem(this.outlineControls);
        this.map.resetSensors();
        this.map.setOutlineEnabled(true);
        this.map.setDroppingEnabled(false);
        this.fwdButton.setText("Next");
    }

    public boolean isDropperActive() {
        return ((CardLayout) this.mapControls.getLayout()).getActiveItem().equals(
                this.dropperControls);
    }

    public boolean isOutlineActive() {
        return ((CardLayout) this.mapControls.getLayout()).getActiveItem().equals(
                this.outlineControls);
    }

    public void reset() {
        this.map.resetSensors();
        this.map.resetOutline();
        this.fwdButton.setEnabled(false);
        showOutline();
    }

    public void setFwdEnabled(boolean enabled) {
        this.fwdButton.setEnabled(enabled);
    }
}
