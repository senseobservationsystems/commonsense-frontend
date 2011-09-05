package nl.sense_os.commonsense.client.alerts.create;

import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.alerts.create.forms.NumTriggerForm;
import nl.sense_os.commonsense.client.alerts.create.forms.PosTriggerForm;
import nl.sense_os.commonsense.client.alerts.create.forms.StringTriggerForm;
import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.client.groups.create.forms.GroupAccessMgtForm;
import nl.sense_os.commonsense.client.groups.create.forms.GroupLoginForm;
import nl.sense_os.commonsense.client.groups.create.forms.GroupMemberRightsForm;
import nl.sense_os.commonsense.client.groups.create.forms.GroupNameForm;
import nl.sense_os.commonsense.client.groups.create.forms.GroupPresetsForm;
import nl.sense_os.commonsense.client.groups.create.forms.GroupReqSharingForm;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.google.gwt.user.client.ui.Widget;



public class AlertCreator extends View {
	
	private Window window;
	private Button nextButton;
	private Button backButton;
	private CardLayout layout;
	private GroupNameForm nameForm;
	private NumTriggerForm numTriggerForm;
	private PosTriggerForm posTriggerForm;
	private StringTriggerForm stringTriggerForm;
	private FormButtonBinding formButtonBinding;
	private Logger LOG = Logger.getLogger(AlertCreator.class.getName());
	
	
	public AlertCreator(Controller c) {	
		super(c);
		LOG.setLevel(Level.ALL);
		
	}

	@Override
	protected void handleEvent(AppEvent event) {
		EventType type = event.getType();
		if (type.equals(AlertCreateEvents.ShowCreator)) {
			
			LOG.fine ("ShowCreator event received");
			show();
		}
		else LOG.fine ("NO ShowCreator event received");

	}
	
	@Override
    protected void initialize() {
        super.initialize();

        window = new CenteredWindow();
        window.setHeading("Create new alert");
        window.setSize(500, 450);
        window.setResizable(false);

        layout = new CardLayout();
        window.setLayout(layout);

        initForms();
        initButtons();
    }
	
	
	private void initForms() {

        numTriggerForm = new NumTriggerForm();
        posTriggerForm = new PosTriggerForm();
        stringTriggerForm = new StringTriggerForm();

        window.add(numTriggerForm);
        window.add(posTriggerForm);
        window.add(stringTriggerForm);

    }
	

    private void initButtons() {

        // listener for clicks on the buttons
        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final Button pressed = ce.getButton();
                if (pressed.equals(nextButton)) {
                    if (nextButton.getText().equals("Next")) {
                        //goToNext();
                    } else {
                        //onSubmit();
                    }
                } else if (pressed.equals(backButton)) {
                    //goToPrev();
                } else {
                    //LOG.warning("Unexpected button pressed");
                }
            }
        };

        nextButton = new Button("Next", l);
        backButton = new Button("Back", l);

        window.setButtonAlign(HorizontalAlignment.RIGHT);
        window.addButton(backButton);
        window.addButton(nextButton);
    }
	
    
    
    private void showStringTriggerForm() {
        
        layout.setActiveItem(stringTriggerForm);
    }
    

	private void show() {
		showStringTriggerForm();
		window.show();
        window.center();
	} 
}
