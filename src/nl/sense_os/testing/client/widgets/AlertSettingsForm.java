package nl.sense_os.testing.client.widgets;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.BoxLayout.BoxLayoutPack;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;


/**
 * This class is to prototype the alert setting form.
 * 
 * It's useful to test form elements, layouts and form events.
 * 
 * @author fede
 *
 */
public class AlertSettingsForm extends ContentPanel {	
	
	public AlertSettingsForm() {
		setHeaderVisible(false);

		// Form
		FormPanel form = new FormPanel();		
		form.setFrame(true);
		//form.setLabelSeparator("");
		form.setHeading("Alert settings");
		form.setWidth(350);

		FormLayout formLayout = new FormLayout();
		formLayout.setLabelSeparator("");		
		form.setLayout(formLayout);
		
		// Sensor combo
		ListStore<AlertSettingsForm.Sensor> sensorStore = new ListStore<AlertSettingsForm.Sensor>();
		sensorStore.add(getSensors());		
		
		ComboBox<AlertSettingsForm.Sensor> sensorCombo = new ComboBox<AlertSettingsForm.Sensor>();
		sensorCombo.setFieldLabel("sensor");
		sensorCombo.setStore(sensorStore);
		sensorCombo.setValueField("id");
		sensorCombo.setDisplayField("name");
		//sensorCombo.setAutoWidth(true);

		// Chechboxes
		CheckBox check1 = new CheckBox();
		check1.setBoxLabel("sms");
		check1.setValue(true);

		CheckBox check2 = new CheckBox();
		check2.setBoxLabel("email");

		CheckBox check3 = new CheckBox();
		check3.setBoxLabel("call");		
		
		CheckBoxGroup checkGroup = new CheckBoxGroup();
		checkGroup.setFieldLabel("alert by");
		checkGroup.add(check1);
		checkGroup.add(check2);
		checkGroup.add(check3);	
		
		// Threshold container 
		LayoutContainer thresholdContainer = new LayoutContainer();
		HBoxLayout thresholdLayout = new HBoxLayout();
		thresholdLayout.setPadding(new Padding(5));
		thresholdLayout.setHBoxLayoutAlign(HBoxLayoutAlign.MIDDLE);
		thresholdLayout.setPack(BoxLayoutPack.START);
		thresholdContainer.setLayout(thresholdLayout);

		ListStore<AlertSettingsForm.Operator> store = new ListStore<AlertSettingsForm.Operator>();
		store.add(getOperators());
		
		ComboBox<AlertSettingsForm.Operator> operatorCombo = new ComboBox<AlertSettingsForm.Operator>();
		operatorCombo.setStore(store);
		operatorCombo.setValueField("id");
		operatorCombo.setDisplayField("name");		
		operatorCombo.setWidth(100);
		operatorCombo.setEmptyText("less/greater ...");		
		
		thresholdContainer.add(operatorCombo, new HBoxLayoutData(new Margins(0, 5, 0, -5)));
		
		TextField<String> thresholdValue = new TextField<String>();
		thresholdValue.setFieldLabel("value");
		thresholdValue.setWidth(200);
		//thresholdValue.setAutoWidth(true);
		
		thresholdContainer.add(thresholdValue, new HBoxLayoutData(new Margins(0, 5, 0, 0)));

		// Buttons
		Button saveBtn = new Button();
		saveBtn.setText("save");
		saveBtn.setWidth(100);
		saveBtn.addListener(Events.OnClick, new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent be) {
				System.out.println("event: " + be.getEvent().getString());
				System.out.println("save pressed");
			}
		});
		
		Button cancelBtn = new Button();
		cancelBtn.setText("cancel");
		cancelBtn.setWidth(100);
		cancelBtn.addListener(Events.OnClick, new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent be) {
				System.out.println("event: " + be.getEvent().getString());
				System.out.println("cancel pressed");
			}
		});
		
		LayoutContainer btnContainer= new LayoutContainer();
		HBoxLayout layout = new HBoxLayout();
		layout.setPadding(new Padding(5));  
		layout.setHBoxLayoutAlign(HBoxLayoutAlign.MIDDLE);  
		layout.setPack(BoxLayoutPack.CENTER);  
		btnContainer.setLayout(layout);  

		HBoxLayoutData layoutData = new HBoxLayoutData(new Margins(0, 5, 0, 0));  
		btnContainer.add(saveBtn, layoutData);
		btnContainer.add(cancelBtn, layoutData);
		
		// Adds widgets to the form.
		/*
		form.add(sensorCombo, formData);
		form.add(thresholdContainer, formData);
		form.add(checkGroup, formData);
		form.add(btnContainer, formData);
		*/
		form.add(sensorCombo);
		form.add(thresholdContainer);
		form.add(checkGroup);
		form.add(btnContainer);		

		// Adds the form to the content panel.
		add(form);
	}

	/**
	 * Data model of the operator combobox.
	 * @author fede
	 *
	 */
	protected class Operator extends BaseModel {
		public Operator() {
			
		}
		
		public Operator(Integer id, String name) {
			setId(id);
			setName(name);
		}
		
		public void setId(Integer id) {
			set("id", id);
		}
		
		public void setName(String name) {
			set("name", name);
		}
		
		public Integer getId() {
			Integer id = (Integer) get("id");
			return id;
		}
		
		public String getName() {
			return (String) get("name");
		}
	}
	
	/**
	 * This is a fake method.
	 * It will be replaced by the data returned from the db, once the 
	 * php scripts are finished.
	 * @return list of operators
	 */
	private List<AlertSettingsForm.Operator> getOperators() {
		List<AlertSettingsForm.Operator> operators = new ArrayList<AlertSettingsForm.Operator>();
		
		operators.add(new Operator(1, "less than"));
		operators.add(new Operator(2, "grather than"));
		
		return operators;
	}
	
	/**
	 * Data model for sensor combobox.
	 * @author fede
	 *
	 */
	protected class Sensor extends BaseModel {
		public Sensor() {
			
		}
		
		public Sensor(Integer id, String name) {
			setId(id);
			setName(name);
		}
		
		public void setId(Integer id) {
			set("id", id);
		}
		
		public void setName(String name) {
			set("name", name);
		}
		
		public Integer getId() {
			Integer id = (Integer) get("id");
			return id;
		}
		
		public String getName() {
			return (String) get("name");
		}
	}

	/**
	 * This is a fake method.
	 * It will be replaced by the data returned from the db, once the 
	 * php scripts are finished.
	 * @return a list of sensors.
	 */
	private List<AlertSettingsForm.Sensor> getSensors() {
		List<AlertSettingsForm.Sensor> sensors = new ArrayList<AlertSettingsForm.Sensor>();
		
		sensors.add(new Sensor(1, "accelerometer"));
		sensors.add(new Sensor(2, "gps"));
		sensors.add(new Sensor(3, "noise"));
		
		return sensors;
	}
	
}
