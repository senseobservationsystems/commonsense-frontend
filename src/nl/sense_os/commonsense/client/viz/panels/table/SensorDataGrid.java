/*
 * Ext GWT - Ext for GWT
 * Copyright(c) 2007, 2008, Ext JS, LLC.
 * licensing@extjs.com
 *
 * http://extjs.com/license
 */
package nl.sense_os.commonsense.client.viz.panels.table;

import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.models.SensorModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class SensorDataGrid extends LayoutContainer {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger("SensorDataGrid");

    public SensorDataGrid(final List<SensorModel> sensors) {

        // grid panel parameters
        ModelType model = createModelType();
        final List<ColumnConfig> colConf = createColConfig(sensors);
        final String url = createUrl(sensors);
        final int pageSize = 25;

        // Grid.
        PaginationGridPanel gridPanel = new PaginationGridPanel(url, model, colConf, pageSize);

        // new Draggable(gridPanel); // disabled for now, nothing is draggable (yet)

        setLayout(new FitLayout());
        add(gridPanel);
    }

    private List<ColumnConfig> createColConfig(final List<SensorModel> sensors) {
        List<ColumnConfig> colConf = new ArrayList<ColumnConfig>();

        ColumnConfig idCol = new ColumnConfig();
        idCol.setId("id");
        idCol.setHeader("row id");
        idCol.setDataIndex("id");
        idCol.setWidth(50);
        colConf.add(idCol);

        ColumnConfig sensorCol = new ColumnConfig();
        sensorCol.setId("sensor_id");
        sensorCol.setHeader("sensor id");
        sensorCol.setDataIndex("sensor_id");
        sensorCol.setWidth(250);
        sensorCol.setRenderer(new GridCellRenderer<ModelData>() {

            @Override
            public Object render(ModelData model, String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<ModelData> store, Grid<ModelData> grid) {
                String id = model.<String> get(property);
                for (SensorModel sensor : sensors) {
                    if (id.equals("" + sensor.getId())) {
                        String name = sensor.getName();
                        String deviceType = sensor.getPhysicalSensor();
                        if (name.equals(deviceType)) {
                            return name;
                        }
                        return name + " (" + deviceType + ")";
                    }
                }
                return id;
            }
        });
        colConf.add(sensorCol);

        ColumnConfig valueCol = new ColumnConfig();
        valueCol.setId("value");
        valueCol.setHeader("value");
        valueCol.setDataIndex("value");
        valueCol.setWidth(300);
        valueCol.setRenderer(new GridCellRenderer<ModelData>() {
            @Override
            public Object render(ModelData model, String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<ModelData> store, Grid<ModelData> grid) {

                String value = model.<String> get("value");

                // special rendering for json values
                if ((value.charAt(0) == '{') && (value.charAt(value.length() - 1) == '}')) {
                    JSONValue jsonValue = JSONParser.parseStrict(value);
                    JSONObject jsonObj = jsonValue.isObject();
                    if (null != jsonObj) {
                        return renderJsonValue(jsonObj);
                    }
                }

                // return the normal value for non-JSON input
                return value;
            }
        });
        colConf.add(valueCol);

        ColumnConfig timeCol = new ColumnConfig();
        timeCol.setId("date");
        timeCol.setHeader("date");
        timeCol.setDataIndex("date");
        timeCol.setWidth(150);
        timeCol.setRenderer(new GridCellRenderer<ModelData>() {
            @Override
            public String render(ModelData model, String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<ModelData> store, Grid<ModelData> grid) {
                double timeInSecs = Double.parseDouble(model.<String> get("date"));
                long timeInMSecs = (long) (1000 * timeInSecs);
                DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
                return format.format(new Date(timeInMSecs));
            }
        });
        colConf.add(timeCol);

        return colConf;
    }

    private ModelType createModelType() {
        ModelType model = new ModelType();
        model.setTotalName("total");
        model.setRoot("data");
        model.addField("id");
        model.addField("sensor_id");
        model.addField("value");
        model.addField("date");
        model.addField("week");
        model.addField("month");
        model.addField("year");
        return model;
    }

    private String createUrl(List<SensorModel> sensors) {

        int id = sensors.get(0).getId();
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        String result = Urls.SENSORS + "/" + id + "/data.json";
        result += "?session_id=" + sessionId;

        final int alias = sensors.get(0).getAlias();
        if (alias != -1) {
            result += "&alias=" + alias;
        }

        long timeRange = 1000l * 60 * 60 * 24 * 7 * 4; // 4 weeks
        result += "&start_date=" + Math.round((System.currentTimeMillis() - timeRange) / 1000);
        result += "&total=1";
        return result;
    }

    private String renderJsonValue(JSONObject json) {
        StringBuilder sb = new StringBuilder();
        for (String key : json.keySet()) {
            // first print the field label
            sb.append("<b>").append(key).append(":</b> ");

            // get the field value
            JSONValue value = json.get(key);
            JSONString jsonString = value.isString();
            String valueString = "";
            if (null != jsonString) {
                valueString = jsonString.stringValue();
            } else {
                valueString = value.toString();
            }

            sb.append(valueString).append("<br />");
        }
        return sb.toString();
    }
}
