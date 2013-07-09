package nl.sense_os.commonsense.main.client.env.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.model.DataPoint;
import nl.sense_os.commonsense.common.client.model.FloatDataPoint;
import nl.sense_os.commonsense.common.client.model.Timeseries;
import nl.sense_os.commonsense.main.client.env.create.EnvCreateEvents;
import nl.sense_os.commonsense.main.client.env.view.EnvViewEvents;
import nl.sense_os.commonsense.main.client.ext.model.ExtDevice;
import nl.sense_os.commonsense.main.client.ext.model.ExtEnvironment;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
import nl.sense_os.commonsense.main.client.viz.data.DataEvents;
import nl.sense_os.commonsense.main.client.viz.panels.VizPanel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MapClickHandler;
import com.google.gwt.maps.client.event.PolygonClickHandler;
import com.google.gwt.maps.client.event.PolygonEndLineHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.maps.client.overlay.PolyEditingOptions;
import com.google.gwt.maps.client.overlay.Polygon;
import com.google.gwt.user.client.Element;

public class EnvMap extends VizPanel {

	private static final Logger LOG = Logger.getLogger(EnvMap.class.getName());
	private MapWidget map;
	private final Map<Marker, List<ExtDevice>> deviceMarkers = new HashMap<Marker, List<ExtDevice>>();

	private ExtEnvironment environment;
	private List<ExtSensor> sensors;
	private Polygon outline;
	private final Window window = new Window();

	private MapClickHandler mapClickHandler;
	private PolygonClickHandler polygonClickHandler;
	private ListStore<ExtDevice> store;
	private HashMap<Integer, List<Timeseries>> sensorValues;

	public EnvMap() {
		this(null);
		// LOG.setLevel(Level.ALL);
	}

	public EnvMap(ExtEnvironment environment) {

		// Create the map.
		this.map = new MapWidget();

		// Add some controls
		this.map.setUIToDefault();

		// put the map in a container so we can drop stuff on it
		this.setLayout(new FitLayout());
		this.add(this.map);

		initOutline();
		initClickHandlers();
		initDeviceChooser();

		setEnvironment(environment);
	}

	@Override
	public void onNewData(JsArray<Timeseries> data) {
		LOG.finest("Got latest values...");

		// order the data by sensor ID
		this.sensorValues = new HashMap<Integer, List<Timeseries>>();
		for (int i = 0; i < data.length(); i++) {
			Timeseries ts = data.get(i);
			int id = ts.getId();

			List<Timeseries> timeseries = this.sensorValues.get(id);
			if (null == timeseries) {
				timeseries = new ArrayList<Timeseries>();
			}
			timeseries.add(ts);
			this.sensorValues.put(id, timeseries);
		}

		drawMarkers();
	}

	private void drawMarkers() {
		LOG.fine("Draw markers...");

		for (ExtSensor sensor : sensors) {
			if (sensor.getName().contains("position")) {
				List<Timeseries> positionData = sensorValues.get(sensor.getId());
				if (positionData != null) {

					JsArray<DataPoint> lat = null;
					JsArray<DataPoint> lng = null;
					for (Timeseries ts : positionData) {
						if (ts.getLabel().contains("latitude")) {
							lat = ts.getData();
						} else if (ts.getLabel().contains("longitude")) {
							lng = ts.getData();
						}
					}

					// draw the marker
					if (null != lat && null != lng) {
						LOG.fine("Marker for " + sensor.getDevice() + "...");

						FloatDataPoint lastLat = lat.get(lat.length() - 1).cast();
						FloatDataPoint lastLng = lng.get(lng.length() - 1).cast();
						double latValue = lastLat.getValue();
						double lngValue = lastLng.getValue();
						LatLng coordinate = LatLng.newInstance(latValue, lngValue);

						// create marker options
						Icon icon = Icon.newInstance("/img/icons/16/sense_black.gif");
						icon.setIconAnchor(Point.newInstance(8, 8));
						MarkerOptions options = MarkerOptions.newInstance(icon);
						options.setDraggable(true);
						options.setTitle(sensor.getDevice().getType());

						Marker marker = new Marker(coordinate, options);
						map.addOverlay(marker);

					} else {
						LOG.warning("Cannot draw marker for " + sensor.getDevice()
								+ ": position data has not latitude or longitude!");
					}

				} else {
					LOG.fine("Cannot draw marker for " + sensor.getDevice()
							+ ": position data is null!");
				}
			}
		}

	}

