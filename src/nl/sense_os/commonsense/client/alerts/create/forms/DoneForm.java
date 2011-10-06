package nl.sense_os.commonsense.client.alerts.create.forms;

import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
//import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.ui.VerticalPanel;


public class DoneForm extends FormPanel	{
	
	private VerticalPanel vp;  	  
	//private FormData formData; 
	private LabelField titleLabel;
	
	public DoneForm() {
	
		super();
		setHeaderVisible(false);
	    setBodyBorder(false);
	    //setScrollMode(Scroll.AUTOY);
	 
	    //formData = new FormData("-20"); 
	 
	    vp = new VerticalPanel();  
	    vp.setSpacing(10); 
	 
	    createTitleLabel();
	 
	    this.add(vp);  
	}
	

	/**
    Create form title
    */
   
	private void createTitleLabel() {
	   titleLabel = new LabelField("<b>Alert Created!</b>");
       titleLabel.setHideLabel(true);
       titleLabel.setStyleName ("titleLabel");
       this.add(titleLabel);      
	}


}
