package nl.sense_os.commonsense.main.client.environmentmanagement.creating.component;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import nl.sense_os.commonsense.lib.client.model.apiclass.Device;
import nl.sense_os.commonsense.main.client.environmentmanagement.component.GxtEnvironmentMap;
import nl.sense_os.commonsense.main.client.environmentmanagement.creating.EnvironmentCreationView;
import nl.sense_os.commonsense.main.client.gxt.component.CenteredWindow;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.util.LibraryColumnsFactory;
import nl.sense_os.commonsense.main.client.gxt.util.SensorOwnerFilter;
import nl.sense_os.commonsense.main.client.gxt.util.SensorTextFilter;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Polygon;

public class GxtEnvironmentCreationDialog extends CenteredWindow implements EnvironmentCreationView {

    private static final Logger LOG = Logger
            .getLogger(GxtEnvironmentCreationDialog.class.getName());

    private ContentPanel west;

    private FormPanel form;
    private TextField<String> name;
    private SpinnerField floors;

    private ContentPanel outlinePanel;
    private ContentPanel devicesPanel;

    private GxtEnvironmentMap map;

    private ButtonBar buttons;
    private Button submitButton;
    private Button cancelButton;
    private boolean isFormValid;
    private boolean isOutlineValid;
    private ContentPanel sensorsPanel;
    private Grid<GxtSensor> grid;
    private ToolBar sensorsFilterBar;
    private GroupingStore<GxtSensor> sensorsStore;

    private Presenter presenter;

    public GxtEnvironmentCreationDialog() {
        setHeading("Create new environment");
        setLayout(new BorderLayout());
        setMinWidth(720);
        setSize("85%", "600px");

        west = new ContentPanel(new AccordionLayout());
        west.setHeaderVisible(false);

        initForm();
        initOutlinePanel();
        initDevicesPanel();
        initSensorsPanel();
        initMapPanel();
        initButtons();

        // do layout
        west.add(form);
        west.add(outlinePanel);
        west.add(devicesPanel);
        west.add(sensorsPanel);
        west.setBottomComponent(buttons);

        add(west, new BorderLayoutData(LayoutRegion.WEST, .33f, 275, 2000));
        add(map, new BorderLayoutData(LayoutRegion.CENTER));
    }

    private void checkValidity() {

        if (isFormValid) {
            form.setHeading("Step 1: Basic info");
        }

        if (isOutlineValid) {
            outlinePanel.setHeading("Step 2: Outline");
        }

        // submit button
        if (isFormValid && isOutlineValid) {
            submitButton.enable();
        } else {
            submitButton.disable();
        }
    }

