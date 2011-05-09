package nl.sense_os.commonsense.client.env.create;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.i18n.client.NumberFormat;

public class EnvCreatorForm extends FormPanel {

    @SuppressWarnings("unused")
    private static final String TAG = "EnvCreatorForm";
    private TextField<String> name;
    private SpinnerField floors;
    private ButtonBar buttons;
    private Button fwdButton;
    private Button backButton;
    private Button cancelButton;

    public EnvCreatorForm() {
        this.setHeaderVisible(false);

        initFields();

        this.add(this.name, new FormData("-10"));
        this.add(this.floors, new FormData("-10"));

        initButtons();
        this.setBottomComponent(this.buttons);

        reset();
    }

    /**
     * @return The number of floors for the environment.
     */
    public int getFloors() {
        return this.floors.getValue().intValue();
    }

    /**
     * @return The name for the environment.
     */
    public String getName() {
        return this.name.getValue();
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
        FormButtonBinding binding = new FormButtonBinding(this);
        binding.addButton(fwdButton);

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

    /**
     * Initializes the form fields.
     */
    private void initFields() {
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

    /**
     * @param value
     *            The number of floors for the environment.
     */
    public void setFloors(int value) {
        this.floors.setValue(value);
    }

    /**
     * @param value
     *            The name for the environment.
     */
    public void setName(String value) {
        this.name.setValue(value);
    }
}
