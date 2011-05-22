package nl.sense_os.commonsense.client.env.create;

import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.shared.models.SensorModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.i18n.client.NumberFormat;

public class EnvCreator extends View {

    private static final Logger LOGGER = Logger.getLogger(EnvCreator.class.getName());
    private Window window;

    private FormPanel form;
    private TextField<String> name;
    private SpinnerField floors;

    private EnvMap map;

    private GroupingStore<SensorModel> store;
    private Grid<SensorModel> grid;

    private ButtonBar buttons;
    private Button fwdButton;
    private Button cancelButton;

    public EnvCreator(Controller c) {
        super(c);
        LOGGER.setLevel(Level.ALL);
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

    private void onOutlineComplete() {
        this.map.setOutlineEnabled(false);
        this.map.setDroppingEnabled(true);
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

        Button resetButton = new Button("Reset", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                resetPanel();
            }
        });
        resetButton.setMinWidth(75);

        // forward button
        this.fwdButton = new Button("Submit", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                submit();
            }
        });
        this.fwdButton.setMinWidth(75);

        // only enable when form is valid
        FormButtonBinding binding = new FormButtonBinding(this.form);
        binding.addButton(fwdButton);

        // cancel button
        this.cancelButton = new Button("Cancel", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                hidePanel();
            }
        });
        this.cancelButton.setMinWidth(75);

        this.buttons = new ButtonBar();
        this.buttons.setAlignment(HorizontalAlignment.RIGHT);
        this.buttons.add(resetButton);
        this.buttons.add(this.fwdButton);
        this.buttons.add(this.cancelButton);
    }

    private void initForm() {

        this.form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBodyBorder(false);

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
    }

    @Override
    protected void initialize() {
        LOGGER.finest("Initialize...");

        this.window = new CenteredWindow();
        this.window.setHeading("Create new environment");
        this.window.setLayout(new BorderLayout());
        this.window.setMinWidth(720);
        this.window.setSize("85%", "600px");

        initForm();
        initMapPanel();
        initButtons();

        // do layout
        this.form.setBottomComponent(buttons);
        this.window.add(this.form, new BorderLayoutData(LayoutRegion.WEST, .33f, 275, 2000));
        this.window.add(this.map, new BorderLayoutData(LayoutRegion.CENTER));

        super.initialize();
    }

    private void initMapPanel() {
        this.map = new EnvMap();
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

    private void resetPanel() {
        LOGGER.finest("Reset panel...");

        this.form.reset();
        this.map.resetOutline();
        this.map.resetSensors();
        this.map.setOutlineEnabled(true);
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
