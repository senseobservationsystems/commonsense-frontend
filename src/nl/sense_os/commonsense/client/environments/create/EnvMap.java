package nl.sense_os.commonsense.client.environments.create;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.dnd.DND.Operation;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MapClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.maps.client.overlay.PolyEditingOptions;
import com.google.gwt.maps.client.overlay.Polygon;

public class EnvMap extends LayoutContainer {

    private static final String TAG = "EnvMap";
    private MapWidget map;
    private HashMap<Marker, List<SensorModel>> markers;
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

    private void initOutline() {

        this.outline = new Polygon(new LatLng[]{});
        this.map.addOverlay(this.outline);

    }

    private void onSensorsDropped(List<SensorModel> data, int x, int y) {
        Log.d(TAG, "onSensorsDropped");

        LatLng latLng = this.map.convertDivPixelToLatLng(Point.newInstance(x, y));
        Marker marker = new Marker(latLng);
        map.addOverlay(marker);
        markers.put(marker, data); // TODO
    }

    public void resetOutline() {
        this.map.removeOverlay(this.outline);
        initOutline();
    }

    public void resetMarkers() {
        for (Marker marker : markers.keySet()) {
            this.map.removeOverlay(marker);
        }
        this.markers.clear();
    }

    public void setDroppingEnabled(boolean enabled) {
        if (enabled) {
            this.map.addMapClickHandler(mapClickHandler);
        } else {
            this.map.removeMapClickHandler(mapClickHandler);
        }
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

    private void setupDragDrop() {

        this.mapClickHandler = new MapClickHandler() {

            @Override
            public void onClick(MapClickEvent event) {
                LatLng latLng = event.getLatLng();
                if (null != latLng) {
                    Marker m = new Marker(latLng);
                    markers.put(m, null);
                    map.addOverlay(m);
                } else {
                    Overlay clicked = event.getOverlay();
                    if (false == clicked.equals(outline)) {
                        map.removeOverlay(clicked);
                        markers.remove(clicked);
                    }
                }

            }
        };

        this.markers = new HashMap<Marker, List<SensorModel>>();

        final DropTarget dropTarget = new DropTarget(this);
        dropTarget.setOperation(Operation.COPY);
        dropTarget.addDNDListener(new DNDListener() {

            @Override
            public void dragDrop(DNDEvent e) {
                final List<SensorModel> data = e.<ArrayList<SensorModel>> getData();
                int x = e.getClientX() - map.getAbsoluteLeft();
                int y = e.getClientY() - map.getAbsoluteTop();
                onSensorsDropped(data, x, y);
            }
        });
        dropTarget.setGroup("env-creator");
    }
}
