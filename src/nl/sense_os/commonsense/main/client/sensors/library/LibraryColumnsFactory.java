package nl.sense_os.commonsense.main.client.sensors.library;

import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.util.SenseIconProvider;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import java.util.Arrays;
import java.util.List;

public class LibraryColumnsFactory {

    public static ColumnModel create() {

        ColumnConfig id = new ColumnConfig(GxtSensor.ID, "ID", 50);
        id.setHidden(true);

        ColumnConfig type = new ColumnConfig(GxtSensor.TYPE, "Type", 50);
        type.setRenderer(new GridCellRenderer<GxtSensor>() {

            @Override
            public Object render(GxtSensor model, String property, ColumnData config,
                    int rowIndex, int colIndex, ListStore<GxtSensor> store, Grid<GxtSensor> grid) {
                return new SenseIconProvider<GxtSensor>().getIcon(model).getHTML();
            }
        });

        ColumnConfig name = new ColumnConfig(GxtSensor.DISPLAY_NAME, "Name", 200);

        ColumnConfig physical = new ColumnConfig(GxtSensor.DESCRIPTION, "Description", 200);
        physical.setHidden(true);

        ColumnConfig devId = new ColumnConfig(GxtSensor.DEVICE_ID, "Device ID", 50);
        devId.setHidden(true);

        ColumnConfig device = new ColumnConfig(GxtSensor.DEVICE, "Device", 100);

        ColumnConfig devUuid = new ColumnConfig(GxtSensor.DEVICE_UUID, "Device UUID", 50);
        devUuid.setHidden(true);

        ColumnConfig dataType = new ColumnConfig(GxtSensor.DATA_TYPE, "Data type", 100);
        dataType.setHidden(true);

        ColumnConfig owner = new ColumnConfig(GxtSensor.OWNER_USERNAME, "Owner", 100);

        ColumnConfig environment = new ColumnConfig(GxtSensor.ENVIRONMENT_NAME, "Environment",
                100);

        List<ColumnConfig> columns = Arrays.asList(type, id, name, physical, devId, device,
                devUuid, dataType, environment, owner);

        return new ColumnModel(columns);
    }

    private LibraryColumnsFactory() {
        // private constructor to prevent instatiation
    }
}
