package nl.sense_os.commonsense.client.alerts.create.utils;

import java.util.Date;
import java.util.List;

import nl.sense_os.commonsense.client.alerts.create.forms.AbstractAlertForm;
import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.client.common.models.SensorModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.DatePickerEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;

public class NewRangeRequest {
	
	private Date startDate;
	private Date endDate;
	private long longStart;
	private long longEnd;

	
	public NewRangeRequest (List<SensorModel> sensors, boolean subsample, AbstractAlertForm parent) {
		
		final AbstractAlertForm parent1 = parent;
		final List<SensorModel> sensors1 = sensors;
		final boolean subsample1 = subsample;
		
		final Window win = new  CenteredWindow();
		win.setHeading("Type in custom range");
	    win.setSize(450, 250);
	    win.setResizable(true);
	    
	    FormPanel simple = new FormPanel();  
	    simple.setHeaderVisible(false);
	    simple.setBodyBorder(false);
	    simple.setButtonAlign(HorizontalAlignment.RIGHT);
		
	    LabelField label = new LabelField();
	    label.setText("<b>No data found in the given range. Try a different range</b>");
	    
	    DateField startField = new DateField();  
	    startField.setFieldLabel("Start Date");
	    startField.setHideLabel(false);
	    startField.setWidth(50);
	    startField.setAllowBlank(false);
	    
	    startField.getDatePicker().addListener(Events.Select, new Listener<DatePickerEvent>() {
	        public void handleEvent(DatePickerEvent be) {
	            //LOG.fine ("This start date selected: " + be.getDate());
	            startDate = be.getDate();
	        }          
	    });
	    
	    DateField endField = new DateField();  
	    endField.setFieldLabel("End Date");
	    endField.setHideLabel(false);
	    endField.setWidth(50);
	    endField.setAllowBlank(false);
	    endField.setValue(new Date());
	    
	    endDate = endField.getValue();
	    
	    endField.getDatePicker().addListener(Events.Select, new Listener<DatePickerEvent>() {
	        public void handleEvent(DatePickerEvent be) {
	            //LOG.fine ("This end date selected: " + be.getDate());
	            endDate = be.getDate();
	        }          
	    });
	    
	    SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {
	
	        @SuppressWarnings("deprecation")
			@Override
	        public void componentSelected(ButtonEvent ce) {
	        	longStart = startDate.getTime();
	            longEnd = endDate.getTime();
	            parent1.goVisualize(sensors1, longStart, longEnd, subsample1);
	            win.close();
	            
	        }
	    };
	    
	    Button button = new Button("Try", l);
	    FormButtonBinding formButtonBinding = new FormButtonBinding(simple);
	    formButtonBinding.addButton(button);       
	    
	    simple.add(label);
	    simple.add(startField);  
	    simple.add(endField);
	    simple.add(button);
	    
		win.add(simple);
		win.show();
		
	}
}
