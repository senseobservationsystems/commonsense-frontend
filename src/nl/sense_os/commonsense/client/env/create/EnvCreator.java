package nl.sense_os.commonsense.client.env.create;

import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.client.common.models.SensorModel;

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
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.FormData;
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

    private GroupingStore<SensorModel> store;
    private Grid<SensorModel> grid;

    private ButtonBar buttons;
    private Button submitButton;
    private Button cancelButton;
    protected boolean isFormValid;
    private boolean isOutlineValid;

    public EnvCreator(Controller c) {
        super(c);
        LOGGER.setLevel(Level.ALL);
    }

    private void checkValidity() {
        if (isFormValid && isOutlineValid) {
            this.submitButton.enable();
        } else {
            this.submitButton.disable();
        }
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(EnvCreateEvents.ShowCreator)) {
            LOGGER.finest("ShowCreator");
            showPanel();

        } else if (type.equals(EnvCreateEvents.CreateSuccess)) {
            LOGGER.finest("CreateSuccess");
            onCreateSuccess();

        } else if (type.equals(EnvCreateEvents.CreateFailure)) {
            LOGGER.warning("CreateFailure");
            onCreateFailure();

        } else if (type.equals(EnvCreateEvents.OutlineComplete)) {
            LOGGER.warning("OutlineComplete");
            onOutlineComplete();

        } else {
            LOGGER.warning("Unexpected event type: " + type);

        }
    }

    private void hidePanel() {
        LOGGER.finest("Hide panel...");

        this.window.hide();
    }

    /**
     * Initializes the Back/Forward/Cancel buttons for the creator wizard. The buttons trigger
     * events that are handled by {@link EnvCreator}.
     */
    private void initButtons() {

        // forward button
        this.submitButton = new Button("Submit", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                submit();
            }
        });
        this.submitButton.setMinWidth(75);
        this.submitButton.disable();

        // cancel button
        this.cancelButton = new Button("Cancel", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                hidePanel();
            }
        });
        this.cancelButton.setMinWidth(75);

        this.buttons = new ButtonBar();
        this.buttons.setAlignment(HorizontalAlignment.CENTER);
        this.buttons.add(this.cancelButton);
        this.buttons.add(this.submitButton);
    }

    private void initForm() {

        this.form = new FormPanel();
        form.setHeading("Step 1: Basic info");
        form.setBodyBorder(false);

        this.form.addListener(Events.Expand, new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                map.setOutlineEnabled(false);
                map.setDroppingEnabled(false);
            }
        });

        initFormFields();

        this.form.add(this.name, new FormData("-10"));
        this.form.add(this.floors, new FormData("-10"));
    }

    /**
     * Initializes the form fields.
     */
    private void initFormFields() {

        this.name = new TextField<String>();
        this.name.setFieldLabel("Name");
        this.name.setAllowBlank(false);

        this.floors = new SpinnerField();
        this.floors.setFieldLabel("Number of floors");
        this.floors.setPropertyEditorType(Integer.class);
        this.floors.setFormat(NumberFormat.getFormat("#"));
        this.floors.setAllowDecimals(false);
        this.floors.setAllowBlank(false);
        this.floors.setMinValue(1);
        this.floors.setOriginalValue(1);

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
        this.name.addListener(Events.Valid, validListener);
        this.name.addListener(Events.Invalid, validListener);
        this.floors.addListener(Events.Valid, validListener);
        this.floors.addListener(Events.Invalid, validListener);
    }

    @Override
    protected void initialize() {
        LOGGER.finest("Initialize...");

        this.window = new CenteredWindow();
        this.window.setHeading("Create new environment");
        this.window.setLayout(new BorderLayout());
        this.window.setMinWidth(720);
        this.window.setSize("85%", "600px");

        this.west = new ContentPanel(new AccordionLayout());
        this.west.setHeaderVisible(false);

        initForm();
        initOutlinePanel();
        initDevicesPanel();
        initMapPanel();
        initButtons();

        // do layout
        this.west.add(this.form);
        this.west.add(this.outlinePanel);
        this.west.add(this.devicesPanel);
        this.west.setBottomComponent(buttons);
        this.window.add(this.west, new BorderLayoutData(LayoutRegion.WEST, .33f, 275, 2000));
        this.window.add(this.map, new BorderLayoutData(LayoutRegion.CENTER));

        super.initialize();
    }

    private void initMapPanel() {
        this.map = new EnvMap();
    }

    private void initOutlinePanel() {
        this.outlinePanel = new ContentPanel();
        // this.outlinePanel.setStyleAttribute("background-color", "white");
        this.outlinePanel.setHeading("Step 2: Outline");
        this.outlinePanel.addListener(Events.Expand, new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                map.setOutlineEnabled(true);
                map.setDroppingEnabled(false);
            }
        });

        Text explanation = new Text("Click the map to draw an outline for your environment");
        this.outlinePanel.add(explanation, new FlowData(10));

        Button resetButton = new Button("Reset", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                map.resetOutline();
                map.setOutlineEnabled(true);
                map.setDroppingEnabled(false);
                isOutlineValid = false;
                checkValidity();
            }
        });
        resetButton.setMinWidth(75);
        this.outlinePanel.add(resetButton, new FlowData(0, 10, 10, 10));
    }

    private void initDevicesPanel() {
        this.devicesPanel = new ContentPanel();
        // this.sensorsPanel.setStyleAttribute("background-color", "white");
        this.devicesPanel.setHeading("Step 3: Sensors");

        this.devicesPanel.addListener(Events.Expand, new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                map.setOutlineEnabled(false);
                map.setDroppingEnabled(true);
            }
        });

        Text explanation = new Text("Click the map to add devices to your environment");
        this.devicesPanel.add(explanation, new FlowData(10));

        Button resetButton = new Button("Reset", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                map.resetSensors();
                map.setOutlineEnabled(false);
                map.setDroppingEnabled(true);
                checkValidity();
            }
        });
        resetButton.setMinWidth(75);
        this.devicesPanel.add(resetButton, new FlowData(0, 10, 10, 10));
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

        this.form.reset();
        this.map.resetOutline();
        this.map.resetSensors();
        this.map.setOutlineEnabled(false);
        this.map.setDroppingEnabled(false);
        this.submitButton.disable();

        ((AccordionLayout) this.west.getLayout()).setActiveItem(this.form);
    }

    private void showPanel() {
        resetPanel();

        this.window.show();
        this.window.center();
    }

    private void submit() {
        LOGGER.finest("Submit...");

        AppEvent create = new AppEvent(EnvCreateEvents.CreateRequest);
        create.setData("name", this.name.getValue());
        create.setData("floors", this.floors.getValue().intValue());
        create.setData("sensors", this.map.getSensors());
        create.setData("outline", this.map.getOutline());
        fireEvent(create);
    }
}