    /**
     * Initializes the Back/Forward/Cancel buttons for the creator wizard. The buttons trigger
     * events that are handled by {@link EnvCreator}.
     */
    private void initButtons() {

        // forward button
        submitButton = new Button("Submit", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (null != presenter) {
                    presenter.onSubmitClick();
                }
            }
        });
        submitButton.setMinWidth(75);
        submitButton.disable();

        // cancel button
        cancelButton = new Button("Cancel", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (null != presenter) {
                    presenter.onCancelClick();
                }
            }
        });
        cancelButton.setMinWidth(75);

        buttons = new ButtonBar();
        buttons.setAlignment(HorizontalAlignment.CENTER);
        buttons.add(cancelButton);
        buttons.add(submitButton);
    }

    private void initDevicesPanel() {
        devicesPanel = new ContentPanel();
        // this.sensorsPanel.setStyleAttribute("background-color", "white");
        devicesPanel.setHeading("Step 3: Position devices");

        Listener<ComponentEvent> enabler = new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                map.editDevices(be.getType().equals(Events.Expand));
            }
        };
        devicesPanel.addListener(Events.Expand, enabler);
        devicesPanel.addListener(Events.Collapse, enabler);

        Text explanation = new Text("Click on the map to add devices to the new environment...");
        devicesPanel.add(explanation, new FlowData(10));

        Button resetButton = new Button("Reset", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                map.clearDevices();
                map.editOutline(false);
                map.editDevices(true);
                checkValidity();
            }
        });
        resetButton.setMinWidth(75);
        devicesPanel.add(resetButton, new FlowData(0, 10, 10, 10));
    }

    private void initSensorsFilters() {

        // text filter
        SensorTextFilter<GxtSensor> textFilter = new SensorTextFilter<GxtSensor>();
        textFilter.bind(sensorsStore);

        // filter to show only my own sensors
        final SensorOwnerFilter<GxtSensor> ownerFilter = new SensorOwnerFilter<GxtSensor>();
        sensorsStore.addFilter(ownerFilter);

        // checkbox to toggle filter
        final CheckBox filterOnlyMe = new CheckBox();
        filterOnlyMe.setBoxLabel("Only my own sensors");
        filterOnlyMe.setHideLabel(true);
        filterOnlyMe.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {

                ownerFilter.setEnabled(filterOnlyMe.getValue());
                sensorsStore.applyFilters(null);
            }
        });

        // add filters to filter bar
        sensorsFilterBar = new ToolBar();
        sensorsFilterBar.add(new LabelToolItem("Filter: "));
        sensorsFilterBar.add(textFilter);
        sensorsFilterBar.add(new SeparatorToolItem());
        sensorsFilterBar.add(filterOnlyMe);
    }

    private void initForm() {

        form = new FormPanel();
        form.setHeading("Step 1: Basic info");
        form.setBodyBorder(false);

        Listener<ComponentEvent> enabler = new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                if (be.getType().equals(Events.Collapse)) {
                    if (isFormValid) {
                        form.setHeading("Step 1: Basic info");
                    } else {
                        form.setHeading("Step 1: Basic info [UNFINISHED]");
                    }
                }
            }
        };
        form.addListener(Events.Expand, enabler);
        form.addListener(Events.Collapse, enabler);

        initFormFields();

        form.add(name, new FormData("-10"));
        form.add(floors, new FormData("-10"));
    }

    /**
     * Initializes the form fields.
     */
    private void initFormFields() {

        name = new TextField<String>();
        name.setFieldLabel("Name");
        name.setAllowBlank(false);

        floors = new SpinnerField();
        floors.setFieldLabel("Number of floors");
        floors.setPropertyEditorType(Integer.class);
        floors.setFormat(NumberFormat.getFormat("#"));
        floors.setAllowDecimals(false);
        floors.setAllowBlank(false);
        floors.setMinValue(1);
        floors.setOriginalValue(1);

        // listen for form validity
        Listener<FieldEvent> validListener = new Listener<FieldEvent>() {

            private boolean isNameValid;
            private boolean isFloorsValid = true;

            @Override
            public void handleEvent(FieldEvent be) {
                EventType type = be.getType();
                if (type.equals(Events.Valid)) {
                    if (be.getField().equals(name)) {
                        LOG.finest("Name valid");
                        isNameValid = true;
                    } else if (be.getField().equals(floors)) {
                        LOG.finest("Floors valid");
                        isFloorsValid = true;
                    }
                } else if (type.equals(Events.Invalid)) {
                    if (be.getField().equals(name)) {
                        LOG.finest("Name invalid");
                        isNameValid = false;
                    } else if (be.getField().equals(floors)) {
                        LOG.finest("Floors invalid");
                        isFloorsValid = false;
                    }
                }
                isFormValid = isNameValid && isFloorsValid;
                checkValidity();
            }
        };
        name.addListener(Events.Valid, validListener);
        name.addListener(Events.Invalid, validListener);
        floors.addListener(Events.Valid, validListener);
        floors.addListener(Events.Invalid, validListener);
    }

    private void initMapPanel() {
        map = new GxtEnvironmentMap();
    }

    private void initOutlinePanel() {
        outlinePanel = new ContentPanel();
        // this.outlinePanel.setStyleAttribute("background-color", "white");
        outlinePanel.setHeading("Step 2: Outline");

        Listener<ComponentEvent> enabler = new Listener<ComponentEvent>() {

            private boolean hasBeenExpanded;

            @Override
            public void handleEvent(ComponentEvent be) {
                map.editOutline(be.getType().equals(Events.Expand));

                if (be.getType().equals(Events.Expand)) {
                    hasBeenExpanded = true;
                }

                if (hasBeenExpanded && be.getType().equals(Events.Collapse)) {
                    if (isOutlineValid) {
                        outlinePanel.setHeading("Step 2: Outline");
                    } else {
                        outlinePanel.setHeading("Step 2: Outline [UNFINISHED]");
                    }
                }
            }
        };
        outlinePanel.addListener(Events.Expand, enabler);
        outlinePanel.addListener(Events.Collapse, enabler);

        Text explanation = new Text("Click the map to draw an outline for your environment");
        outlinePanel.add(explanation, new FlowData(10));

        Button resetButton = new Button("Reset", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                map.clearOutline();
                map.editOutline(true);
                map.editDevices(false);
                isOutlineValid = false;
                checkValidity();
            }
        });
        resetButton.setMinWidth(75);
        outlinePanel.add(resetButton, new FlowData(0, 10, 10, 10));
    }

    private void initSensorsPanel() {

        sensorsPanel = new ContentPanel(new FitLayout());
        // this.sensorsPanel.setStyleAttribute("background-color", "white");
        sensorsPanel.setHeading("Step 4: Other sensors");

        Listener<ComponentEvent> enabler = new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                // do something fancy
            }
        };
        sensorsPanel.addListener(Events.Expand, enabler);
        sensorsPanel.addListener(Events.Collapse, enabler);

        Text explanation = new Text(
                "Select any additional sensors that are related to your environment");
        LayoutContainer explWrapper = new LayoutContainer();
        explWrapper.add(explanation, new FlowData(10));
        sensorsPanel.setTopComponent(explWrapper);

        sensorsStore = new GroupingStore<GxtSensor>();
        List<GxtSensor> library = Registry
                .<List<GxtSensor>> get(nl.sense_os.commonsense.shared.client.util.Constants.REG_SENSOR_LIST);
        sensorsStore.add(library);

        initSensorsFilters();

        CheckBoxSelectionModel<GxtSensor> sm = new CheckBoxSelectionModel<GxtSensor>();

        // column model
        List<ColumnConfig> cols = LibraryColumnsFactory.create().getColumns();
        cols.add(0, sm.getColumn());
        ColumnModel cm = new ColumnModel(cols);

        grid = new Grid<GxtSensor>(sensorsStore, cm);
        grid.setSelectionModel(sm);
        grid.addPlugin(sm);

        sensorsPanel.setTopComponent(sensorsFilterBar);
        sensorsPanel.add(grid);
    }

    @Override
    public void setBusy(boolean busy) {
        if (busy) {
            submitButton.setIconStyle("sense-btn-icon-loading");
        } else {
            submitButton.setIconStyle("sense-btn-icon=go");
        }
        submitButton.setEnabled(!busy);
    }

    @Override
    public Map<Device, LatLng> getDevicePositions() {
        return map.getDevices();
    }

    @Override
    public int getFloors() {
        return floors.getValue().intValue();
    }

    @Override
    public String getName() {
        return name.getValue();
    }

    @Override
    public Polygon getOutline() {
        return map.getOutline();
    }

    @Override
    public List<GxtSensor> getSensors() {
        return grid.getSelectionModel().getSelectedItems();
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

}
