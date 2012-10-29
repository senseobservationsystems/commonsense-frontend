package nl.sense_os.commonsense.main.client.visualization.component.table;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.visualization.VisualizationView;
import nl.sense_os.commonsense.shared.client.communication.CommonSenseApi.Urls;
import nl.sense_os.commonsense.shared.client.model.Timeseries;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.Widget;

public class TableVisualization extends Composite implements VisualizationView {

    private static final Logger LOG = Logger.getLogger(TableVisualization.class.getName());

	public TableVisualization(List<GxtSensor> sensors, long start, long end, boolean subsample) {

        ContentPanel panel = new ContentPanel(new FitLayout());
        panel.setHeading("Table: " + createTitle(sensors));

        // grid panel parameters
        ModelType model = createModelType();
        final List<ColumnConfig> colConf = createColConfig(sensors);
        final String url = createUrl(sensors, start, end);
        final int pageSize = 25;

        // Grid
        PaginationGridPanel gridPanel = new PaginationGridPanel(url, model, colConf, pageSize,
                start, end);
        panel.add(gridPanel);

        initComponent(panel);
	}

    private List<ColumnConfig> createColConfig(final List<GxtSensor> sensors) {
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
                String id = Long.toString(Math.round(model.<Double> get(property)));
                for (GxtSensor sensor : sensors) {
                    if (id.equals("" + sensor.getId())) {
                        String name = sensor.getName();
                        String deviceType = sensor.getDescription();
                        if (name.equals(deviceType)) {
                            return id + ". " + name;
                        }
                        return id + ". " + name + " (" + deviceType + ")";
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
                double timeInSecs = -1;
                try {
                    timeInSecs = Double.parseDouble(model.<String> get("date"));
                } catch (ClassCastException e) {
                    timeInSecs = model.get("date");
                }
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

    private String createTitle(List<GxtSensor> sensors) {
        String title = "";
        for (GxtSensor sensor : sensors) {
            title += sensor.getDisplayName() + ", ";
        }

        // remove trailing ", "
        title = title.substring(0, title.length() - 2);

        return title;
    }

    private String createUrl(List<GxtSensor> sensors, long startTime, long endTime) {

        String id = sensors.get(0).getId();

        final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
        urlBuilder.setPath(Urls.PATH_SENSOR_DATA.replace("%1", id));

        urlBuilder.setParameter("start_date",
                NumberFormat.getFormat("#.000").format(startTime / 1000d));
        if (endTime != -1) {
            urlBuilder.setParameter("end_date",
                    NumberFormat.getFormat("#.000").format(endTime / 1000d));
        }

        return urlBuilder.buildString();
    }

    @Override
    public void onShow(Widget parent) {
        if (parent instanceof LayoutContainer) {
            ((LayoutContainer) parent).layout();
        }
    }

	private String renderJsonValue(JSONObject json) {
        LOG.finest("Render JSON value: " + json.toString());

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

	@Override
	public void setPresenter(Presenter presenter) {
		// not used
	}

    @Override
	public void visualize(JsArray<Timeseries> data) {
        // not used
	}
}