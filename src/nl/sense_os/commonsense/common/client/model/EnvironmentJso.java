package nl.sense_os.commonsense.common.client.model;

import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Polygon;

/**
 * Overlay for environment objects that are sent from CommonSense back end using JSON.
 */
public class EnvironmentJso extends JavaScriptObject {

    protected EnvironmentJso() {
        // empty protected constructor
    }

    public final native int getId() /*-{
		return parseInt(this.id);
    }-*/;

    public final native String getName() /*-{
		return this.name;
    }-*/;

    public final native int getFloors() /*-{
		if (undefined == this.floors) {
			return -1;
		} else {
			return parseInt(this.floors);
		}
    }-*/;

    public final native String getRawOutline() /*-{
		if (undefined != this.gps_outline) {
			return this.gps_outline;
		} else {
			return '';
		}
    }-*/;

    public final Polygon getOutline() {
        String rawOutline = getRawOutline();
        if (null != rawOutline && rawOutline.length() > 0) {
            String[] values = rawOutline.split(";");
            LatLng[] points = new LatLng[values.length];
            for (int i = 0; i < values.length; i++) {
                points[i] = LatLng.fromUrlValue(values[i]);
            }
            return new Polygon(points);
        } else {
            return null;
        }
    }

    public final LatLng getPosition() {
        String rawPosition = getRawPosition();
        if (null != rawPosition && rawPosition.length() > 0) {
            return LatLng.fromUrlValue(getRawPosition());
        } else {
            return null;
        }
    }

    public final native String getRawPosition() /*-{
		if (undefined != this.position) {
			return this.position;
		} else {
			return '';
		}
    }-*/;

    private final native double getRawDate() /*-{
		if (undefined != this.date) {
			return this.date;
		} else {
			return -1;
		}
    }-*/;

    public final Date getDate() {
        long date = Math.round(getRawDate() * 1000);
        return new Date(date);
    }
}
