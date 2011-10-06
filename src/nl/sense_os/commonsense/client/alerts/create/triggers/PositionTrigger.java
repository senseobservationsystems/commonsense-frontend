package nl.sense_os.commonsense.client.alerts.create.triggers;

import java.util.ArrayList;

import nl.sense_os.commonsense.client.alerts.create.utils.IndexPolygon;

public class PositionTrigger {
	private boolean insideMode;
	private ArrayList<IndexPolygon> circleList;
	
	public void setCircleList(ArrayList<IndexPolygon> circleList) {
		this.circleList = circleList;
	}
	
	public ArrayList<IndexPolygon> getCircleList() {
		return this.circleList;
	}
	
	public void setInsideMode(boolean insideMode) {
		this.insideMode = insideMode;
	}
	
	public boolean getInsideMode() {
		return this.insideMode;
	}
	
}