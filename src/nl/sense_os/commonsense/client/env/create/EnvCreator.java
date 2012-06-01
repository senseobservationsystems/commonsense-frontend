package nl.sense_os.commonsense.client.env.create;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.common.utility.SensorOwnerFilter;
import nl.sense_os.commonsense.client.common.utility.SensorTextFilter;
import nl.sense_os.commonsense.client.env.components.EnvMap;
import nl.sense_os.commonsense.client.sensors.library.LibraryColumnsFactory;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Window;
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

public class EnvCreator extends View {

    private static final Logger LOGGER = Logger.getLogger(EnvCreator.class.getName());
    private Window window;

    private ContentPanel west;

    private FormPanel form;
    private TextField<String> name;
    private SpinnerField floors;

    private ContentPanel outlinePanel;
    private ContentPanel devicesPanel;

    private EnvMap map;

    private ButtonBar buttons;
    private Button submitButton;
    private Button cancelButton;
    protected boolean isFormValid;
    private boolean isOutlineValid;
    private ContentPanel sensorsPanel;
    private Grid<SensorModel> grid;
    private ToolBar sensorsFilterBar;
    private GroupingStore<SensorModel> sensorsStore;

    public EnvCreator(Controller c) {
	super(c);
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

    @Override
    protected void handleEvent(AppEvent event) {
	final EventType type = event.getType();

	if (type.equals(EnvCreateEvents.ShowCreator)) {
	    LOGGER.finest("NewCreator");
	    showPanel();

	} else if (type.equals(EnvCreateEvents.CreateSuccess)) {
	    LOGGER.finest("CreateSuccess");
	    onCreateSuccess();

	} else if (type.equals(EnvCreateEvents.CreateFailure)) {
	    LOGGER.warning("CreateFailure");
	    onCreateFailure();

	} else if (type.equals(EnvCreateEvents.OutlineComplete)) {
	    LOGGER.finest("OutlineComplete");
	    onOutlineComplete();

	} else {
	    LOGGER.warning("Unexpected event type: " + type);

	}
    }

    private void hidePanel() {
	LOGGER.finest("Hide panel...");

	window.hide();
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
		submit();
	    }
	});
	submitButton.setMinWidth(75);
	submitButton.disable();

	// cancel button
	cancelButton = new Button("Cancel", new SelectionListener<ButtonEvent>() {

	    @Override
	    public void componentSelected(ButtonEvent ce) {
		hidePanel();
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
	SensorTextFilter<SensorModel> textFilter = new SensorTextFilter<SensorModel>();
	textFilter.bind(sensorsStore);

	// filter to show only my own sensors
	final SensorOwnerFilter<SensorModel> ownerFilter = new SensorOwnerFilter<SensorModel>();
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
			LOGGER.finest("Name valid");
			isNameValid = true;
		    } else if (be.getField().equals(floors)) {
			LOGGER.finest("Floors valid");
			isFloorsValid = true;
		    }
		} else if (type.equals(Events.Invalid)) {
		    if (be.getField().equals(name)) {
			LOGGER.finest("Name invalid");
			isNameValid = false;
		    } else if (be.getField().equals(floors)) {
			LOGGER.finest("Floors invalid");
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

    @Override
    protected void initialize() {
	LOGGER.finest("Initialize...");

	window = new CenteredWindow();
	window.setHeading("Create new environment");
	window.setLayout(new BorderLayout());
	window.setMinWidth(720);
	window.setSize("85%", "600px");

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
	window.add(west, new BorderLayoutData(LayoutRegion.WEST, .33f, 275, 2000));
	window.add(map, new BorderLayoutData(LayoutRegion.CENTER));

	super.initialize();
    }

    private void initMapPanel() {
	map = new EnvMap();
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

	sensorsStore = new GroupingStore<SensorModel>();
	List<SensorModel> library = Registry.<List<SensorModel>> get(Constants.REG_SENSOR_LIST);
	sensorsStore.add(library);

	initSensorsFilters();

	CheckBoxSelectionModel<SensorModel> sm = new CheckBoxSelectionModel<SensorModel>();

	// column model
	List<ColumnConfig> cols = LibraryColumnsFactory.create().getColumns();
	cols.add(0, sm.getColumn());
	ColumnModel cm = new ColumnModel(cols);

	grid = new Grid<SensorModel>(sensorsStore, cm);
	grid.setSelectionModel(sm);
	grid.addPlugin(sm);

	sensorsPanel.setTopComponent(sensorsFilterBar);
	sensorsPanel.add(grid);
    }

    private void onCreateFailure() {
	MessageBox.confirm(null, "Failed to store the enviroment in CommonSense! Retry?",
		new Listener<MessageBoxEvent>() {

		    @Override
		    public void handleEvent(MessageBoxEvent be) {
			if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
			    submit();
			} else {
			    hidePanel();
			}
		    }
		});
    }

    private void onCreateSuccess() {
	MessageBox.info(null, "The environment was successfully stored in CommonSense.",
		new Listener<MessageBoxEvent>() {

		    @Override
		    public void handleEvent(MessageBoxEvent be) {
			hidePanel();
		    }
		});

    }

    private void onOutlineComplete() {
	isOutlineValid = true;
	checkValidity();
    }

    private void resetPanel() {
	LOGGER.finest("Reset panel...");

	form.reset();
	map.clearOutline();
	map.clearDevices();
	map.editOutline(false);
	map.editDevices(false);
	submitButton.disable();

	((AccordionLayout) west.getLayout()).setActiveItem(form);
    }

    private void showPanel() {
	resetPanel();

	window.show();
	window.center();
    }

    private void submit() {
	LOGGER.finest("SubmitRequest...");

	AppEvent create = new AppEvent(EnvCreateEvents.CreateRequest);
	create.setData("name", name.getValue());
	create.setData("floors", floors.getValue().intValue());
	create.setData("devices", map.getDevices());
	create.setData("sensors", grid.getSelectionModel().getSelectedItems());
	create.setData("outline", map.getOutline());
	fireEvent(create);
    }
}
