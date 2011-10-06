package nl.sense_os.commonsense.client.alerts.create.forms;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.alerts.create.AlertCreator;
import nl.sense_os.commonsense.client.alerts.create.triggers.NumericTrigger;
import nl.sense_os.commonsense.client.common.models.SensorModel;

import com.extjs.gxt.ui.client.widget.form.FormPanel;

public class OuterNumTriggerForm extends FormPanel{

	private NumTriggerForm numForm;
	private Logger LOG = Logger.getLogger(AlertCreator.class.getName());
	
	public OuterNumTriggerForm (List<SensorModel> sensors, long start, long end, 
			boolean subsample, String title) {
		super();
		LOG.setLevel(Level.ALL);
		setHeaderVisible(false);
        setBodyBorder(false);
        numForm = new NumTriggerForm(sensors, start, end, subsample,title); 
		//numForm = new NumTriggerForm(sensors, start, end, subsample,title);

//        setWidth("100%");
//        setHeight("100%");
//        numForm.setHeight("100%");
//        numForm.setWidth("50%");
		add(numForm);
		LOG.fine ("numForm added by outerNumForm");
	}
	
	 /**
	  * Resize all comboBoxes according to parent window size (from AlertCreator)
	  */
	
	public void passParentWindowSize( int width, int height) {

		//int w = this.getWidth();
		//LOG.fine ("This width is " + w);
		numForm.passParentWindowSize(width, height);

		 
	 }
	
	public NumericTrigger getNumericTrigger() {
		NumericTrigger thresh = numForm.getNumericTrigger();
		return thresh;
	}
}