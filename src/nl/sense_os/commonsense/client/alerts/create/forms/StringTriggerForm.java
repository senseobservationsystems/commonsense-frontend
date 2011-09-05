package nl.sense_os.commonsense.client.alerts.create.forms;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.ListField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CustomButton;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import nl.sense_os.commonsense.client.alerts.create.AlertCreator;
import nl.sense_os.commonsense.client.groups.create.forms.AbstractGroupForm;


class IndexPanel extends HorizontalPanel {
	private int Id;
	
	public IndexPanel() {
		super();
	}
	
	public void setId (int Id) {
		this.Id = Id;
	}
	
	public int getId() {
		return this.Id;
	}
}

class MyWidget extends HorizontalPanel {
	private String Id;
	private boolean equalFieldOrNot;
	
	public MyWidget() {
		super();
	}
	
	public void setId (String Id) {
		this.Id = Id;
	}
	
	public String getId() {
		return this.Id;
	}
	
	public void setEqual(boolean equal) {
		this.equalFieldOrNot = equal;
	}
	
	public boolean getEqual() {
		return this.equalFieldOrNot;
	}
}


public class StringTriggerForm extends AbstractGroupForm {
	
	 private ArrayList<String> values = new ArrayList<String>();
 
	 private MediaButton plusButton;
	 private MediaButton plusButton1;
	 private LabelField titleLabel;
	 private List<StringSensorValue> stringSensorValues;
	 private ListStore<StringSensorValue> store;
	 private StringTriggerForm stringForm;
	 private Logger LOG = Logger.getLogger(AlertCreator.class.getName());
	 private int numEqualFields;
	 private int numUnequalFields;
	 StringTriggerForm form = this;
	 private int currentId; 
	 
	 
	 public class StringSensorValue extends BaseModel {

		  public StringSensorValue() {
		  }

		  public StringSensorValue(String name) {
		    set("name", name);
		  }
	 }
	 
	 public class MediaButton extends CustomButton {
			public MediaButton(Image img, ClickHandler handler) {
				super(img, handler);
			}
			
			public MediaButton (Image img) {
				super(img);
			}	
	 }
	 
	
	 
	 private void createSensorValues() {
		 	stringSensorValues = new ArrayList<StringSensorValue>();
	
			stringSensorValues.add(new StringSensorValue("foo"));
			stringSensorValues.add(new StringSensorValue("bar"));
			stringSensorValues.add(new StringSensorValue("baz"));
			stringSensorValues.add(new StringSensorValue("toto"));
			stringSensorValues.add(new StringSensorValue("tintin"));
			
			store = new ListStore<StringSensorValue>();  
	        store.add(stringSensorValues); 
	 }


	private ComboBox createComboBox (String label, boolean hide) {
		 ComboBox<StringSensorValue> combo = new ComboBox<StringSensorValue>();  
	     combo.setFieldLabel(label);
	     combo.setHideLabel(hide);
	     combo.setDisplayField("name");  
	     combo.setTriggerAction(TriggerAction.ALL);  
	     combo.setStore(store); 
	     //combo.setResizable(true);
	     return combo;
	 }
	
	private void createTitleLabel() {
		titleLabel = new LabelField("<b>Sensor with String Values</b>");
        titleLabel.setHideLabel(true);
	}


	public IndexPanel createPlusCombo () {
		 
		 ComboBox<StringSensorValue> combo = createComboBox ("Alert if value is equal to ", true);
		 plusButton = createPlusButton();
	
	     Grid plusButtonGrid = new Grid (1,1);
	     plusButtonGrid.setWidget(0,0,plusButton);
	     plusButtonGrid.getCellFormatter().setHorizontalAlignment(0, 0, HasAlignment.ALIGN_LEFT);
	     plusButtonGrid.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
	     
		 IndexPanel panel = new IndexPanel();
		 
		 panel.add(combo);
		 panel.add(plusButtonGrid);
	     panel.setCellWidth(combo, "100%");
	     panel.setStyleName ("comboPlusPanel");
	     combo.setSize("98%", "100%"); 
	     
	     return panel;
	 }


