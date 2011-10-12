package nl.sense_os.commonsense.client.alerts.create.forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.alerts.create.triggers.StringTrigger;
import nl.sense_os.commonsense.client.alerts.create.utils.MediaButton;
import nl.sense_os.commonsense.client.alerts.create.utils.MyWidget;
import nl.sense_os.commonsense.client.alerts.create.utils.StringSensorValue;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
//import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.user.client.ui.CustomButton;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;


//@SuppressWarnings("serial")
//class StringSensorValue extends BaseModel {
//	  private String name;
//	
//	  public StringSensorValue() {
//	  }
//
//	  public StringSensorValue(String name) {
//	    set("name", name);
//	    this.name = name;
//	  }
//	  
//	  public String getName() {
//		  return this.name;
//	  }
//	  
//}
//
//class MediaButton extends CustomButton {
//		public MediaButton(Image img, ClickHandler handler) {
//			super(img, handler);
//		}
//		
//		public MediaButton (Image img) {
//			super(img);
//		}	
//}
//
//
//class MyWidget extends HorizontalPanel {
//		private String Id;
//		private boolean equalFieldOrNot;
//		
//		public MyWidget() {
//			super();
//		}
//		
//		public void setId (String Id) {
//			this.Id = Id;
//		}
//		
//		public String getId() {
//			return this.Id;
//		}
//		
//		public void setEqual(boolean equal) {
//			this.equalFieldOrNot = equal;
//		}
//		
//		public boolean getEqual() {
//			return this.equalFieldOrNot;
//		}
//	}


public class StringTriggerForm extends AbstractAlertForm {
	 
	 private Logger LOG = Logger.getLogger(StringTriggerForm.class.getName());
	 private LabelField titleLabel;
	 private ComboBox<StringSensorValue> combo1;
	 private ComboBox<StringSensorValue> combo2;
	 private MediaButton plusButton1;
	 private MediaButton plusButton2;
	 StringTriggerForm form = this;
		 
	 private List<StringSensorValue> stringSensorValues;
	 private ListStore<StringSensorValue> store;
	 
	 @SuppressWarnings("unchecked")
	 private ArrayList<ComboBox> comboList; 
	 @SuppressWarnings("unchecked")
	 private ArrayList<ComboBox> equalComboList;
	 @SuppressWarnings("unchecked")
	 private ArrayList<ComboBox> unequalComboList;
	 private ArrayList<String> originalIds;
	 
	 private int numEqualFields;
	 private int numUnequalFields;
	 private int parent_width;
	 private static final int PLUSBUTTONSIZE = 72;
	 
	 private ComboBox<StringSensorValue> controlCombo; 
	 private StringSensorValue controlValue;
	 private ComboBox controlBox;
	 private OuterStringTriggerForm parent;
	 private StringTriggerForm th = this;
	 	   
	 //private JsArray<Timeseries> data;
	 
	 
	 @SuppressWarnings("unchecked")
	 public StringTriggerForm (List<SensorModel> sensors, long start, long end, boolean subsample, String title) {
			super();
			LOG.setLevel(Level.ALL);
			this.setLayoutOnChange(true);
			layoutData.setMargins(new Margins(0, 0, 10, 0));	    
			//FormData layoutData1 = new FormData("-10");  
			        
			comboList = new ArrayList<ComboBox>();	
			equalComboList = new ArrayList<ComboBox>();
			unequalComboList = new ArrayList<ComboBox>();
			controlCombo = new ComboBox<StringSensorValue>();
			createSensorValues();		
			createTitleLabel();      
			createPlusCombo1();
			createPlusCombo2();      
			getOriginalIds();			
			
			this.setButtonAlign(HorizontalAlignment.RIGHT);
		
			visualize(sensors, start, end, subsample);	
		}
	 
	 	
	 	public void setParent(OuterStringTriggerForm panel) {
	 		this.parent = panel;
	 	}
	 	
	 	public OuterStringTriggerForm getParent() {
	 		return this.parent;
	 	}
	 
	 	@Override
	    protected void onNewData(JsArray<Timeseries> data) {
			LOG.fine ("Hey got data");
			//LOG.fine ("Datatype is " + datatype);
			int records = data.get(0).getData().length();
			LOG.fine ("Number of records: " + records);
			
			ArrayList<String> values = new ArrayList<String>();
	    	
	    	for (int i = 0; i < records; i ++ ) {
	    		String el = data.get(0).getData().get(i).getRawValue();
	    		if (!values.contains(el)) {
	    			values.add(el);
	    		}   	
	    	}
	    	
	    	stringSensorValues.clear();
	    	stringSensorValues.add(new StringSensorValue("(no selection)"));
	    	
	    	for (int i = 0; i < values.size(); i++ ) {
	    		//LOG.fine ("Element " + i + " equals " + values.get(i));
	    		stringSensorValues.add(new StringSensorValue(values.get(i)));
	    		store.removeAll();
	    		store.add(stringSensorValues); 
	    		combo1.setStore(store);
	    		combo2.setStore(store);
	    		layout();
	    		
	    	}
			
	 	}
	 	
	
	 /**
	  * Initialize an arrayList of string sensor values
	  */
	 
