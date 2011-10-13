package nl.sense_os.commonsense.client.alerts.create.forms;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.alerts.create.triggers.NumericTrigger;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.viz.data.timeseries.DataPoint;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;

import com.chap.links.client.Graph;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class TrialNumForm extends FormPanel {
	
	private static final Logger LOG = Logger.getLogger(NumTriggerForm.class.getName());	
	
	private LabelField titleLabel;
	private ArrayList<TextBox> boxList;
	private ArrayList<TextField> fieldList;
    private Radio radio1;
    private  Radio radio2;
    private  Radio radio3;
    private  Radio radio4;
    private RadioGroup group;
    
    private TextField box11;
    private TextField box21;
    private TextField box31;
    private TextField box41;
    private TextField box51;
    private TextField box61;
    
    //private JsArray<Timeseries> datan3;
    private JsArray<Timeseries> data;
    private long start;
    private long end;
    private double vstart;
    private double vend;
    private double orig_vstart;
    private double orig_vend;    
    private Date startDate;
    private Date endDate;
    
    private FormPanel graphPanel;
    private Graph.Options graphOpts = Graph.Options.create();
	private Graph alertGraph;
	
	private GraphLine maxLine;
    private GraphLine minLine;    
    private GraphLine insideRangeLine1;
    private GraphLine insideRangeLine2;
    private GraphLine outsideRangeLine1;
    private GraphLine outsideRangeLine2;
    private ArrayList<GraphLine> graphLinesList;
	
	private int parent_width;
	private int parent_height;
	
	private TrialInnerNumForm numForm;
	
	
	public TrialNumForm (List<SensorModel> sensors, long start, long end, boolean subsample, String title) {
	    	super();
	        LOG.setLevel(Level.ALL);	
	        setHeaderVisible(false);
	        setBodyBorder(false);
	        numForm = new TrialInnerNumForm(sensors, start, end, subsample,title);
	        numForm.setParent(this);
	        
	        createTitleLabel();	        
	        initGraphPanel();
	        initControls(); 	        	
	 }

	
	/**
     * A class to create and distinguish between min, max etc. graph lines
     *
     */
    
    class GraphLine {
    	private String name;
    	private boolean active;
    	private double value;
    	
    	public GraphLine(String name) {
    		this.name = name;
    		this.active = false;
    	}
    	
    	public String getName() {
    		return this.name;
    	}
    	
    	public double getValue() {
    		return this.value;
    	}
    	
    	public void setValue(double value) {
    		this.value = value;
    	}
    	
    	public boolean isActive() {
    		return this.active;
    	}
    	  	
    	public void setActive(boolean active) {
    		this.active = active;
    	}
    	
    }
    
    public NumericTrigger getNumericTrigger() {
    	NumericTrigger thresh = new NumericTrigger();
    	ArrayList<Double> valueList = new ArrayList<Double>();
    	String type = "";
    	
    	if (radio1.getValue() == true) {
    		
    		if (!maxLine.isActive()) {
    			LOG.fine ("Oops no max line drawn");
    			return null;
    		}  
    		else {
    			type = "max";
    			double val = maxLine.getValue();
    			valueList.add(val);
    		}
    	}
    	else if (radio2.getValue() == true) {
    		if (!minLine.isActive()) {
    			LOG.fine ("Oops no min line drawn");
    			return null;
    		}  
    		else {		
	    		type =  "min";
	    		double val = minLine.getValue();
	    		valueList.add(val);
    		}
    	}
    	else if (radio3.getValue() == true) {
    		if (!insideRangeLine1.isActive() || !insideRangeLine2.isActive()) {
    			LOG.fine ("Oops one of the inside range lines is missing");
    			return null;
    		}  
    		else {		
	    		type = "inside";
	    		double val1 = insideRangeLine1.getValue();
	    		double val2 = insideRangeLine2.getValue();
	    		valueList.add(val1);
	    		valueList.add(val2);
    		}
    	}
    	else if (radio4.getValue() == true) { 		
    		if (!outsideRangeLine1.isActive() || !outsideRangeLine2.isActive()) {
    			LOG.fine ("Oops one of the outside range lines is missing");
    			return null;
    		}  
    		else {
	    		type = "outside";
	    		double val1 = outsideRangeLine1.getValue();
	    		double val2 = outsideRangeLine2.getValue();
	    		valueList.add(val1);
	    		valueList.add(val2);
    		}
    	}
    	
    	thresh.setType(type);
    	thresh.setValues(valueList);
    	
    	return thresh;
    	
    }
	
	
	private void createTitleLabel() {
		titleLabel = new LabelField("<b>Sensor with Numeric Values</b>");
        titleLabel.setHideLabel(true);
        titleLabel.setStyleName ("titleLabel");

        this.add(titleLabel);
        
	}
	
	
	/**
	 * Create a set of controls on the bottom
	 */
	
	private void initControls() {
	
	    createRadios();	 
	    createTextFields();
	    addTextListeners();
	    addRadioListeners();		            
	    createLines();
	    
	    Grid g1 = new Grid (2, 2);
	    g1.setWidget (0,0, radio1); 
	    g1.setWidget (1,0, radio2); 
	    g1.setWidget (0,1, box11); 
	    g1.setWidget (1,1, box21);
	    
	    Grid g2 = new Grid (2,3);
	    g2.setWidget (0,0, radio3); 
	    g2.setWidget (1,0, radio4); 
	    g2.setWidget (0,1, box31); 
	    g2.setWidget (1,1, box51); 
	    g2.setWidget (0,2, box41); 
	    g2.setWidget (1,2, box61); 
	    
	    HorizontalPanel outerPanel = new HorizontalPanel();
	    outerPanel.setSpacing(10);
	    outerPanel.add(g1);
	    outerPanel.add(g2);
	
	    BorderLayoutData data = new BorderLayoutData(LayoutRegion.SOUTH, 55);
	    //data.setMargins(new Margins(5, 5, 5, 5));
	    
	    add(outerPanel);
	    add(outerPanel, data);
	    layout(); 
	     
	}
	
	/**
	 * Initializes a graph panel.
	 */
	private void initGraphPanel() {
		
		graphPanel = new FormPanel();
		graphPanel.setHeaderVisible(false);
		graphPanel.setBorders(false);
		graphPanel.setBodyBorder(false);		
		
	
		graphOpts.setLegendVisibility(false);
		graphOpts.setHeight(250);
		//graphOpts.setHeight("100%");
		graphOpts.setWidth("100%");
		alertGraph = new Graph();	
		
		graphPanel.add(alertGraph);
		
	
	    // Add the panel to the layout
	    BorderLayoutData layoutData = new BorderLayoutData(LayoutRegion.CENTER);
	    layoutData.setMargins(new Margins(0,0,0,0));	
	    this.add(graphPanel, layoutData);    
	}
	

	
	/**
     * Creates a set of radio buttons
     */
    
    private void createRadios () {
		radio1 = new Radio();  
	    radio1.setBoxLabel("Max threshold");  
	    radio1.setValue(true);
	
	    radio2 = new Radio();  
	    radio2.setBoxLabel("Min threshold");  
	    
	    radio3 = new Radio();  
	    radio3.setBoxLabel("Inside safe range");  
	    
	    radio4 = new Radio();  
	    radio4.setBoxLabel("Outside safe range"); 
	    
	    group = new RadioGroup();
	    group.add(radio1);
	    group.add(radio2);
	    group.add(radio3);
	    group.add(radio4);
	    
	}
    
    @SuppressWarnings("unchecked")
	private void createTextFields() {
    	box11 = new TextField(); 
	    box21 = new TextField();  
	    box31 = new TextField();   
	    box41 = new TextField();  
	    box51 = new TextField();   
	    box61 = new TextField(); 
	    
	    fieldList = new ArrayList<TextField>();	    
	    fieldList.add(box11);
	    fieldList.add(box21);
	    fieldList.add(box31);
	    fieldList.add(box41);
	    fieldList.add(box51);
	    fieldList.add(box61);
	    
	    setEnabled(box11);
	    
	    for (int i = 0; i < fieldList.size(); i++ ) {
	    	TextField field = fieldList.get(i);
	    	formatTextField(field);
	    }
    }

	
	/**
	 * Formats the textField
	 * @param box
	 */

	@SuppressWarnings("unchecked")
	private void formatTextField(TextField field) {
		field.setWidth("42px");
		field.setHeight("20px");
		//field.setStyleName("textBox1");
	}


	/**
	 * Sets all TextBoxes to ReadOnly, EXCEPT the ones in the parameters
	 * @param boxes
	 */
	
	private void setEnabled(TextBox... boxes) {
		
		ArrayList<TextBox> specialBoxes = new ArrayList<TextBox>();
		for (TextBox box: boxes) {
			specialBoxes.add(box);
		}
		
		for (int i = 0; i < boxList.size(); i++ ) {
	    	TextBox box = boxList.get(i);
	    	if (specialBoxes.contains(box)) {
	    		box.setReadOnly(false);
	    	}
	    	
	    	else {
	    		box.setReadOnly (true);
	    		box.setText("");
	    	}
	    }
	}
	
	/**
	 * Sets all TextBoxes to ReadOnly, EXCEPT the ones in the parameters
	 * @param boxes
	 */
	
	@SuppressWarnings("unchecked")
	private void setEnabled(TextField... fields) {
		
		ArrayList<TextField> specialFields = new ArrayList<TextField>();
		for (TextField field: fields) {
			specialFields.add(field);
		}
		
		for (int i = 0; i < fieldList.size(); i++ ) {
			TextField field = fieldList.get(i);
	    	if (specialFields.contains(field)) {
	    		field.setEnabled(true);
	    		field.setAllowBlank(false);
	    	}
	    	
	    	else {
	    		field.setEnabled(false);
	    		field.setAllowBlank(true);
	    		field.setRawValue("");
	    		
	    	}
	    }
	}
	
	@SuppressWarnings("unchecked")
	private Double getFieldValue(ComponentEvent event) {
		String text = null;
		Double value = null;
		
		if (keyEnterOrTab(event)) {	
			TextField sender = (TextField)event.getSource();
			text = sender.getRawValue(); 
			
			if (isNumber(text)) {		    				
    			value = getValue(text);
    			LOG.fine ("Got text " + text + " got value " + value);
			}
		}
		//LOG.fine ("Got text " + text);
		return value;

	}
	
	
	private void addTextListeners() {
		box11.addKeyListener(new KeyListener() {
			 public void componentKeyDown (ComponentEvent event) {				 							 
					 Double value = getFieldValue(event);
					 if (value != null) drawThresholdLine (value, maxLine);	        				        			        			
		    } 
		});	
		
		box21.addKeyListener(new KeyListener() {
			 public void componentKeyDown (ComponentEvent event) {				 							 
					 Double value = getFieldValue(event);
					 if (value != null) drawThresholdLine (value, minLine);	        				        			        			
		    } 
		});	
		
		box31.addKeyListener(new KeyListener() {
			 public void componentKeyDown (ComponentEvent event) {				 							 
					 Double value = getFieldValue(event);					  
					 String text2 = "";
					 text2 = box41.getRawValue();
					 
		    		 if (isNumber(text2)) {		    				
		        			double value2 = getValue(text2);
		        			if (value!= null) drawThresholdLines (value, value2, insideRangeLine1, insideRangeLine2);	        						        				        			
		    		 }	        			
		    		 else if (value!= null) drawThresholdLine (value, insideRangeLine1);	  	        			        			
		    } 
		});	
		
		box41.addKeyListener(new KeyListener() {
			 public void componentKeyDown (ComponentEvent event) {				 							 
					 Double value = getFieldValue(event);					 
					 String text2 = box31.getRawValue();     				
		    		 if (isNumber(text2)) {		    				
		        			double value2 = getValue(text2);
		        			if (value!= null) drawThresholdLines (value, value2, insideRangeLine2, insideRangeLine1);	        						        				        			
		    		 }	        			
		    		 else if (value != null) drawThresholdLine (value, insideRangeLine2);	  	        			        			
		    } 
		});	
		
		box51.addKeyListener(new KeyListener() {
			 public void componentKeyDown (ComponentEvent event) {				 							 
					 Double value = getFieldValue(event);					  
					 String text2 = box61.getRawValue();
					 
		    		 if (isNumber(text2)) {		    				
		        			double value2 = getValue(text2);
		        			if (value!= null) drawThresholdLines (value, value2, outsideRangeLine1, outsideRangeLine2);	        						        				        			
		    		 }	        			
		    		 else if (value!= null) drawThresholdLine (value, outsideRangeLine1);	  	        			        			
		    } 
		});	
		
		box61.addKeyListener(new KeyListener() {
			 public void componentKeyDown (ComponentEvent event) {				 							 
					 Double value = getFieldValue(event);					 
					 String text2 = box51.getRawValue();     				
		    		 if (isNumber(text2)) {		    				
		        			double value2 = getValue(text2);
		        			if (value!= null) drawThresholdLines (value, value2, outsideRangeLine2, outsideRangeLine1);	        						        				        			
		    		 }	        			
		    		 else if (value != null) drawThresholdLine (value, outsideRangeLine2);	  	        			        			
		    } 
		});		
	}
	
	
	   
	/**
	 * Determines if the key down is an enter or tab
	 * @param event
	 * @return
	 */

	public boolean keyEnterOrTab (KeyDownEvent event) {
		int keyCode = event.getNativeKeyCode();
		if (keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_TAB) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Determines if the key down is an enter or tab
	 * @param event
	 * @return
	 */

	public boolean keyEnterOrTab (ComponentEvent event) {
		int keyCode = event.getKeyCode();
		if (keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_TAB) {
			return true;
		}
		
		return false;
	}

	
	/**
	 * Determines if a string contains a numeric value
	 * @param s
	 * @return
	 */
	
	public static boolean isNumber(String s) {
		try {
			Double.parseDouble(s);
		}
		catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * Gets the numeric value from a string
	 * @param text
	 * @return
	 */
	
	public double getValue(String text) {
		double value = Double.parseDouble(text);
		//LOG.fine ("The value is " + value);			
		return value;
	}



	/**
	 * Adds radio button listeners
	 */

	@SuppressWarnings("unchecked")
	private void addRadioListeners() {
		 
	
	    radio1.addListener(Events.OnClick, new Listener() {
	    	
	    	@Override
	    	public void handleEvent(BaseEvent be) {
	    	    
	    	    if (radio1.getValue()== true) {	    	    	
	    	    	setEnabled(box11);
	    	    	removeLines();	    	    	
	    	    }	    	
	    	}});
	    
	    
	    radio2.addListener(Events.OnClick, new Listener() {
	    	
	    	@Override
	    	public void handleEvent(BaseEvent be) {
	    	  
	    	    if (radio2.getValue()== true) {		    	    			    	    	    	    	
	    	    	setEnabled(box21);
	    	    	removeLines();	    	    	
	    	    }		    	   	    	
	    	}});
	    
	    
	    radio3.addListener(Events.OnClick, new Listener() {
	    	
	    	@Override
	    	public void handleEvent(BaseEvent be) {
	    	  
	    	    if (radio3.getValue()== true) {	    	
	    	    	setEnabled(box31, box41);
	    	    	removeLines();
	    	    }		    	   		    	
	    	}});
	    
	    
	    radio4.addListener(Events.OnClick, new Listener() {
	    	
	    	@Override
	    	public void handleEvent(BaseEvent be) {
	    	    
	    	    if (radio4.getValue()== true) {	    	    	
	    	    	setEnabled(box51,box61);
	    	    	removeLines();
	    	    }		    	   		    	
	    	}});
	
	}
	
	
	/**
	 * Creates threshold graph lines
	 */

	public void createLines() {
		maxLine = new GraphLine ("maxLine");
	    minLine = new GraphLine ("minLine");
	    insideRangeLine1 = new GraphLine ("insideRangeLine1");
	    insideRangeLine2 = new GraphLine ("insideRangeLine2");
	    outsideRangeLine1 = new GraphLine ("outsideRangeLine1");
	    outsideRangeLine2 = new GraphLine ("outsideRangeLine2");
	    graphLinesList = new ArrayList<GraphLine>();
	}
	
	/**
	 * Removes all threshold graph lines
	 */
	
	private void removeLines () {
		if (maxLine.isActive()) removeLine (0, maxLine); 
		if (minLine.isActive()) removeLine (0, minLine);
		if (insideRangeLine1.isActive()) removeLine (0, insideRangeLine1); 
		if (insideRangeLine2.isActive()) removeLine (0, insideRangeLine2);
		if (outsideRangeLine1.isActive()) removeLine (0, outsideRangeLine1); 
		if (outsideRangeLine2.isActive()) removeLine (0, outsideRangeLine2);
		vend = orig_vend;
		vstart = orig_vstart; 
		graphOpts.setVerticalStart(orig_vstart);
		graphOpts.setVerticalEnd(orig_vend);
		alertGraph.draw(data,graphOpts);
		
		graphLinesList.clear();
	}
	

	/**
	 * Removes a specified line from the graph
	 * @param line
	 */
	
	private void removeLine (double value, GraphLine line) {
    	if (line.isActive()) {
    		//data.pop();
    		data.setLength(data.length()-1);
    	
			//LOG.fine ("Line " + line.getName() + " removed. Vstart is " + vstart + " vend is " + vend);
						
			alertGraph.draw(data, graphOpts);				
			line.setActive(false);

		}
    }
	
	/**
	 * Draws a threshold line at a specified value
	 * @param value
	 * @param line
	 */

	public void drawThresholdLine (double value, GraphLine line) {
	
		removeLine(value, line);
		
		double newStart = getVerticalStart(value);
		double newEnd = getVerticalEnd(value);
		graphOpts.setVerticalStart(newStart);
		graphOpts.setVerticalEnd(newEnd);
				        			
		insertLine(value, line);
	
	}
	
	/**
	 * Draws two threshold line at specified value range
	 * @param value
	 * @param line
	 */

	public void drawThresholdLines (double value1, double value2, GraphLine line1, GraphLine line2) {
		
		removeLines();

		double newStart = getVerticalStart(value1);
		double newEnd = getVerticalEnd(value1);
		double newStart1 = getVerticalStart(value2);
		double newEnd1 = getVerticalEnd(value2);
		
		if (newStart1 < newStart)
		graphOpts.setVerticalStart(newStart1);
		else graphOpts.setVerticalStart(newStart);
		if (newEnd1 > newEnd) 
		graphOpts.setVerticalEnd(newEnd1);
		else graphOpts.setVerticalEnd(newEnd);
	        			
		insertLine(value1, line1);
		insertLine (value2, line2);
	
	}

	
	private double getVerticalStart (double value) {
		
		double newStart = orig_vstart; 
    	if (value < orig_vstart) {
	    	//LOG.fine ("Vstart is " + vstart + " and the value is " + value);
			double vertRange = orig_vend - value; 
			newStart = value - 0.05 * vertRange;		
    	}
    	//LOG.fine ("Setting start at " + newStart);
		return newStart;
	}
	
	
	private double getVerticalEnd (double value) {
		
		double newEnd = orig_vend;
		
		if (value > orig_vend) {	    		
			double vertRange = value - orig_vstart; 
			newEnd = value + 0.05 * vertRange;			
		}
		
		//LOG.fine ("Setting end at " + newEnd);
		return newEnd;
	}
	
	 /**
     * Draws a horizontal line on the graph at a specified value
     * @param value
     * @param line
     */
    
    private void insertLine (double value, GraphLine line) {
		
		String newString = " {" +
         " \"label\" : \"Activity\"," +
         " \"data\" : [" +
         " {\"date\": " + start + ", \"value\" : " + value + "}," +
         " {\"date\": " + end + ", \"value\" : " + value + "}" +
         " ]" +
         " }";
		
		Timeseries datan0 = JsonUtils.safeEval(newString);
		data.push(datan0);
		
		graphOpts.setLineWidth(3, data.length()-1);
		graphOpts.setLineColor("Blue", data.length()-1);
		alertGraph.draw(data, graphOpts);
		
		line.setActive(true);
		line.setValue(value);
		
	}
    
	
	/**
	  * Resizes the graph according to parent window size (from AlertCreator)
	*/
	
	public void passParentWindowSize( int width, int height) {
		 LOG.fine ("Window width is " + width + " window height is " + height);		 
		 
		 parent_width = width;	
		 parent_height = height;
		 
		 int newWidth = parent_width - 50;
		 int newHeight = parent_height - 200;	 
		 
		 graphOpts.setWidth(newWidth);
		 graphOpts.setHeight(newHeight);
		 
		 alertGraph.draw(data, graphOpts);
		 //LOG.fine("GraphOpts width is " + alertGraph.getWidth());
		 
		 layout();
	 }
	
	public void passData (JsArray<Timeseries> data) {
		int length = data.length();
		this.data = data;
		LOG.fine ("Data length passData is " + length);
		start = Long.MAX_VALUE;
		end = 0;
		vstart = Double.MAX_VALUE;
		vend = -Double.MAX_VALUE;
		
		for (int i = 0; i < length; i++ ) {
			
			Timeseries ts = data.get(i);
			
			double current_vstart = ts.findMin();
			if (current_vstart < vstart) vstart = current_vstart;
			double current_vend = ts.findMax();
			if (current_vend > vend) vend = current_vend;
			orig_vstart = vstart;
			orig_vend = vend;
						
			JsArray<DataPoint> data1 = ts.getData();
			
			startDate = data1.get(0).getTimestamp();
			endDate = data1.get(data1.length()-1).getTimestamp();
						
			for (int j = 0; j < data1.length(); j++) {								
				long timestamp = (long)(data1.get(j).getTime());				
				if (timestamp < start) start = timestamp;
				if (timestamp > end) end = timestamp;					
				//LOG.fine ("Raw date is " + timestamp);
				
				
			}
		}
		
		graphOpts.setVerticalStart(vstart);
		graphOpts.setVerticalEnd(vend);
		graphOpts.setStart(startDate);
		graphOpts.setEnd(endDate);
		
	
		//LOG.fine ("Ts vstart is " + vstart + " Ts vend is " + vend);
		//LOG.fine ("Start is " + start + " end is " + end);


    	alertGraph.draw(data,graphOpts);
    	//LOG.fine ("Tried to draw graph");
	}
	
}