	public HorizontalPanel createPlusCombo1 () {
		 
		 ComboBox<StringSensorValue> combo = createComboBox ("Alert if value is equal to ", true);
		 plusButton1 = createPlusButton1();
	     
	     Grid plusButtonGrid = new Grid (1,1);
	     plusButtonGrid.setWidget(0,0,plusButton1);
	     plusButtonGrid.getCellFormatter().setHorizontalAlignment(0, 0, HasAlignment.ALIGN_CENTER);
	     plusButtonGrid.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
	    
	     
		 HorizontalPanel panel = new HorizontalPanel();
		 panel.add(combo);
		 panel.add(plusButtonGrid);
		 panel.setStyleName ("comboPlusPanel");
	     panel.setCellWidth(combo, "100%");
	     combo.setSize("98%", "100%"); 
	     
	     return panel;
	 }


	private MediaButton createPlusButton() {
		 	 
		 Image img = new Image("/images/plus.png");
	     img.setHeight("15px");
	     img.setWidth ("15px");
	     
	     MediaButton plusButton = new MediaButton(img);
	     plusButton.setWidth("15px");
	     plusButton.setHeight("15px");
	     //plusButton.addStyleName("addStuffButton");
	     plusButton.setStyleName("plusButton");
	     
	     plusButton.addClickHandler(new ClickHandler() {
	    	    
	    		public void onClick(ClickEvent event) {
	    			
	    			ArrayList<String> originalIds = new ArrayList<String>();  
	    			
	    			MyWidget panel = createNewCombo();  			
	    			layout();	    							
	    			int count = form.getItemCount();	
	
	    			for (int i = 0; i < count; i++ ) {
		            	 Component c2 = getItem(i);
		            	 String Id = c2.getId();		      
		            	 originalIds.add(Id);
		            }
	    			
	    			int insertIndex = count - (numUnequalFields + 2);
	    			
	    			insert(panel, insertIndex, layoutData);
	    			layout();
	    			count = form.getItemCount();
	    			String newId = null;
	    			 
	    			for (int i = 0; i < count; i++ ) {
		            	 Component c2 = getItem(i);
		            	 String Id = c2.getId();	            	
		            	 if (!originalIds.contains(Id)) newId = Id;
		            }
		        	
	    			//LOG.fine ("Plus Button is clicked. New Id is " + newId);
	    			panel.setId(newId);
	    			panel.setEqual(true);
	    			numEqualFields++;
	    		   	       	      
	   	      }
	   	 });

	     return plusButton;
	 }
	 
	

