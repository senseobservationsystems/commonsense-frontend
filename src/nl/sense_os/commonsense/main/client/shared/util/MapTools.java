package nl.sense_os.commonsense.main.client.shared.util;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Polygon;

public class MapTools {

    public static Polygon outlineToPolygon(String outline) {
        if (null != outline && outline.length() > 0) {
            String[] values = outline.split(";");
            LatLng[] points = new LatLng[values.length];
            for (int i = 0; i < values.length; i++) {
                points[i] = LatLng.fromUrlValue(values[i]);
            }
            return new Polygon(points);
        } else {
            return null;
        }
    }

    public static LatLng positionToLatLng(String position) {
        if (null != position && position.length() > 0) {
            return LatLng.fromUrlValue(position);
        } else {
            return null;
        }
    }

    private MapTools() {
        // do not instantiate
    }
}
