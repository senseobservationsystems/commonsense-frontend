/*
 * Ext GWT - Ext for GWT
 * Copyright(c) 2007, 2008, Ext JS, LLC.
 * licensing@extjs.com
 *
 * http://extjs.com/license
 */
package nl.sense_os.commonsense.main.client.viz.panels.table;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.model.Timeseries;
import nl.sense_os.commonsense.lib.client.communication.CommonSenseClient;
import nl.sense_os.commonsense.lib.client.communication.CommonSenseClient.Urls;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
import nl.sense_os.commonsense.main.client.viz.panels.VizPanel;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.store.ListStore;
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

public class SensorDataGrid extends VizPanel {

	private static final Logger LOG = Logger.getLogger("SensorDataGrid");

	public SensorDataGrid(final List<ExtSensor> sensors, long startTime, long endTime) {

		// grid panel parameters
		ModelType model = createModelType();
		final List<ColumnConfig> colConf = createColConfig(sensors);
		final String url = createUrl(sensors, startTime, endTime);
		final int pageSize = 25;

		// Grid.
		PaginationGridPanel gridPanel = new PaginationGridPanel(url, model, colConf, pageSize,
				startTime, endTime);

		// new Draggable(gridPanel); // disabled for now, nothing is draggable (yet)

		setLayout(new FitLayout());
		add(gridPanel);
	}

	private List<ColumnConfig> createColConfig(final List<ExtSensor> sensors) {
		List<ColumnConfig> colConf = new ArrayList<ColumnConfig>();

		ColumnConfig idCol = new ColumnConfig();
		idCol.setId("id");
		idCol.setHeaderText("row id");
		idCol.setDataIndex("id");
		idCol.setWidth(50);
		colConf.add(idCol);

		ColumnConfig sensorCol = new ColumnConfig();
		sensorCol.setId("sensor_id");
		sensorCol.setHeaderText("sensor");
		sensorCol.setDataIndex("sensor_id");
		sensorCol.setWidth(250);
		sensorCol.setRenderer(new GridCellRenderer<ModelData>() {

			@Override
			public Object render(ModelData model, String property, ColumnData config, int rowIndex,
					int colIndex, ListStore<ModelData> store, Grid<ModelData> grid) {
				int id = (int) model.<Double> get(property).doubleValue();
				for (ExtSensor sensor : sensors) {
					LOG.fine("id='" + id + "', sensor.id='" + sensor.getId() + "'");
					if (id == sensor.getId()) {
						String name = sensor.getName();
						String deviceType = sensor.getDescription();
						if (deviceType != null && deviceType.length() > 0
								|| name.equals(deviceType)) {
							return id + ". " + name;
						}
						return id + ". " + name + " (" + deviceType + ")";
					}
				}
				return "" + id;
			}
		});
		colConf.add(sensorCol);

		ColumnConfig valueCol = new ColumnConfig();
		valueCol.setId("value");
		valueCol.setHeaderText("value");
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
		timeCol.setHeaderText("date");
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

	private String createUrl(List<ExtSensor> sensors, long startTime, long endTime) {

		int id = sensors.get(0).getId();

		final UrlBuilder urlBuilder = new UrlBuilder().setProtocol(CommonSenseClient.Urls.PROTOCOL)
				.setHost(CommonSenseClient.Urls.HOST);
		urlBuilder.setPath(Urls.PATH_SENSORS + "/" + id + "/data.json");

		final int alias = sensors.get(0).getAlias();
		if (alias != -1) {
			urlBuilder.setParameter("alias", "" + alias);
		}

		urlBuilder.setParameter("start_date",
				NumberFormat.getFormat("#.000").format(startTime / 1000d));
		if (endTime != -1) {
			urlBuilder.setParameter("end_date",
					NumberFormat.getFormat("#.000").format(endTime / 1000d));
		}

		// urlBuilder.setParameter("total", "1");

		return urlBuilder.buildString();
	}

	@Override
	protected void onNewData(JsArray<Timeseries> data) {
		// nothing to do
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
}
