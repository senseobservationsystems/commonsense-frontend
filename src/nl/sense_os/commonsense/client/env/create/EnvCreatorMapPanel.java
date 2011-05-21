package nl.sense_os.commonsense.client.env.create;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.sensors.library.LibraryColumnsFactory;
import nl.sense_os.commonsense.client.sensors.library.SensorGroupRenderer;
import nl.sense_os.commonsense.client.utility.SenseKeyProvider;
import nl.sense_os.commonsense.client.utility.SensorProcessor;
import nl.sense_os.commonsense.shared.constants.Constants;
import nl.sense_os.commonsense.shared.models.SensorModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.dnd.GridDragSource;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
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
    private static final Logger logger = Logger.getLogger("EnvCreatorMapPanel");
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
        this.store.setKeyProvider(new SenseKeyProvider<SensorModel>());
        this.store.groupBy(SensorModel.DEVICE_UUID);

        // Column model
        ColumnModel cm = LibraryColumnsFactory.create();

        GroupingView groupingView = new GroupingView();
        groupingView.setShowGroupedColumn(true);
        groupingView.setForceFit(true);
        groupingView.setGroupRenderer(new SensorGroupRenderer(cm));

        this.grid = new Grid<SensorModel>(this.store, cm);
        this.grid.setModelProcessor(new SensorProcessor<SensorModel>());
        this.grid.setView(groupingView);
        this.grid.setBorders(false);
        this.grid.setStateful(true);
        this.grid.setLoadMask(true);
        this.grid.setId("envSensorsGrid");

        this.grid.getSelectionModel().addSelectionChangedListener(
                new SelectionChangedListener<SensorModel>() {

                    @Override
                    public void selectionChanged(SelectionChangedEvent<SensorModel> se) {
                        SensorModel selected = se.getSelectedItem();
                        if (selected != null && selected.getDevice() != null) {

                            // select all other sensors from this device
                            grid.getSelectionModel().setFiresEvents(false);
                            grid.getSelectionModel().deselectAll();
                            List<SensorModel> sensors = store.getModels();
                            for (SensorModel sensor : sensors) {
                                if (sensor.getDevice() != null) {
                                    if (sensor.getDevice().getId()
                                            .equals(selected.getDevice().getId())) {
                                        grid.getSelectionModel().select(true, sensor);
                                    }
                                }
                            }
                            grid.getSelectionModel().setFiresEvents(true);
                        }
                    }
                });
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

        List<SensorModel> sensors = Registry.<List<SensorModel>> get(Constants.REG_SENSOR_LIST);
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
        source.addDNDListener(new DNDListener() {
            @Override
            public void dragDrop(DNDEvent e) {
                grid.getSelectionModel().deselectAll();
                super.dragDrop(e);
            }
        });
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
