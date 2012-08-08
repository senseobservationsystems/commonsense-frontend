package nl.sense_os.commonsense.main.client.sensors.library;

import nl.sense_os.commonsense.common.client.model.ExtSensor;
import nl.sense_os.commonsense.common.client.util.SenseIconProvider;

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

        ColumnConfig id = new ColumnConfig(ExtSensor.ID, "ID", 50);
        id.setHidden(true);

        ColumnConfig type = new ColumnConfig(ExtSensor.TYPE, "Type", 50);
        type.setRenderer(new GridCellRenderer<ExtSensor>() {

            @Override
            public Object render(ExtSensor model, String property, ColumnData config,
                    int rowIndex, int colIndex, ListStore<ExtSensor> store, Grid<ExtSensor> grid) {
                return new SenseIconProvider<ExtSensor>().getIcon(model).getHTML();
            }
        });

        ColumnConfig name = new ColumnConfig(ExtSensor.DISPLAY_NAME, "Name", 200);

        ColumnConfig physical = new ColumnConfig(ExtSensor.DESCRIPTION, "Description", 200);
        physical.setHidden(true);

        ColumnConfig devId = new ColumnConfig(ExtSensor.DEVICE_ID, "Device ID", 50);
        devId.setHidden(true);

        ColumnConfig device = new ColumnConfig(ExtSensor.DEVICE, "Device", 100);

        ColumnConfig devUuid = new ColumnConfig(ExtSensor.DEVICE_UUID, "Device UUID", 50);
        devUuid.setHidden(true);

        ColumnConfig dataType = new ColumnConfig(ExtSensor.DATA_TYPE, "Data type", 100);
        dataType.setHidden(true);

        ColumnConfig owner = new ColumnConfig(ExtSensor.OWNER_USERNAME, "Owner", 100);

        ColumnConfig environment = new ColumnConfig(ExtSensor.ENVIRONMENT_NAME, "Environment",
                100);

        List<ColumnConfig> columns = Arrays.asList(type, id, name, physical, devId, device,
                devUuid, dataType, environment, owner);

        return new ColumnModel(columns);
    }

    private LibraryColumnsFactory() {
        // private constructor to prevent instatiation
    }
}
