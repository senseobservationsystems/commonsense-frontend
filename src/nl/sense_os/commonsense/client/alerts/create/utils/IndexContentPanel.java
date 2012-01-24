package nl.sense_os.commonsense.client.alerts.create.utils;

import com.extjs.gxt.ui.client.widget.ContentPanel;

public class IndexContentPanel extends ContentPanel{
	private int index;
	
	public IndexContentPanel() {
		super();
		
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
}
