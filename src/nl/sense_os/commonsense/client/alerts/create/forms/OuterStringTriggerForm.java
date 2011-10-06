package nl.sense_os.commonsense.client.alerts.create.forms;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.alerts.create.AlertCreator;
import nl.sense_os.commonsense.client.alerts.create.triggers.StringTrigger;
import nl.sense_os.commonsense.client.common.models.SensorModel;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;

public class OuterStringTriggerForm extends FormPanel{
	
	private StringTriggerForm stringForm;
	private Logger LOG = Logger.getLogger(AlertCreator.class.getName());
	private ComboBox controlBox;
	
	public OuterStringTriggerForm (List<SensorModel> sensors, long start, long end, boolean subsample, String title) {
		super();
		setHeaderVisible(false);
        setBodyBorder(false);
        setScrollMode(Scroll.AUTOY);
		stringForm = new StringTriggerForm(sensors, start, end, subsample,title);
		stringForm.setParent(this);
		add(stringForm);
		LOG.fine ("stringForm added by outerStringForm");
		
		
		ListStore<StringSensorValue> store = new ListStore<StringSensorValue>();
		controlBox = new ComboBox();
	    controlBox.setStore(store);
	    controlBox.setAllowBlank(false);
	    controlBox.setVisible(false);
	    this.add(controlBox);
	}
	
	public ComboBox getControlBox() {
		return this.controlBox;
	}
	
	public StringTriggerForm getStringTriggerForm() {
		return stringForm;
	}
	
	
	public StringTrigger getStringTrigger() {
		StringTrigger strTrigger = stringForm.getStringTrigger();
		return strTrigger;
	}
	 
	 public ArrayList<String> getUnequalValues() {
		 ArrayList<String> unequalValues = stringForm.getUnequalValues();
		 return unequalValues;
	 }
	 
	 public ArrayList<String> getEqualValues() {
		 ArrayList<String> equalValues = stringForm.getEqualValues();
		 return equalValues;
	 }
	
	 /**
	  * Resize all comboBoxes according to parent window size (from AlertCreator)
	  */
	
	public void passParentWindowSize( int width, int height) {
		 stringForm.passParentWindowSize(width, height);
	 }
}
