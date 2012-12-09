package nl.sense_os.commonsense.main.client.environmentmanagement.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import nl.sense_os.commonsense.lib.client.model.apiclass.Device;
import nl.sense_os.commonsense.lib.client.model.apiclass.Environment;
import nl.sense_os.commonsense.lib.client.model.apiclass.Sensor;
import nl.sense_os.commonsense.main.client.environmentmanagement.EnvironmentDisplayView;
import nl.sense_os.commonsense.main.client.shared.util.MapTools;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
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

public class GxtEnvironmentMap extends ContentPanel implements EnvironmentDisplayView {

	private static final Logger LOG = Logger.getLogger(GxtEnvironmentMap.class.getName());
	private MapWidget map;
    private final Map<Marker, List<Device>> deviceMarkers = new HashMap<Marker, List<Device>>();

    private Environment environment;
    private List<Sensor> sensors;
    private List<String> positions;
	private Polygon outline;

	private MapClickHandler mapClickHandler;
	private PolygonClickHandler polygonClickHandler;

    private Presenter presenter;

    private ToolButton refresh;
    private ToolButton autoRefresh;

    /**
     * Adds tool buttons to the panel's heading.
     */
    protected void showToolButtons() {

        // regular refresh button
        refresh = new ToolButton("x-tool-refresh");
        refresh.setToolTip("refresh");

        // auto-refresh button
        autoRefresh = new ToolButton("x-tool-right");
        autoRefresh.setToolTip("start auto-refresh");

        // add buttons to the panel's header
        Header header = getHeader();
        header.addTool(autoRefresh);
        header.addTool(refresh);
    }

	public GxtEnvironmentMap() {
		this(null);
		// LOG.setLevel(Level.ALL);
	}

    public GxtEnvironmentMap(Environment environment) {

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

	private void drawMarkers() {
		LOG.fine("Draw markers...");
        // TODO
        /*
         * for (Sensor sensor : sensors) { if (sensor.getName().contains("position")) {
         * List<Timeseries> positionData = sensorValues.get(sensor.getId()); if (positionData !=
         * null) {
         * 
         * JsArray<DataPoint> lat = null; JsArray<DataPoint> lng = null; for (Timeseries ts :
         * positionData) { if (ts.getLabel().contains("latitude")) { lat = ts.getData(); } else if
         * (ts.getLabel().contains("longitude")) { lng = ts.getData(); } }
         * 
         * // draw the marker if (null != lat && null != lng) { LOG.fine("Marker for " +
         * sensor.getDevice() + "...");
         * 
         * FloatDataPoint lastLat = lat.get(lat.length() - 1).cast(); FloatDataPoint lastLng =
         * lng.get(lng.length() - 1).cast(); double latValue = lastLat.getValue(); double lngValue =
         * lastLng.getValue(); LatLng coordinate = LatLng.newInstance(latValue, lngValue);
         * 
         * // create marker options Icon icon = Icon.newInstance("/img/icons/16/sense_black.gif");
         * icon.setIconAnchor(Point.newInstance(8, 8)); MarkerOptions options =
         * MarkerOptions.newInstance(icon); options.setDraggable(true);
         * options.setTitle(sensor.getDevice().getType());
         * 
         * Marker marker = new Marker(coordinate, options); map.addOverlay(marker);
         * 
         * } else { LOG.warning("Cannot draw marker for " + sensor.getDevice() +
         * ": position data has not latitude or longitude!"); }
         * 
         * } else { LOG.fine("Cannot draw marker for " + sensor.getDevice() +
         * ": position data is null!"); } } }
         */

	}

    @Override
    public void addDevice(LatLng latLng, List<Device> devices) {
		LOG.finest("Add device marker...");

		// create title
		String title = "";
        for (Device device : devices) {
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
        map.addOverlay(m);

        deviceMarkers.put(m, devices);
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
    @Override
    public Map<Device, LatLng> getDevices() {
		// create list of deviceMarkers
        Map<Device, LatLng> result = new HashMap<Device, LatLng>();
        for (Entry<Marker, List<Device>> entry : this.deviceMarkers.entrySet()) {
			LatLng latLng = entry.getKey().getLatLng();
            for (Device d : entry.getValue()) {
                result.put(d, latLng);
            }
		}

		return result;
	}

	/**
	 * @return The outline.
	 */
    @Override
	public Polygon getOutline() {
		return this.outline;
	}

	private void initClickHandlers() {

		this.mapClickHandler = new MapClickHandler() {

			@Override
			public void onClick(MapClickEvent event) {
				LatLng latLng = event.getLatLng();
				if (null != latLng) {
                    if (null != presenter) {
                        presenter.onDeviceAddClick(latLng);
                    }
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
                    if (null != presenter) {
                        presenter.onDeviceAddClick(latLng);
                    }
				} else {
					LOG.warning("Clicked polygon, but LatLng=null");
				}

			}
		};
	}

	private void initDeviceChooser() {

	}

	private void initOutline() {

		this.outline = new Polygon(new LatLng[] {});
		this.map.addOverlay(this.outline);
		this.outline.addPolygonEndLineHandler(new PolygonEndLineHandler() {

			@Override
			public void onEnd(PolygonEndLineEvent event) {
				// logger.fine( "Outline complete");
                if (null != presenter) {
                    presenter.onOutlineComplete();
                }
			}
		});
	}

	@Override
	protected void onRender(Element parent, int index) {
		if (null != environment) {
            Polygon outline = MapTools.outlineToPolygon(environment.getRawOutline());
            setOutline(outline);
		}
		super.onRender(parent, index);
	}

    @Override
    public void setEnvironment(Environment environment) {
		this.environment = environment;

		if (null != environment) {
            if (null != environment.getRawOutline()) {
                LOG.fine("outline: " + environment.getRawOutline());
                Polygon outline = MapTools.outlineToPolygon(environment.getRawOutline());
                setOutline(outline);
			} else {
				LOG.warning("Environment has no outline!");
			}
		}
	}

    private void setOutline(Polygon outline) {
		this.map.removeOverlay(this.outline);
		this.outline = outline;
		this.map.addOverlay(this.outline);

		// zoom and center to the outline
		this.map.setCenter(this.outline.getBounds().getCenter());
		// this.map.setZoomLevel(this.map.getBoundsZoomLevel(this.outline.getBounds()));
		this.map.setZoomLevel(15);
		LOG.finest("Zoom level: " + this.map.getBoundsZoomLevel(this.outline.getBounds()));
	}

    public void setSensors(List<Sensor> sensors, List<String> positions) {
		LOG.finest("Set sensors...");

		this.sensors = sensors;
        this.positions = positions;

        drawMarkers();
	}

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
}
