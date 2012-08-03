package nl.sense_os.commonsense.main.client.alerts.create.utils;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Polygon;



/**
Polygon for which you can set and get an index
*/

public class IndexPolygon extends Polygon {
	private int index;
	private double radius;
	private LatLng[] points;
	
	public IndexPolygon(LatLng[] points1, String color, int width, double opacity, String fillColor, double fillOpacity) {    	
		super(points1, color, width, opacity, fillColor, fillOpacity);
	}
	
	public IndexPolygon(LatLng[] points1) {
		super(points1);
	}
	
	public void setIndex (int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	public double getRadius() {
		return this.radius;
	}	    	
	
	public LatLng[] getPoints() {
		return this.points;
	}
	
	public void setPoints(LatLng[] points) {
		this.points = points;
	}
	
}