	private void addDeviceMarker(LatLng latLng, List<ExtDevice> devices) {
		LOG.finest("Add device marker...");

		// create title
		String title = "";
		for (ExtDevice device : devices) {
			String type = device.getType();
			if (type.equals("myrianode")) {
				title += device.getType() + " " + device.getUuid();
			} else {
				title += device.getType();
			}
			title += "; ";
		}
		if (title.length() > 0) {
			title = title.substring(0, title.length() - 2);
		}

		// create marker options
		Icon icon = Icon.newInstance("/img/icons/16/sense_black.gif");
		icon.setIconAnchor(Point.newInstance(8, 8));
		MarkerOptions options = MarkerOptions.newInstance(icon);
		options.setDraggable(true);
		options.setTitle(title);

		// create marker
		Marker m = new Marker(latLng, options);
		this.map.addOverlay(m);

		this.deviceMarkers.put(m, devices);
	}

	public void clearDevices() {
		LOG.finest("Clear devices...");

		for (Marker marker : deviceMarkers.keySet()) {
			this.map.removeOverlay(marker);
		}
		this.deviceMarkers.clear();
	}

	public void clearOutline() {
		LOG.finest("Clear outline...");

		this.map.removeOverlay(this.outline);
		initOutline();
	}

	public void editDevices(boolean enable) {
		if (enable) {
			LOG.finest("Devices enabled...");
			this.map.addMapClickHandler(mapClickHandler);
			this.outline.addPolygonClickHandler(this.polygonClickHandler);
		} else {
			LOG.finest("Devices disabled...");
			this.map.removeMapClickHandler(mapClickHandler);
			this.outline.removePolygonClickHandler(this.polygonClickHandler);
		}
	}

	public void editOutline(boolean enable) {

		if (enable) {
			LOG.finest("Outline enabled...");
			if (null == outline) {
				initOutline();
				this.outline.setDrawingEnabled(PolyEditingOptions.newInstance(128));
			}
			if (this.outline.getVertexCount() > 0) {
				this.outline.setDrawingEnabled();
			} else {
				this.outline.setDrawingEnabled(PolyEditingOptions.newInstance(128));
			}
		} else {
			LOG.finest("Outline disabled...");
			this.outline.setEditingEnabled(false);
		}
	}

	/**
	 * @return A list of deviceMarkers with their lat/lng from the map's markers, stored in the
	 *         "latlng" property.
	 */
	public List<ExtDevice> getDevices() {
		// create list of deviceMarkers
		List<ExtDevice> result = new ArrayList<ExtDevice>();
		for (Entry<Marker, List<ExtDevice>> entry : this.deviceMarkers.entrySet()) {
			LatLng latLng = entry.getKey().getLatLng();
			for (ExtDevice device : entry.getValue()) {
				device.set("latlng", latLng);
				result.add(device);
			}
		}

		return result;
	}

	/**
	 * @return The outline.
	 */
	public Polygon getOutline() {
		return this.outline;
	}

	private void initClickHandlers() {

		this.mapClickHandler = new MapClickHandler() {

			@Override
			public void onClick(MapClickEvent event) {
				LatLng latLng = event.getLatLng();
				if (null != latLng) {
					showDeviceChooser(latLng);
				} else {
					Overlay clicked = event.getOverlay();
					if (false == clicked.equals(outline)) {
						map.removeOverlay(clicked);
						deviceMarkers.remove(clicked);
					}
				}

			}
		};

		this.polygonClickHandler = new PolygonClickHandler() {

			@Override
			public void onClick(PolygonClickEvent event) {
				LatLng latLng = event.getLatLng();
				if (null != latLng) {
					showDeviceChooser(latLng);
				} else {
					LOG.warning("Clicked polygon, but LatLng=null");
				}

			}
		};
	}

