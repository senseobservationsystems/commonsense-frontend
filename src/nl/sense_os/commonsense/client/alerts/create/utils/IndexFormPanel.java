package nl.sense_os.commonsense.client.alerts.create.utils;

import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.Button;

public class IndexFormPanel extends FormPanel{
	private int index;
	private Button deleteButton;
	private ComboBox combo;
	private TextField address;
	private TextArea description;
	
	public IndexFormPanel() {
		super();		
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public void setButton(Button b) {
		this.deleteButton = b;
	}
	
	public void showDeleteButton(boolean visible) {
		this.deleteButton.setVisible(visible); 
	}
	
	public void setCombo(ComboBox combo1) {
		this.combo = combo1;
	}
	
	public void setAddress(TextField text) {
		this.address = text;
	}
	
	public void setDescription(TextArea text) {
		this.description = text;
	}
	
	public ComboBox getCombo() {
		return combo;
	}
	
	public TextField getTextField() {
		return this.address;
	}
	
	public TextArea getTextArea() {
		return this.description;
	}
	
	public String getType() {
		StringSensorValue comboValue = (StringSensorValue) combo.getValue();
		String name = null;
		if (comboValue != null) {
			name = comboValue.getName();
		}
		return name;
	}
	
	public String getAddress() {
		String address1 = null;
		if (address.getValue()!= null) {
		address1 = address.getValue().toString();
		}
		return address1; 
	}
	
	public String getDescription() {
		String description1 = null;
		if (description.getValue()!= null){
		description1 = description.getValue().toString();
		}
		return description1; 
	}
}
