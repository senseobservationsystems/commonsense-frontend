package nl.sense_os.commonsense.client.env.create;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.models.DeviceModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
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

public class EnvMap extends LayoutContainer {

    private static final Logger LOGGER = Logger.getLogger(EnvMap.class.getName());
    private MapWidget map;
    private final Map<Marker, List<DeviceModel>> deviceMarkers = new HashMap<Marker, List<DeviceModel>>();
    private Polygon outline;

    private MapClickHandler mapClickHandler;
    private PolygonClickHandler polygonClickHandler;

    public EnvMap() {
        // Create the map.
        this.map = new MapWidget();

        // Add some controls
        this.map.setUIToDefault();

        // put the map in a container so we can drop stuff on it
        this.setLayout(new FitLayout());
        this.add(this.map);

        initOutline();
        initClickHandler();
    }

    private void addDeviceMarker(LatLng latLng, List<DeviceModel> devices) {

        // create title
        String title = "";
        for (DeviceModel device : devices) {
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
        for (Marker marker : deviceMarkers.keySet()) {
            this.map.removeOverlay(marker);
        }
        this.deviceMarkers.clear();
    }

    /**
     * @return A list of deviceMarkers with their lat/lng from the map's markers, stored in the
     *         "latlng" property.
     */
    public List<DeviceModel> getDevices() {
        // create list of deviceMarkers
        List<DeviceModel> result = new ArrayList<DeviceModel>();
        for (Entry<Marker, List<DeviceModel>> entry : this.deviceMarkers.entrySet()) {
            LatLng latLng = entry.getKey().getLatLng();
            for (DeviceModel device : entry.getValue()) {
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

    private void initClickHandler() {

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
                    LOGGER.warning("Clicked polygon, but LatLng=null");
                }

            }
        };

        // final DropTarget dropTarget = new DropTarget(this);
        // dropTarget.setOperation(Operation.MOVE);
        // dropTarget.addDNDListener(new DNDListener() {
        //
        // @Override
        // public void dragDrop(DNDEvent e) {
        // final List<SensorModel> data = e.<ArrayList<SensorModel>> getData();
        // int x = e.getClientX() - map.getAbsoluteLeft();
        // int y = e.getClientY() - map.getAbsoluteTop();
        // onSensorsDropped(data, x, y);
        // // logger.fine( "Event: " + e.getClientX() + ", " + e.getClientY());
        // // logger.fine( "Map: " + map.getAbsoluteLeft() + ", " + map.getAbsoluteTop());
        // }
        // });
        // dropTarget.setGroup("env-creator");
    }

    private void initOutline() {

        this.outline = new Polygon(new LatLng[]{});
        this.map.addOverlay(this.outline);
        this.outline.addPolygonEndLineHandler(new PolygonEndLineHandler() {

            @Override
            public void onEnd(PolygonEndLineEvent event) {
                // logger.fine( "Outline complete");
                Dispatcher.forwardEvent(EnvCreateEvents.OutlineComplete);
            }
        });

    }

    public void resetOutline() {
        this.map.removeOverlay(this.outline);
        initOutline();
    }

    public void setDevicesEnabled(boolean enabled) {
        if (enabled) {
            LOGGER.finest("Devices enabled...");
            this.map.addMapClickHandler(mapClickHandler);
            this.outline.addPolygonClickHandler(this.polygonClickHandler);
        } else {
            LOGGER.finest("Devices disabled...");
            this.map.removeMapClickHandler(mapClickHandler);
            this.outline.removePolygonClickHandler(this.polygonClickHandler);
        }
    }

    public void setOutlineEnabled(boolean enabled) {

        if (enabled) {
            LOGGER.finest("Outline enabled...");
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
            LOGGER.finest("Outline disabled...");
            this.outline.setEditingEnabled(false);
        }
    }

    private void showDeviceChooser(final LatLng latLng) {
        final Window window = new Window();
        window.setLayout(new FitLayout());
        window.setSize(300, 300);
        window.setHeading("Select the devices for this position");

        ListStore<DeviceModel> store = new ListStore<DeviceModel>();

        // only display devices that are not added to the map yet
        List<DeviceModel> myDevices = Registry.<List<DeviceModel>> get(Constants.REG_DEVICE_LIST);
        List<DeviceModel> selectable = new ArrayList<DeviceModel>();
        for (DeviceModel device : myDevices) {

            boolean isAlreadyInMap = false;
            for (Entry<Marker, List<DeviceModel>> entry : deviceMarkers.entrySet()) {
                for (DeviceModel mapDevice : entry.getValue()) {
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

        CheckBoxSelectionModel<DeviceModel> sm = new CheckBoxSelectionModel<DeviceModel>();

        ColumnConfig check = sm.getColumn();
        ColumnConfig id = new ColumnConfig(DeviceModel.ID, "ID", 50);
        ColumnConfig type = new ColumnConfig(DeviceModel.TYPE, "Type", 100);
        ColumnConfig uuid = new ColumnConfig(DeviceModel.UUID, "UUID", 50);
        ColumnModel cm = new ColumnModel(Arrays.asList(check, id, type, uuid));

        final Grid<DeviceModel> grid = new Grid<DeviceModel>(store, cm);
        grid.setAutoExpandColumn(DeviceModel.TYPE);
        grid.setSelectionModel(sm);
        grid.addPlugin(sm);

        window.add(grid);

        Button ok = new Button("Ok", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                window.hide();
                addDeviceMarker(latLng, grid.getSelectionModel().getSelectedItems());
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
        window.show();
    }
}