	private void initDeviceChooser() {
		window.setLayout(new FitLayout());
		window.setSize(300, 300);
        window.setHeadingText("Select the devices for this position");

		store = new ListStore<ExtDevice>();

		CheckBoxSelectionModel<ExtDevice> sm = new CheckBoxSelectionModel<ExtDevice>();

		ColumnConfig check = sm.getColumn();
		ColumnConfig id = new ColumnConfig(ExtDevice.ID, "ID", 50);
		ColumnConfig type = new ColumnConfig(ExtDevice.TYPE, "Type", 100);
		ColumnConfig uuid = new ColumnConfig(ExtDevice.UUID, "UUID", 50);
		ColumnModel cm = new ColumnModel(Arrays.asList(check, id, type, uuid));

		final Grid<ExtDevice> grid = new Grid<ExtDevice>(store, cm);
		grid.setAutoExpandColumn(ExtDevice.TYPE);
		grid.setSelectionModel(sm);
		grid.addPlugin(sm);

		window.add(grid);

		Button ok = new Button("Ok", new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				window.hide();
				addDeviceMarker(window.<LatLng> getData("latlng"), grid.getSelectionModel()
						.getSelectedItems());
			}
		});

		Button cancel = new Button("Cancel", new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				window.hide();
			}
		});
		window.addButton(ok);
		window.addButton(cancel);
	}

	private void initOutline() {

		this.outline = new Polygon(new LatLng[] {});
		this.map.addOverlay(this.outline);
		this.outline.addPolygonEndLineHandler(new PolygonEndLineHandler() {

			@Override
			public void onEnd(PolygonEndLineEvent event) {
				// logger.fine( "Outline complete");
				Dispatcher.forwardEvent(EnvCreateEvents.OutlineComplete);
			}
		});
	}

	@Override
	protected void onRender(Element parent, int index) {
		if (null != environment) {
			setOutline(environment.getOutline());
		}
		super.onRender(parent, index);
	}

	private void setEnvironment(ExtEnvironment environment) {
		this.environment = environment;

		if (null != environment) {
			if (null != environment.getOutline()) {
				LOG.fine("outline: " + environment.getOutline());
				setOutline(environment.getOutline());
			} else {
				LOG.warning("Environment has no outline!");
			}

			// request the environment sensors
			AppEvent getSensors = new AppEvent(EnvViewEvents.RequestSensors);
			getSensors.setData("environment", environment);
			getSensors.setData("panel", this);
			Dispatcher.forwardEvent(getSensors);
		}
	}

	public void setOutline(Polygon outline) {
		this.map.removeOverlay(this.outline);
		this.outline = outline;
		this.map.addOverlay(this.outline);

		// zoom and center to the outline
		this.map.setCenter(this.outline.getBounds().getCenter());
		// this.map.setZoomLevel(this.map.getBoundsZoomLevel(this.outline.getBounds()));
		this.map.setZoomLevel(15);
		LOG.finest("Zoom level: " + this.map.getBoundsZoomLevel(this.outline.getBounds()));
	}

	public void setSensors(List<ExtSensor> sensors) {
		LOG.finest("Set sensors...");

		this.sensors = sensors;

		// request the environment sensors
		AppEvent getPositions = new AppEvent(DataEvents.LatestValuesRequest);
		getPositions.setData("sensors", sensors);
		getPositions.setData("vizPanel", this);
		Dispatcher.forwardEvent(getPositions);
	}

	private void showDeviceChooser(LatLng latLng) {
		LOG.finest("Show device chooser...");

		store.removeAll();

		// only display devices that are not added to the map yet
		List<ExtDevice> myDevices = Registry
				.<List<ExtDevice>> get(nl.sense_os.commonsense.common.client.util.Constants.REG_DEVICE_LIST);
		List<ExtDevice> selectable = new ArrayList<ExtDevice>();
		for (ExtDevice device : myDevices) {

			boolean isAlreadyInMap = false;
			for (Entry<Marker, List<ExtDevice>> entry : deviceMarkers.entrySet()) {
				for (ExtDevice mapDevice : entry.getValue()) {
					if (device.equals(mapDevice)) {
						isAlreadyInMap = true;
					}
				}
			}
			if (!isAlreadyInMap) {
				selectable.add(device);
			}
		}
		store.add(selectable);

		window.setData("latlng", latLng);
		window.show();
	}
}
