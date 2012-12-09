package nl.sense_os.commonsense.main.client.environmentmanagement.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.sense_os.commonsense.lib.client.model.apiclass.Device;
import nl.sense_os.commonsense.main.client.environmentmanagement.DeviceChooserView;
import nl.sense_os.commonsense.main.client.gxt.component.CenteredWindow;
import nl.sense_os.commonsense.main.client.gxt.model.GxtDevice;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class GxtDeviceChooser extends CenteredWindow implements DeviceChooserView {

    private ListStore<GxtDevice> store;
    private Presenter presenter;

    public GxtDeviceChooser() {
        setLayout(new FitLayout());
        setSize(300, 300);
        setHeading("Select the devices for this position");

        store = new ListStore<GxtDevice>();

        CheckBoxSelectionModel<GxtDevice> sm = new CheckBoxSelectionModel<GxtDevice>();

        ColumnConfig check = sm.getColumn();
        ColumnConfig id = new ColumnConfig(GxtDevice.ID, "ID", 50);
        ColumnConfig type = new ColumnConfig(GxtDevice.TYPE, "Type", 100);
        ColumnConfig uuid = new ColumnConfig(GxtDevice.UUID, "UUID", 50);
        ColumnModel cm = new ColumnModel(Arrays.asList(check, id, type, uuid));

        final Grid<GxtDevice> grid = new Grid<GxtDevice>(store, cm);
        grid.setAutoExpandColumn(GxtDevice.TYPE);
        grid.setSelectionModel(sm);
        grid.addPlugin(sm);

        add(grid);

        Button ok = new Button("Ok", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (null != presenter) {
                    presenter.onSubmitClick();
                }
            }
        });

        Button cancel = new Button("Cancel", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (null != presenter) {
                    presenter.onCancelClick();
                }
            }
        });
        addButton(ok);
        addButton(cancel);
    }

    @Override
    public void setDevices(List<Device> devices) {
        store.removeAll();

        List<GxtDevice> gxtDevices = new ArrayList<GxtDevice>();
        for (Device device : devices) {
            gxtDevices.add(new GxtDevice(device));
        }

        store.add(gxtDevices);
    }

    @Override
    public List<Device> getDevices() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
}
