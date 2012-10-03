package nl.sense_os.commonsense.main.client.alerts.create.triggers;

import java.util.ArrayList;

public class StringTrigger {
	private ArrayList<String> equalValues;
	private ArrayList<String> unequalValues;
	
	public void setEqualValues(ArrayList<String> equalValues) {
		this.equalValues = equalValues;
	}
	
	public ArrayList<String> getEqualValues() {
		return this.equalValues;
	}
	
	public void setUnequalValues(ArrayList<String> unequalValues) {
		this.unequalValues = unequalValues;
	}
	
	public ArrayList<String> getUnequalValues() {
		return this.unequalValues;
	}
}