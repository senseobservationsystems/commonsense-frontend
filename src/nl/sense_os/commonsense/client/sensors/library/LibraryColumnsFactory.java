package nl.sense_os.commonsense.client.sensors.library;

import java.util.Arrays;
import java.util.List;

import nl.sense_os.commonsense.client.utility.SensorIconProvider;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

public class LibraryColumnsFactory {

    private static List<ColumnConfig> columns;

    private LibraryColumnsFactory() {
        // private constructor to prevent instatiation
    }

    public static ColumnModel create() {

        if (null == columns) {

            ColumnConfig id = new ColumnConfig(SensorModel.ID, "ID", 50);
            id.setHidden(true);

            ColumnConfig type = new ColumnConfig(SensorModel.TYPE, "Type", 50);
            type.setRenderer(new GridCellRenderer<SensorModel>() {

                @Override
                public Object render(SensorModel model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<SensorModel> store,
                        Grid<SensorModel> grid) {
                    SensorIconProvider<SensorModel> provider = new SensorIconProvider<SensorModel>();
                    provider.getIcon(model).getHTML();
                    return provider.getIcon(model).getHTML();
                }
            });

            ColumnConfig name = new ColumnConfig(SensorModel.NAME, "Name", 200);

            ColumnConfig devType = new ColumnConfig(SensorModel.DEVICE_TYPE, "Physical sensor", 200);
            devType.setRenderer(new GridCellRenderer<SensorModel>() {

                @Override
                public Object render(SensorModel model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<SensorModel> store,
                        Grid<SensorModel> grid) {
                    if (model.getType().equals("1")
                            && !model.getDeviceType().equals(model.getName())) {
                        return model.getDeviceType();
                    } else {
                        return "";
                    }
                }
            });
            devType.setHidden(true);

            ColumnConfig devId = new ColumnConfig("dev_uuid", "Device ID", 50);
            devId.setRenderer(new GridCellRenderer<SensorModel>() {

                @Override
                public Object render(SensorModel model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<SensorModel> store,
                        Grid<SensorModel> grid) {
                    if (model.getDevice() != null) {
                        return model.getDevice().getId();
                    } else {
                        return "";
                    }
                }
            });
            devId.setHidden(true);

            ColumnConfig device = new ColumnConfig(SensorModel.DEVICE, "Device", 150);
            device.setRenderer(new GridCellRenderer<SensorModel>() {

                @Override
                public Object render(SensorModel model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<SensorModel> store,
                        Grid<SensorModel> grid) {
                    if (model.getDevice() != null) {
                        return model.getDevice().toString();
                    } else {
                        return "";
                    }
                }
            });

            ColumnConfig dataType = new ColumnConfig(SensorModel.DATA_TYPE, "Data type", 100);
            dataType.setHidden(true);

            ColumnConfig owner = new ColumnConfig(SensorModel.OWNER, "Owner", 100);
            owner.setRenderer(new GridCellRenderer<SensorModel>() {

                @Override
                public Object render(SensorModel model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<SensorModel> store,
                        Grid<SensorModel> grid) {
                    if (model.getOwner() != null) {
                        return model.getOwner().toString();
                    } else {
                        return Registry.get(Constants.REG_USER);
                    }
                }
            });

            ColumnConfig environment = new ColumnConfig(SensorModel.ENVIRONMENT, "Environment", 150);
            environment.setRenderer(new GridCellRenderer<SensorModel>() {

                @Override
                public Object render(SensorModel model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<SensorModel> store,
                        Grid<SensorModel> grid) {
                    if (model.getEnvironment() != null) {
                        return model.getEnvironment().toString();
                    } else {
                        return "";
                    }
                }
            });

            columns = Arrays.asList(type, id, name, devType, devId, device, dataType, environment,
                    owner);
        }

        return new ColumnModel(columns);
    }
}
