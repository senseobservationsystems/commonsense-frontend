package nl.sense_os.commonsense.client.environments.create;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.i18n.client.NumberFormat;

public class EnvForm extends FormPanel {

    protected static final String TAG = "EnvForm";
    private TextField<String> name;
    private SpinnerField floors;
    private Listener<FieldEvent> validListener = new Listener<FieldEvent>() {
        @Override
        public void handleEvent(FieldEvent be) {
            checkValidity();
        }
    };

    public EnvForm() {
        this.setHeaderVisible(false);

        this.name = new TextField<String>();
        this.name.setFieldLabel("Name");
        this.name.setAllowBlank(false);
        this.name.addListener(Events.Valid, validListener);
        this.name.addListener(Events.Invalid, validListener);

        this.floors = new SpinnerField();
        this.floors.setPropertyEditorType(Integer.class);
        this.floors.setAllowDecimals(false);
        this.floors.setFormat(NumberFormat.getFormat("#"));
        this.floors.setMinValue(1);
        this.floors.setOriginalValue(1);
        this.floors.setFieldLabel("Number of floors");
        // this.floors.addListener(Events.Valid, validListener);
        // this.floors.addListener(Events.Invalid, validListener);

        this.add(this.name, new FormData("-10"));
        this.add(this.floors, new FormData("-10"));

        reset();
    }

    private void checkValidity() {
        if (this.isValid()) {
            Dispatcher.forwardEvent(EnvCreateEvents.FormValid);
        } else {
            Dispatcher.forwardEvent(EnvCreateEvents.FormInvalid);
        }
    }

    public void resetForm() {
        this.name.reset();
        this.floors.reset();
    }

    public String getName() {
        return this.name.getValue();
    }

    public int getFloors() {
        return this.floors.getValue().intValue();
    }
}