	private MediaButton createPlusButton1() {
		 
		 
		 Image img = new Image("/images/plus.png");
	     img.setHeight("15px");
	     img.setWidth ("15px");
	     
	     MediaButton plusButton1 = new MediaButton (img);     
	     plusButton1.setWidth("15px");
	     plusButton1.setHeight("15px");
	     plusButton1.setStyleName("plusButton");
	     
	     plusButton1.addClickHandler(new ClickHandler() {
	    	    
	    	 public void onClick(ClickEvent event) {
	    			
	    			ArrayList<String> originalIds = new ArrayList<String>();  
	    			
	    			MyWidget panel = createNewCombo();  			
	    			layout();	    							
	    			int count = form.getItemCount();	
	
	    			for (int i = 0; i < count; i++ ) {
		            	 Component c2 = getItem(i);
		            	 String Id = c2.getId();		      
		            	 originalIds.add(Id);
		            }

	    			add(panel, layoutData);
	    			layout();
	    			count = form.getItemCount();
	    			String newId = null;
	    			 
	    			for (int i = 0; i < count; i++ ) {
		            	 Component c2 = getItem(i);
		            	 String Id = c2.getId();	            	
		            	 if (!originalIds.contains(Id)) newId = Id;
		            }
		        	
	    			//LOG.fine ("Plus Button1 is clicked. New Id is " + newId);
	    			panel.setId(newId);
	    			panel.setEqual(false);
	    			numUnequalFields++;
	    		   	       	      
	   	      }
	   	 });
	      
	     return plusButton1;
	 }
	
	
	public MyWidget createNewCombo () {
		 ComboBox<StringSensorValue> combo = createComboBox ("Alert if value is equal to ", true);
		 MediaButton minButton = createMinButton();
	
		 Grid minButtonGrid = new Grid (1,1);
		 minButtonGrid.setWidget(0,0,minButton);
		 minButtonGrid.getCellFormatter().setHorizontalAlignment(0, 0, HasAlignment.ALIGN_RIGHT);
		 minButtonGrid.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
		 
	
		 final MyWidget panel = new MyWidget();
		 panel.add(combo);
		 panel.add(minButtonGrid);
	     panel.setCellWidth(combo, "100%");
	     combo.setSize("98%", "100%"); 
	    
	     minButton.addClickHandler(new ClickHandler() {
	    	 
	    	 
	          public void onClick(ClickEvent event) {
	             
	             String panelId = panel.getId();
	             
	             boolean equalFieldOrNot = panel.getEqual();
	             if (equalFieldOrNot == true) numEqualFields--;
	             else numUnequalFields--;
	             //LOG.fine ("The panel Id from createNewCombo is " + panelId);
	
	             
	             int count = form.getItemCount();
	             
	             for (int i = 0; i < count; i++ ) {
	            	 Component c2 = getItem(i);
	            	 String Id = c2.getId();
	            	 //LOG.fine ("Getting component " + i + " id is " + Id);
	            	 if (Id.equals(panelId)) {
	            		 //LOG.fine ("Found!");
	            		 form.remove(form.getWidget(i));
	            		 layout();
	            		 break;
	            	 }
	             }     
	             
	          }
	      });
	     
	     return panel;
	 }


	private MediaButton createMinButton() {
		 
		 Image img = new Image("/images/minus.png");
	     img.setHeight("25px");
	     img.setWidth ("17px");
	     
	     MediaButton minButton = new MediaButton(img);	     	
	     minButton.setWidth("15px");
	     minButton.setHeight("15px");
	     minButton.setStyleName("minButton");
	     
	     return minButton;
	 }
	
	
	 public StringTriggerForm() {
		
		super();		
		createSensorValues();
		createTitleLabel();
        this.setLayoutOnChange(true);
        
        
        // init layout
        layoutData.setMargins(new Margins(0, 0, 10, 0));
        FormData layoutData1 = new FormData("-10");  

//        Grid plusButtonGrid = new Grid (1,1);
//        plusButtonGrid.setWidget(0,0,plusButton);
//        
//        Grid plusButtonGrid1 = new Grid (1,1);
//        plusButtonGrid1.setWidget(0,0,plusButton1);
//        
//        plusButtonGrid.getCellFormatter().setHorizontalAlignment(0, 0, HasAlignment.ALIGN_CENTER);
//        plusButtonGrid1.getCellFormatter().setHorizontalAlignment(0, 0, HasAlignment.ALIGN_RIGHT);
         
        LabelField alertEqualLabel = new LabelField("Alert if value is equal to: ");
        LabelField alertUnequalLabel = new LabelField("Alert if value is not equal to: ");
        

        IndexPanel panelEqual = createPlusCombo();
        HorizontalPanel panelNotEqual = createPlusCombo1();
      
        add(titleLabel, layoutData);
        
        add(alertEqualLabel);
        //add(panel1, new FormData ("-12"));  
        add(panelEqual, new FormData ("-8")); 
        //add(plusButtonGrid, new FormData ("-38"));    
        
        add(alertUnequalLabel);
        //add(combo1, new FormData ("-38")); 
        add(panelNotEqual, new FormData ("-8"));
        //add(plusButtonGrid1, layoutData);
       
        int count = form.getItemCount();
    	LOG.fine ("Item count is " + count);
       

        this.setButtonAlign(HorizontalAlignment.RIGHT);
        //add(container, layoutData1);
  
	}


}
