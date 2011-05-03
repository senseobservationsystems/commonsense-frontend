package nl.sense_os.commonsense.client.environments.components;

import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class EnvForm extends FormPanel {

    private TextField<String> name;
    private SpinnerField floors;

    public EnvForm() {
        this.setHeaderVisible(false);

        this.name = new TextField<String>();
        this.name.setFieldLabel("Name");

        this.floors = new SpinnerField();
        this.floors.setPropertyEditorType(Integer.class);
        this.floors.setMinValue(1);
        this.floors.setFieldLabel("Number of floors");

        this.add(this.name, new FormData("-10"));
        this.add(this.floors, new FormData("-10"));
    }

}
