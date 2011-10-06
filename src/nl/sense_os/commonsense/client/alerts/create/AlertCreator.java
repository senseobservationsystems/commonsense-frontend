package nl.sense_os.commonsense.client.alerts.create;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.alerts.create.forms.AlertTypesForm;
import nl.sense_os.commonsense.client.alerts.create.forms.DoneForm;
import nl.sense_os.commonsense.client.alerts.create.forms.NumTriggerForm;
import nl.sense_os.commonsense.client.alerts.create.forms.OuterNumTriggerForm;
import nl.sense_os.commonsense.client.alerts.create.forms.OuterStringTriggerForm;
import nl.sense_os.commonsense.client.alerts.create.forms.PosTriggerForm;
import nl.sense_os.commonsense.client.alerts.create.triggers.AlertType;
import nl.sense_os.commonsense.client.alerts.create.triggers.NumericTrigger;
import nl.sense_os.commonsense.client.alerts.create.triggers.PositionTrigger;
import nl.sense_os.commonsense.client.alerts.create.triggers.StringTrigger;
import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.LayoutData;


public class AlertCreator extends View {
	
	private Window window;
	private Button nextButton;
	private Button backButton;
	private Button moreButton;
	private Button doneButton;
	private CardLayout layout;
	private PosTriggerForm posTriggerForm;
	private NumTriggerForm numTriggerForm;
	private AlertTypesForm alertTypesForm;
	private DoneForm doneForm;
	private Component prevComponent;
	private Logger LOG = Logger.getLogger(AlertCreator.class.getName());
	private long defaultStart = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 5; // 5 days ago
	private SensorModel sens;
	private String datatype;
	private String name;
	private OuterStringTriggerForm outerStringTriggerForm;
	private OuterNumTriggerForm outerNumTriggerForm;
	private StringTrigger strTrigger; 
	private NumericTrigger numTrigger;
	private PositionTrigger posTrigger;
	private ArrayList<AlertType> alertTypeList;
	private FormButtonBinding formButtonBinding;
	
	
	public AlertCreator(Controller c) {	
		super(c);
		LOG.setLevel(Level.ALL);
		
	}

	@Override
	protected void handleEvent(AppEvent event) {
		EventType type = event.getType();
		if (type.equals(AlertCreateEvents.ShowCreator)) {
			
			LOG.fine ("ShowCreator event received");
			sens = event.getData("sensor");	
			show(sens);			
		}
		
		else LOG.fine ("NO ShowCreator event received");

	}
	
