package nl.sense_os.commonsense.main.client.alerts.create.triggers;

import java.util.ArrayList;

public class NumericTrigger {
	private String type;
	private ArrayList<Double> values;
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}
	
	public void setValues(ArrayList<Double> values) {
		this.values = values;
	}
	
	public ArrayList<Double> getValues() {
		return this.values;
	}
	
}