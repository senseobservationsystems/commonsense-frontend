package nl.sense_os.commonsense.main.client.states.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.component.CenteredWindow;
import nl.sense_os.commonsense.common.client.constant.Constants;
import nl.sense_os.commonsense.common.client.model.DeviceModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class StateDefaultsDialog extends CenteredWindow {

    private static final Logger LOG = Logger.getLogger(StateDefaultsDialog.class.getName());
    private ListStore<DeviceModel> store;
    private Grid<DeviceModel> grid;
    private FormPanel form;
    private Button submitButton;
    private Button cancelButton;
    private CheckBox overwrite;

    public StateDefaultsDialog() {
        super();

        setHeading("Create default states");
        setLayout(new FitLayout());
        setSize(600, 400);
        setClosable(false);

        initGrid();
        initForm();
        initButtons();

        add(form);
        addButton(submitButton);
        addButton(cancelButton);
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Grid<DeviceModel> getGrid() {
        return grid;
    }

    public Button getSubmitButton() {
        return submitButton;
    }

    private void initButtons() {

        grid.getSelectionModel().addSelectionChangedListener(
                new SelectionChangedListener<DeviceModel>() {

                    @Override
                    public void selectionChanged(SelectionChangedEvent<DeviceModel> se) {
                        // enable the submit button as soon as a device was selected
                        LOG.finest("Grid selection changed...");
                        submitButton.setEnabled(se.getSelection().size() > 0);
                    }
                });

        submitButton = new Button("Submit");
        submitButton.setIconStyle("sense-btn-icon-go");
        submitButton.setEnabled(false);
        submitButton.setMinWidth(75);

        cancelButton = new Button("Cancel");
        cancelButton.setMinWidth(75);
    }

    private void initForm() {
        form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBodyBorder(false);
        form.setScrollMode(Scroll.AUTOY);
        form.setLabelAlign(LabelAlign.TOP);

        LabelField label = new LabelField("CommonSense will generate default state sensors, "
                + "using the sensors in your library.<br><br>");
        label.setHideLabel(true);

        AdapterField gridField = new AdapterField(grid);
        gridField.setHeight(225);
        gridField.setResizeWidget(true);
        gridField.setFieldLabel("Select device(s) to use for the states");

        overwrite = new CheckBox();
        overwrite.setBoxLabel("Update existing state sensors (this will overwrite their settings)");
        overwrite.setHideLabel(true);
        overwrite.setValue(false);

        form.add(label, new FormData("-10"));
        form.add(gridField, new FormData("-10"));
        form.add(overwrite, new FormData("-10"));
    }

    private void initGrid() {
        store = new ListStore<DeviceModel>();
        store.add(Registry.<List<DeviceModel>> get(Constants.REG_DEVICE_LIST));

        ColumnConfig id = new ColumnConfig(DeviceModel.ID, "ID", 50);
        ColumnConfig type = new ColumnConfig(DeviceModel.TYPE, "Type", 150);
        ColumnConfig uuid = new ColumnConfig(DeviceModel.UUID, "UUID", 50);
        ColumnModel cm = new ColumnModel(Arrays.asList(id, type, uuid));

        grid = new Grid<DeviceModel>(store, cm);
        grid.setAutoExpandColumn(DeviceModel.UUID);
        grid.getSelectionModel().setSelectionMode(SelectionMode.MULTI);
    }

    public boolean isOverwrite() {
        return overwrite.getValue();
    }

    public void setBusy(boolean busy) {
        if (busy) {
            submitButton.setIconStyle("sense-btn-icon-loading");
        } else {
            submitButton.setIconStyle("sense-btn-icon-go");
        }
    }
}