	@Override
    protected void initialize() {
		
        super.initialize();
        
        window = new CenteredWindow();
        window.setHeading("Create new alert");
        window.setSize(500, 450);
        window.setResizable(true);

        layout = new CardLayout();
        window.setLayout(layout);

        initForms();
        initButtons();     

    }
	
	
	private void initForms() {
		
        alertTypesForm = new AlertTypesForm();
        doneForm = new DoneForm();
        
        window.add(alertTypesForm);
        window.add(doneForm);
        

    }
	
	
    private void initButtons() {
    	
    	LOG.fine ("InitButtons");
        // listener for clicks on the buttons
        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final Button pressed = ce.getButton();
                if (pressed.equals(nextButton)) {                
                    goToNext();               
                } else if (pressed.equals(backButton)) {
                    goToPrev();
                } else if (pressed.equals(moreButton)) {
                    goToMore();
                } else if (pressed.equals(doneButton)) {
                    goToDone();                
                } else {
                    LOG.warning("Unexpected button pressed");
                }
            }
        };

        nextButton = new Button("Next", l);
        backButton = new Button("Back", l);
        moreButton = new Button ("More", l);
        doneButton = new Button ("Done", l);
        
        nextButton.setBorders(false);
        nextButton.addStyleName("formButton");
        backButton.addStyleName("formButton");
        moreButton.addStyleName("formButton");
        doneButton.addStyleName("formButton");
        
        window.setButtonAlign(HorizontalAlignment.RIGHT);
        window.addButton(backButton);
        window.addButton(nextButton);
        window.addButton(moreButton);
    	window.addButton(doneButton);
    	
    	backButton.hide();
    	moreButton.hide();
    	doneButton.hide();
    	nextButton.show();
             
    }
    
    private void showButtons() {
    	backButton.hide();
    	moreButton.hide();
    	doneButton.hide();
    	nextButton.show();
    }
    
    
    private void goToPrev() {
	    Component active = layout.getActiveItem();
	    if (active.equals(alertTypesForm) || active.equals(doneForm)) {
	        
	    	if (prevComponent.equals(outerStringTriggerForm)) 
	    		showStringTriggerForm();
	    	else if (prevComponent.equals(posTriggerForm))
	    		showPosTriggerForm();
	    	else if (prevComponent.equals(numTriggerForm) || prevComponent.equals(outerNumTriggerForm)) {
	    		showNumTriggerForm();		
	    	}
	    	
	        moreButton.hide();
	        doneButton.hide();
	        nextButton.show();
	        backButton.hide();
	        
	    }
	}

	private void goToNext() {
	    Component active = layout.getActiveItem();
	    prevComponent = active;
	    boolean validTrigger = false; 
	    
	    if (active.equals(posTriggerForm) || active.equals(outerStringTriggerForm) || active.equals(outerNumTriggerForm)) {
	    	    	
	    	if (active.equals(posTriggerForm)) {
	    		posTrigger = posTriggerForm.getPositionTrigger();
	    		if (posTrigger!= null) validTrigger = true;
	    	
	    	} else if (active.equals(outerStringTriggerForm)) {	    		
	    		strTrigger = outerStringTriggerForm.getStringTrigger();
	    		if (strTrigger!= null) validTrigger = true;
	    		
	    	} else if (active.equals(outerNumTriggerForm)) {
	    		numTrigger = outerNumTriggerForm.getNumericTrigger();
	    		if (numTrigger!= null) validTrigger = true;	    		
	    	}
	    	
	    	if (validTrigger) {
	    		
//    			moreButton.show();
//		        doneButton.show();
//		        nextButton.hide();
//		        backButton.show();
		        showAlertTypesForm();
	    	}
	    }
    }
    
    
    private void goToMore() {	    	
     		AlertType alertType = alertTypesForm.getAlertType();
     		
     		if (alertType!= null) {
     			alertTypeList.add(alertType);
     			
    			String description = alertType.getDescription();   			
	    		alertTypesForm.setFieldsBlank();
	    		alertTypesForm.setDescription(description);	    		
		        showAlertTypesForm();
		        moreButton.show();
		        doneButton.show();
		        nextButton.hide();
    		}	    	
    }
    
    

    private void goToDone() {
	    AlertType alertType = alertTypesForm.getAlertType();
	    if (alertType!= null) {
	    	alertTypeList.add(alertType);
	    }
	    
		if (alertTypeList.size()> 0) {
			LOG.fine ("So many alertTypes found: " + alertTypeList.size());
	        showDoneForm();
	        moreButton.hide();
	        doneButton.hide();
	        nextButton.hide();
	        backButton.hide();
		}	    	
    }
    
    
    private void showStringTriggerForm() {  
    	layout.setActiveItem(outerStringTriggerForm);      
    }
    
    private void showNumTriggerForm() {  	
        layout.setActiveItem(outerNumTriggerForm);
	}
    
    private void showPosTriggerForm() {  
        layout.setActiveItem(posTriggerForm);
	}
 
    private void showAlertTypesForm() {	
    	layout.setActiveItem(alertTypesForm); 
	
    	formButtonBinding = new FormButtonBinding(alertTypesForm);
    	formButtonBinding.addButton(moreButton);
    	formButtonBinding.addButton(doneButton);
    	
    	moreButton.show();
        doneButton.show();
        nextButton.hide();
        backButton.show();
        //moreButton.setEnabled(false);
    }
    
    private void showDoneForm() {	
    	layout.setActiveItem(doneForm);
    }
    
	private void show(SensorModel sens) {
		
		datatype = sens.getDataType();
		LOG.fine ("Got datatype " + datatype);
		name = sens.getName();
		//LOG.fine ("Got name " + name);
		
		ArrayList<SensorModel> sensors = new ArrayList<SensorModel>();
		sensors.add(sens);
		        
        long start = defaultStart;
		long end = System.currentTimeMillis();
		alertTypeList = new ArrayList<AlertType>();

		//visualize(sensors, start, end, true); 
		
		if (datatype.equals("string")) {		
			 outerStringTriggerForm = new OuterStringTriggerForm(sensors, start, end, true, "String form");
			 window.add(outerStringTriggerForm);
			 showStringTriggerForm();
			 showButtons();
		}
		
		
		else if (datatype.equals("float")) {
			outerNumTriggerForm = new OuterNumTriggerForm(sensors, start, end, true, "Numeric form");		
			window.add(outerNumTriggerForm);
			showNumTriggerForm();
			showButtons();
		}
		
		else if (name.contains ("position")) {
			posTriggerForm = new PosTriggerForm();
			window.add(posTriggerForm);
			showPosTriggerForm();
			showButtons();
		}

	    window.show();
		window.center();	
		window.addListener(Events.Resize, new Listener<WindowEvent>() {
			
			            @Override
			            public void handleEvent( WindowEvent we) {
			               //we.getWidth();
			               //height = we.getHeight();
			               if (layout.getActiveItem().equals(outerStringTriggerForm)) outerStringTriggerForm.passParentWindowSize(we.getWidth(), we.getHeight());
			               else if (layout.getActiveItem().equals(outerNumTriggerForm)) outerNumTriggerForm.passParentWindowSize(we.getWidth(), we.getHeight());
			               alertTypesForm.passParentWindowSize(we.getWidth(), we.getHeight());
			            }
			    	 
			    	});
		
		 if (layout.getActiveItem().equals(posTriggerForm)) posTriggerForm.afterShow();
	} 	
}
