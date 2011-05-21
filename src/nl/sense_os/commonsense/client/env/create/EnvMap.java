package nl.sense_os.commonsense.client.env.create;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import nl.sense_os.commonsense.shared.models.SensorModel;

import com.extjs.gxt.ui.client.dnd.DND.Operation;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MapClickHandler;
import com.google.gwt.maps.client.event.PolygonEndLineHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.maps.client.overlay.PolyEditingOptions;
import com.google.gwt.maps.client.overlay.Polygon;

public class EnvMap extends LayoutContainer {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger("EnvMap");
    private MapWidget map;
    private Map<Marker, List<SensorModel>> sensors;
    private Polygon outline;

    private MapClickHandler mapClickHandler;

    public EnvMap() {
        // Create the map.
        this.map = new MapWidget();

        // Add some controls
        this.map.setUIToDefault();

        // put the map in a container so we can drop stuff on it
        this.setLayout(new FitLayout());
        this.add(this.map);

        initOutline();
        setupDragDrop();
    }

    public Polygon getOutline() {
        return this.outline;
    }

    public Map<Marker, List<SensorModel>> getSensors() {
        return this.sensors;
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

    private void onSensorsDropped(List<SensorModel> data, int x, int y) {
        // logger.fine( "onSensorsDropped");

        LatLng latLng = this.map.convertContainerPixelToLatLng(Point.newInstance(x, y));
        MarkerOptions options = MarkerOptions.newInstance();
        options.setDraggable(true);

        String title = "";
        for (SensorModel sensor : data) {
            title += sensor.getName() + "; ";
        }
        options.setTitle(title);

        Marker marker = new Marker(latLng, options);
        map.addOverlay(marker);
        sensors.put(marker, data); // TODO catch overlapping drops
    }

    public void resetOutline() {
        this.map.clearOverlays();
        initOutline();
    }

    public void resetSensors() {
        for (Marker marker : sensors.keySet()) {
            this.map.removeOverlay(marker);
        }
        this.sensors.clear();
    }

    public void setDroppingEnabled(boolean enabled) {
        if (enabled) {
            this.map.addMapClickHandler(mapClickHandler);
        } else {
            this.map.removeMapClickHandler(mapClickHandler);
        }
    }

    public void setOutline(Polygon outline) {
        this.outline = outline;
        this.map.clearOverlays();
        this.map.addOverlay(outline);
        layout();
    }

    public void setOutlineEnabled(boolean enabled) {

        if (enabled) {
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
            this.outline.setEditingEnabled(false);
        }
    }

    public void setSensors(Map<Marker, List<SensorModel>> sensors) {

        resetSensors();
        this.sensors = sensors;

        for (Marker marker : sensors.keySet()) {
            this.map.addOverlay(marker);
        }
    }

    private void setupDragDrop() {

        this.mapClickHandler = new MapClickHandler() {

            @Override
            public void onClick(MapClickEvent event) {
                LatLng latLng = event.getLatLng();
                if (null != latLng) {
                    Marker m = new Marker(latLng);
                    sensors.put(m, null);
                    map.addOverlay(m);
                } else {
                    Overlay clicked = event.getOverlay();
                    if (false == clicked.equals(outline)) {
                        map.removeOverlay(clicked);
                        sensors.remove(clicked);
                    }
                }

            }
        };

        this.sensors = new HashMap<Marker, List<SensorModel>>();

        final DropTarget dropTarget = new DropTarget(this);
        dropTarget.setOperation(Operation.MOVE);
        dropTarget.addDNDListener(new DNDListener() {

            @Override
            public void dragDrop(DNDEvent e) {
                final List<SensorModel> data = e.<ArrayList<SensorModel>> getData();
                int x = e.getClientX() - map.getAbsoluteLeft();
                int y = e.getClientY() - map.getAbsoluteTop();
                onSensorsDropped(data, x, y);
                // logger.fine( "Event: " + e.getClientX() + ", " + e.getClientY());
                // logger.fine( "Map: " + map.getAbsoluteLeft() + ", " + map.getAbsoluteTop());
            }
        });
        dropTarget.setGroup("env-creator");
    }
}