	 private void createSensorValues() {
		 	stringSensorValues = new ArrayList<StringSensorValue>();				
			store = new ListStore<StringSensorValue>();  
			//store = new ListStore<String>();
		
	        store.add(stringSensorValues); 
	 }
	 
	 
	 /**
	  * Create a ComboBox with a hidden label, and assign its Store
	  */ 
	
	@SuppressWarnings("unchecked")
	private ComboBox createComboBoxx() {
		ComboBox<StringSensorValue> combo = new ComboBox<StringSensorValue>();  
        combo.setDisplayField("name");  
        combo.setWidth("100%"); 
        combo.setHideLabel(true);
        combo.setStore(store);  
        combo.setTypeAhead(true); 
        combo.setAllowBlank(true);
        combo.setTriggerAction(TriggerAction.ALL);  
        
        comboList.add(combo);
        
        return combo;
	}
	
	/**
	  * Creates and adds a title label for the form
	  */
	
	private void createTitleLabel() {
		titleLabel = new LabelField("<b>Sensor with String Values</b>");
        titleLabel.setHideLabel(true);
        add(titleLabel, layoutData);
	}
	
	
	/**
	  * Gets the assigned Ids of layout elements and puts them in a new ArrayList
	  */
	
	public void getOriginalIds() {
		 originalIds = new ArrayList<String>(); 
	        
	     int count = form.getItemCount();
	     //LOG.fine ("Item count is " + count);
	
	     for (int i = 0; i < count; i++ ) {
	        Component c2 = getItem(i);
	        String Id = c2.getId();		      
	        originalIds.add(Id);
	     }
	 }

	
	/**
	  * Creates and formats a grid to hold a plus Button a
	  */
	
	public Grid createPlusButtonGrid(MediaButton button) {
		 Grid buttonGrid = new Grid (1,1);
		 buttonGrid.setWidget(0,0,button);
		 buttonGrid.getCellFormatter().setHorizontalAlignment(0, 0, HasAlignment.ALIGN_LEFT);
		 buttonGrid.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
	     return buttonGrid;
	}
	
	/**
	  * Creates and formats a grid to hold the MinButton
	  */
	
