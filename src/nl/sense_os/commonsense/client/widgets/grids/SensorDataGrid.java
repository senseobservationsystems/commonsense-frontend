/*
 * Ext GWT - Ext for GWT
 * Copyright(c) 2007, 2008, Ext JS, LLC.
 * licensing@extjs.com
 *
 * http://extjs.com/license
 */
package nl.sense_os.commonsense.client.widgets.grids;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.i18n.client.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.sense_os.commonsense.dto.TagModel;
import nl.sense_os.commonsense.dto.UserModel;

public class SensorDataGrid extends LayoutContainer {

    private static final String BASE_URL = "http://data.sense-os.nl/commonsense/gae/get_sensor_data_paged.php";

    public SensorDataGrid(final TagModel[] tags, UserModel user) {

        // Data store structure.
        ModelType model = new ModelType();
        model.setTotalName("total");
        model.setRoot("data");
        model.addField("t");
        model.addField("d");
        model.addField("s");
        model.addField("v");

        // Column model
        List<ColumnConfig> colConf = new ArrayList<ColumnConfig>();

        ColumnConfig sensorCol = new ColumnConfig();
        sensorCol.setId("s");
        sensorCol.setHeader("sensor_id");
        sensorCol.setDataIndex("s");
        sensorCol.setWidth(250);
        sensorCol.setRenderer(new GridCellRenderer<ModelData>() {
            @Override
            public String render(ModelData model, String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<ModelData> store, Grid<ModelData> grid) {
                int modelDevice = ((Double) model.get("d")).intValue();
                int modelSensor = ((Double) model.get("s")).intValue();
                for (TagModel tag : tags) {
                    if (tag.getParentId() == modelDevice && tag.getTaggedId() == modelSensor) {
                        return tag.get("text");
                    }
                }
                return "" + modelSensor;
            }
        });
        colConf.add(sensorCol);

        ColumnConfig valueCol = new ColumnConfig();
        valueCol.setId("v");
        valueCol.setHeader("value");
        valueCol.setDataIndex("v");
        valueCol.setWidth(300);
        colConf.add(valueCol);
        
        ColumnConfig timeCol = new ColumnConfig();
        timeCol.setId("t");
        timeCol.setHeader("time");
        timeCol.setDataIndex("t");
        timeCol.setWidth(250);
        timeCol.setRenderer(new GridCellRenderer<ModelData>() {
            @Override
            public String render(ModelData model, String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<ModelData> store, Grid<ModelData> grid) {
                Double timeInSecs = ((Double) model.get("t"));
                long timeInMSecs = (long) (1000 * timeInSecs);
                DateTimeFormat format = DateTimeFormat.getMediumDateTimeFormat();
                return format.format(new Date(timeInMSecs));
            }
        });
        colConf.add(timeCol);

        // Grid.
        PaginationGridPanel gridPanel = new PaginationGridPanel(createUrl(tags, user), model,
                colConf, 25);

        // Grid config.
        gridPanel.setWidth("100%");
        gridPanel.setAutoHeight(true);
        gridPanel.setTitle("sensor data");
        gridPanel.setCollapsible(true);
        gridPanel.setBodyBorder(true);

        // new Draggable(gridPanel); // disabled for now, nothing is draggable (yet)
        
        add(gridPanel);
    }

    private String createUrl(TagModel[] tags, UserModel user) {
        String result = BASE_URL + "?email=" + user.getName() + "&password=" + user.getPassword();
        for (TagModel tag : tags) {
            result += "&d_id[]=" + tag.getParentId() + "&s_id[]=" + tag.getTaggedId();
        }
        return result;
    }
}