	public Grid createMinButtonGrid(MediaButton button) {
		 Grid minButtonGrid = new Grid (1,1);
		 minButtonGrid.setWidget(0,0, button);
		 minButtonGrid.getCellFormatter().setHorizontalAlignment(0, 0, HasAlignment.ALIGN_RIGHT);
		 minButtonGrid.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);		 
	     return minButtonGrid;
	}

	/**
	  * Creates a combination of a label, comboBox, and Plus button for "Alert if value is equal", and 
	  * adds it to the layout
	  */
	
	@SuppressWarnings("unchecked")
	public void createPlusCombo1 () {
		 
		 
		 LabelField alertEqualLabel = new LabelField("Alert if value is equal to: ");
		 combo1 = createComboBoxx(); 
		 combo1.setWidth(420);
		 combo1.setEditable(false);
		 
		 
//		 combo1.addKeyListener(new KeyListener() {
//			 public void componentKeyDown(ComponentEvent event) {
		 
		 
		 combo1.addSelectionChangedListener(new SelectionChangedListener() {
			public void selectionChanged (SelectionChangedEvent event) {		 
				ComboBox comboBox = (ComboBox) event.getSource();
				
				//LOG.fine("Selected Item  = " + comboBox.getSelection());

				boolean ok = false; 
				
				for (int i = 0; i < equalComboList.size(); i ++) {
					ComboBox<StringSensorValue> box = equalComboList.get(i);
					String name = box.getRawValue();
					if (!name.equals(null) &&!name.equals("(no selection)")) {
						ok = true;
						OuterStringTriggerForm outerStr = th.getParent();
						outerStr.getControlBox().setRawValue(name);
						break;
					}
				}
				
				for (int i = 0; i < unequalComboList.size(); i ++) {
					ComboBox<StringSensorValue> box = unequalComboList.get(i);
					String name = box.getRawValue();
					if (!name.equals(null) &&!name.equals("(no selection)")) {
						ok = true;
						OuterStringTriggerForm outerStr = th.getParent();
						outerStr.getControlBox().setRawValue(name);
						//controlBox.setRawValue(name);
						break;
					}
				}
				
				LOG.fine ("OK is " + ok);
				//if (!ok) controlBox.setRawValue(null);
				if (!ok) {
					OuterStringTriggerForm outerStr = th.getParent();
					outerStr.getControlBox().setRawValue(null);
				}
				
				LOG.fine ("The controlBox value is " + th.getParent().getControlBox().getRawValue());
				
			 }
		 });
		 
		 
		 equalComboList.add(combo1);
		 plusButton1 = createPlusButton1();	 
		 Grid plusButtonGrid = createPlusButtonGrid (plusButton1);     
 
		 HorizontalPanel panel = new HorizontalPanel();
		 panel.add(combo1);
		 panel.add(plusButtonGrid);
	     
	     VerticalPanel vp = new VerticalPanel();
	     vp.setStyleName ("comboPlusPanel");
	     vp.add(alertEqualLabel);   
	     vp.add(panel);
	     add(vp);
	     
	     controlBox = new ComboBox();
	     controlBox.setStore(store);
	     controlBox.setAllowBlank(false);
	     controlBox.setVisible(false);
	     this.add(controlBox);
	     
	    
	 }

	
	/**
	  * Creates a combination of a label, comboBox, and Plus button for "Alert if value is not equal", 
	  * and adds it to the layout
	  */
	
	@SuppressWarnings("unchecked")
	public void createPlusCombo2 () {
		 
		 LabelField alertUnequalLabel = new LabelField("Alert if value is not equal to: ");	 
		 combo2 = createComboBoxx();
		 combo2.setWidth(420);
		 combo2.setEditable(false);
		 unequalComboList.add(combo2);
		 plusButton2 = createPlusButton2();
		 Grid plusButtonGrid = createPlusButtonGrid (plusButton2);
		 
		 HorizontalPanel panel = new HorizontalPanel();
		 panel.add(combo2);
		 panel.add(plusButtonGrid);
		 
		 VerticalPanel vp = new VerticalPanel();
		 vp.setStyleName ("comboPlusPanel");
	     vp.add(alertUnequalLabel);  
	     vp.add(panel);
	     
	     add(vp);	
		
	 }

	/**
	 * Creates a plus button and adds a clickListener
	 * @return
	 */
	
	private MediaButton createPlusButton1() {
		 	 
		 Image img = new Image("/images/plus.png");
	     img.setHeight("15px");
	     img.setWidth ("15px");
	     
	     MediaButton plusButton1 = new MediaButton(img);
	     plusButton1.setWidth("15px");
	     plusButton1.setHeight("15px");
	     plusButton1.setStyleName("plusButton");
	     
	     plusButton1.addClickHandler(new ClickHandler() {
	    	    
	    		public void onClick(ClickEvent event) {
	
	    			MyWidget panel = createNewCombo(true); 
	    			VerticalPanel vp = new VerticalPanel();	    	
	    		    vp.setStyleName ("comboPlusPanel");
	    		    vp.add(panel);
	    			
	    			int count = form.getItemCount();		
	    			int insertIndex = count - (numUnequalFields + 1);    			
	    			insert(vp, insertIndex, layoutData);
	    			
	    			String newId = getNewId();	        				
	    			panel.setId(newId);
	    			panel.setEqual(true);
	    			numEqualFields++;    			
	    			//LOG.fine ("Plus Button 1 is clicked. New Id is " + newId);
	    		   	       	      
	   	      }
	   	 });
	
	     return plusButton1;
	 }


	/**
	 * Creates a second plusButton and adds a clickListener
	 * @return
	 */
	
	private MediaButton createPlusButton2() {
		 	 
		 Image img = new Image("/images/plus.png");
	     img.setHeight("15px");
	     img.setWidth ("15px");
	     
	     MediaButton plusButton2 = new MediaButton (img);     
	     plusButton2.setWidth("15px");
	     plusButton2.setHeight("15px");
	     plusButton2.setStyleName("plusButton");
	     
	     plusButton2.addClickHandler(new ClickHandler() {
	    	    
	    	 public void onClick(ClickEvent event) {
	
	    			MyWidget panel = createNewCombo(false);  
	    			VerticalPanel vp = new VerticalPanel();	    	
	    		    vp.setStyleName ("comboPlusPanel");
	    		    vp.add(panel);    			
	    			add(vp, layoutData);
	
	    			String newId = getNewId();  			
	    			panel.setId(newId);
	    			panel.setEqual(false);
	    			numUnequalFields++; 			
	    			//LOG.fine ("Plus Button1 is clicked. New Id is " + newId);
	    		   	       	      
	   	      }
	   	 });
	      
	     return plusButton2;
	 }


	/**
	 * Creates a combination of a comboBox and minButton 
	 */
	
	@SuppressWarnings("unchecked")
	public MyWidget createNewCombo (boolean equal) {
		 
		 final ComboBox<StringSensorValue> combo = createComboBoxx ();
		 combo.setEditable(false);
		 if (equal) equalComboList.add(combo);
		 else unequalComboList.add(combo);
		 
		 MediaButton minButton = createMinButton();
		 Grid minButtonGrid = createMinButtonGrid(minButton);
		
		 final MyWidget panel = new MyWidget();
		 panel.add(combo);
		 panel.add(minButtonGrid);
		 combo.setWidth (combo2.getWidth());// parent_width - PLUSBUTTONSIZE); 
	     	         
	     minButton.addClickHandler(new ClickHandler() {
	
	          public void onClick(ClickEvent event) {	             
	             
	        	 String panelId = panel.getId();	             
	             boolean equalField = panel.getEqual();
	             
	             if (equalField) {
	            	 numEqualFields--;
	            	 equalComboList.remove(combo);
	             }
	             else {
	            	 numUnequalFields--;
	            	 unequalComboList.remove(combo);
	             }
	             
	             removeWidget (panelId);	                         
	          }
	      });
	     
	     return panel;
	 }
	
	
	/**
	 * Creates a minButton
	 * @return
	 */
	
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


	/**
	 * Gets the assigned Id of a newly created layout element
	 */

	private String getNewId() {
		int count = form.getItemCount();	    			
		String newId = null;
		 
		for (int i = 0; i < count; i++ ) {
	    	 Component c2 = getItem(i);
	    	 String Id = c2.getId();	            	
	    	 if (!originalIds.contains(Id)) {
	    		 newId = Id;
	    		 originalIds.add(newId);
	    	 }
	    }
		
		return newId;
	}

	/**
	 * Removes the panel with a specified panelId from the layout
	 * @param panelId
	 */
	
	public void removeWidget(String panelId) {
		 
		int count = form.getItemCount();
         
         for (int i = 0; i < count; i++ ) {
        	 Component c2 = getItem(i);
        	 String Id = c2.getId();
        	 //LOG.fine ("Getting component " + i + " id is " + Id);
        	 
        	 if (Id.equals(panelId)) {
        		 //LOG.fine ("Found!");
        		 form.remove(form.getWidget(i));
        		 //layout();
        		 break;
        	 }
         }
         
       //LOG.fine ("The panel Id from createNewCombo is " + panelId);	
	}
	
	public StringTrigger getStringTrigger() {
		 StringTrigger strTrigger = new StringTrigger();
		 
		 ArrayList<String> equalValues = getEqualValues();
		 ArrayList<String> unequalValues = getUnequalValues();
		 
		 if (equalValues.size() == 0 && unequalValues.size()==0) return null;
		 
		 else {
			 if (equalValues.size()> 0) strTrigger.setEqualValues(equalValues);
			 if (unequalValues.size()> 0) strTrigger.setUnequalValues(unequalValues);
			 return strTrigger;
		 }	 		 
	 }


	public ArrayList<String> getEqualValues() {
		 ArrayList<String> equalValues = new ArrayList<String>();
		 for (int i = 0; i < equalComboList.size(); i++ ) {
			 StringSensorValue str = (StringSensorValue)equalComboList.get(i).getValue();
			 if (str!= null) equalValues.add(str.getName());
		 }
		 return equalValues;
	 }


	public ArrayList<String> getUnequalValues() {
		 ArrayList<String> unequalValues = new ArrayList<String>();
		 for (int i = 0; i < unequalComboList.size(); i++ ) {
			 StringSensorValue str = (StringSensorValue)unequalComboList.get(i).getValue();
			 if (str!= null) unequalValues.add(str.getName());
		 }
		 return unequalValues;
	 }


	/**
	  * Resize all comboBoxes according to parent window size (from AlertCreator)
	  */
	
	public void passParentWindowSize( int width, int height) {
		 //LOG.fine ("Window width is " + width + " window height is " + height);		 
		 parent_width = width;		
		 
		 int newWidth = parent_width - PLUSBUTTONSIZE;
		 String newWidth1 = Integer.toString(newWidth);
		 
		 for (int i = 0; i < comboList.size(); i++) {
			 comboList.get(i).setWidth(newWidth1);
		 }
		 		 
		 layout();
	 }


}
