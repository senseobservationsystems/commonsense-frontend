package nl.sense_os.commonsense.client.viz.panels.map;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.viz.data.timeseries.DataPoint;
import nl.sense_os.commonsense.client.viz.data.timeseries.FloatDataPoint;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;
import nl.sense_os.commonsense.client.viz.panels.VizPanel;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SliderEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SliderField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.maps.client.overlay.PolylineOptions;

public class MapPanel extends VizPanel {

    private static final Logger LOG = Logger.getLogger(MapPanel.class.getName());
    private MapWidget map;
    private DateSlider startSlider;
    private DateSlider endSlider;
    private Timeseries latTimeseries;
    private Timeseries lngTimeseries;
    private Marker startMarker;
    private Marker endMarker;
    private Polyline trace;
    private int traceStartIndex;
    private int traceEndIndex;
    private int  initMaxTime;
    private int currentMinTime;
    private int currentMaxTime;
    
    private int storeStartIndex;
    private int storeEndIndex;
    
    ArrayList<ArrayList<Timeseries>> latLongList;
    ArrayList<Polyline> polyList = new ArrayList<Polyline>();
    ArrayList<Marker> startMarkerList = new ArrayList<Marker>();
    ArrayList<Marker> endMarkerList = new ArrayList<Marker>();
    ArrayList<Integer> traceStartIndexList = new ArrayList<Integer>();
    ArrayList<Integer> traceEndIndexList = new ArrayList<Integer>();
    ArrayList<String> traceColorList = new ArrayList<String>();
    
    
   

    public MapPanel(List<SensorModel> sensors, long start, long end, String title) {
        super();

        setHeading("My map: " + title);
        setLayout(new BorderLayout());
        setId("viz-map-" + title);
        LOG.setLevel(Level.ALL);

        initSliders();
        initMapWidget();

        visualize(sensors, start, end);
    }

    /**
     * Calculates the slider parameters for a set of sensor values.
     * 
     * @param data
     *            JsonValueModels with positional sensor data over time
     */

    
    public final DateTimeFormat format = DateTimeFormat
    .getFormat(PredefinedFormat.DATE_TIME_SHORT);
    
    private String calculDate (DataPoint v) {
    	long mseconds = v.getTimestamp().getTime();
    	String formatDate = format.format(new Date(mseconds));
    	return formatDate;
    }
    
    private String calculDate (int sliderValue) {
    	long mseconds = sliderValue * 1000l;
    	String formatDate = format.format(new Date(mseconds));
    	return formatDate;
    }
    
    
    
    private void calcSliderRange() {
    	
    	// bring all latitude timeseries lists together in one arrayList
    	ArrayList<Timeseries> allLatSeries = new ArrayList<Timeseries>();
    	
    	
    	//DataPoints with minimum and maximum slider values-to-be
    	DataPoint min_i;
        DataPoint max_i;
        
    	for (int m = 0; m < latLongList.size(); m++ ) {
    		Timeseries latitudes = latLongList.get(m).get(0);
    		allLatSeries.add(latitudes);	
    	}
    	
    	int min = Integer.MAX_VALUE;
    	int max = 0;
    	
    	// this is just to initialize it something; cannot find constructor
    	min_i = allLatSeries.get(0).getData().get(0);
    	max_i = allLatSeries.get(0).getData().get(0);

    	for (int n = 0; n < allLatSeries.size(); n++) {
    		
    		JsArray<DataPoint> values = allLatSeries.get(n).getData();
    		//JsArray<DataPoint> values = latTimeseries.getData();
            DataPoint v = values.get(0);

            // was: int min = ..
            int localMin = (int) Math.floor(v.getTimestamp().getTime() / 1000l);
            if (localMin< min) {
            	min = localMin;
            	min_i = v;
            }
            
            v = values.get(values.length() - 1);
            
            // was: int max = ..
            int localMax = (int) Math.ceil(v.getTimestamp().getTime() / 1000l);
            if (localMax > max) {
            	max = localMax;
            	max_i = v;
            	
            }
    	}
    	
    	
        int interval = (max - min) / 25;
        max = min + 25 * interval;


        startSlider.setMinValue(min);
        startSlider.setMaxValue(max);
        startSlider.setIncrement(interval);
        startSlider.disableEvents(true);
        // if you set the value to min, the slider starts with the second value
        // for some reason; so i set it to 0, then it equals min anyway
        startSlider.setValue(0);
        startSlider.enableEvents(true);

        endSlider.setMinValue(min);
        endSlider.setMaxValue(max);
        endSlider.setIncrement(interval);
        endSlider.disableEvents(true);
        // if you set the value to max, the slider starts with the second value
        // for some reason; so i add something to the max, then it equals max anyway
        endSlider.setValue(max + 100000);
        endSlider.enableEvents(true);
        
        //LOG.fine("And the start slider value is " + startSlider.getValue());
        //LOG.fine("Calculate slider range: min is " + min + " max " + max);
        //LOG.fine("The minimum date is " + min_i.getTimestamp().getTime());
    	//LOG.fine("The maximum date is " + max_i.getTimestamp().getTime());
        LOG.fine("The minimum date is " + calculDate(min_i));
    	LOG.fine("The maximum date is " + calculDate(max_i));
        LOG.fine ("The min according to slider is " + startSlider.onFormatValue(min));
        LOG.fine ("The max according to slider is " + endSlider.onFormatValue(max));	
    }

    private void centerMap() {
        final LatLngBounds bounds = trace.getBounds();
        map.setCenter(bounds.getCenter());
        map.setZoomLevel(map.getBoundsZoomLevel(bounds));
    }

    /**
     * This method is called when data is first added to the map. It draws the complete trace on the
     * map, based on the current setting of the sliders.
     */
    private void drawTrace() {
    	traceColorList.add("#FF7F00");
    	traceColorList.add ("#E9967A");
    	String traceColor;
        
    	// clean the map - 
    	// clearOverlays was not commented! did that to keep the first markers in place
        //map.clearOverlays();
        trace = null;

        // get the time window for the trace from the sliders
        int minTime = startSlider.getValue();
        int maxTime = endSlider.getValue();
        initMaxTime = endSlider.getValue();
        currentMinTime = startSlider.getValue();
        currentMaxTime = endSlider.getValue();
        
        for (int l = 0; l < latLongList.size(); l++) {
        	
        traceColor = traceColorList.get(l);
        	
        ArrayList<Timeseries> testing = latLongList.get(l);
        if (!testing.isEmpty()) {
        	
        latTimeseries = latLongList.get(l).get(0);
        lngTimeseries = latLongList.get(l).get(1);
        } 
        
        // get the sensor values
        JsArray<DataPoint> latValues = latTimeseries.getData();
        JsArray<DataPoint> lngValues = lngTimeseries.getData();

        LOG.fine( "Number of points: " + latValues.length());

        // Draw the filtered points.
        if (latValues.length() > 0 && maxTime > minTime) {
            LatLng[] points = new LatLng[latValues.length()];

            traceStartIndex = -1;
            traceEndIndex = -1;
            int lastPoint = -1;
            FloatDataPoint lat;
            FloatDataPoint lng;
            for (int i = 0, j = 0; i < latValues.length(); i++) {
                lat = latValues.get(i).cast();
                lng = lngValues.get(i).cast();

                // timestamp in secs
                long timestamp = lat.getTimestamp().getTime() / 1000;
                // changed condition next line
                if (/*timestamp >= minTime && timestamp < maxTime*/ timestamp!= 0) {
                    // update indices
                    lastPoint = j;
                    traceEndIndex = i;
                    if (-1 == traceStartIndex) {
                        traceStartIndex = i;
                    }
                    // store coordinate
                    LatLng coordinate = LatLng.newInstance(lat.getValue(), lng.getValue());
                    points[j] = coordinate;
                    j++;
                }
            }
            
            traceStartIndexList.add(traceStartIndex);
            traceEndIndexList.add(traceEndIndex);
            
            LOG.fine("traceStartIndex for " + " is " + traceStartIndex + " traceEndIndex is " + traceEndIndex);

            // Add the first marker
            final MarkerOptions markerOptions = MarkerOptions.newInstance();
            startMarker = new Marker(points[0], markerOptions);
            startMarkerList.add(startMarker);
            map.addOverlay(startMarker);
            //LOG.fine("Drawing start marker");
            

            // Add the last marker
            endMarker = new Marker(points[lastPoint], markerOptions);
            endMarkerList.add(endMarker);
            map.addOverlay(endMarker);

            // Draw a track line
            PolylineOptions lineOptions = PolylineOptions.newInstance(false, true);
            trace = new Polyline(points, traceColor, 5, 1, lineOptions);
            polyList.add(trace);
            map.addOverlay(trace);
            LOG.fine("trace vertex count is " + trace.getVertexCount());

        } else {
            LOG.warning("No position values in selected time range");
        }
        LOG.fine("Has drawn " + polyList.size() + " polylines by now");
    }
}

    /**
	 * Create a set of sliders on the bottom, to filter the points to draw according to a time
	 * specified with the sliders.
	 */
	private void initSliders() {
	
	    FormPanel slidersForm = new FormPanel();
	    slidersForm.setHeaderVisible(false);
	    slidersForm.setBorders(false);
	    slidersForm.setBodyBorder(false);
	    slidersForm.setPadding(0);
	    storeStartIndex = 0;
	    storeEndIndex = 0;
	    //currentMinTime = 0;
	    //currentMaxTime = 0;
	    //weirdSlider = false;
	    //initMaxTime = 0;
	
	    Listener<SliderEvent> slideListener = new Listener<SliderEvent>() {
	
	        @Override
	        public void handleEvent(SliderEvent be) {
	            updateTrace();
	        }
	    };
	
	    // create start time slider
	    startSlider = new DateSlider();
	    startSlider.setMessage("{0}");
	    startSlider.setId("viz-map-startSlider");
	    startSlider.addListener(Events.Change, slideListener);
	
	    SliderField startField = new SliderField(startSlider);
	    startField.setFieldLabel("Trace start");
	
	    endSlider = new DateSlider();
	    endSlider.setMessage("{0}");
	    endSlider.setValue(endSlider.getMaxValue());
	    endSlider.setId("viz-map-endSlider");
	    endSlider.addListener(Events.Change, slideListener);
	
	    SliderField endField = new SliderField(endSlider);
	    endField.setFieldLabel("Trace end");
	
	    slidersForm.add(startField, new FormData("-5"));
	    slidersForm.add(endField, new FormData("-5"));
	
	    BorderLayoutData data = new BorderLayoutData(LayoutRegion.SOUTH, 50);
	    data.setMargins(new Margins(0, 5, 5, 5));
	    this.add(slidersForm, data);
	}

	/**
     * Initializes a Google map.
     */
    private void initMapWidget() {

        map = new MapWidget();
        map.setWidth("100%");

        // Add some controls for the zoom level
        map.setUIToDefault();

        // Add the map to the layout
        BorderLayoutData layoutData = new BorderLayoutData(LayoutRegion.CENTER);
        layoutData.setMargins(new Margins(5));
        this.add(map, layoutData);
        
    }
    
    
    
    
    @Override
    protected void onNewData(JsArray<Timeseries> data) {

    	
    	LOG.fine("Total data length equals this: .. " + data.length());
    	
    	//LOG.fine("Appending an array.. " + data.get(0).getLabel());
    	//LOG.fine ("The ID equals.. " + data.get(0).getIdd());
    	//LOG.fine("The last ID equals..." + data.get(data.length()-1).getIdd());
    	
    	// create an ArrayList to store multiple arrayLists, to group the Timeseries
    	// objects by their ID
    	
    	ArrayList<ArrayList<Timeseries>> bigList = new ArrayList<ArrayList<Timeseries>>();
    	

    	//store all the id's that we get
    	
    	ArrayList<Integer> Id_names = new ArrayList<Integer>();
    	
    	// if the ID is new, add it to the id list, create an arraylist for Timeseries with 
    	// that ID, and include that arraylist into the Big Arraylist
    	
    	for (int i = 0; i < data.length(); i++) {
    		int newId = data.get(i).getIdd();
    		if (!Id_names.contains(newId)) { //&& newId!= null) {
    			//LOG.fine ("New Id found " + newId);
    			Id_names.add(newId);	
    			ArrayList<Timeseries> currentList = new ArrayList<Timeseries>();
    			currentList.add(data.get(i));
    			bigList.add(currentList);
    			
    		}
    	// if the ID is not new, add the Timeseries to the first arraylist within the 
        // Big Arraylist which has the same ID
    		
    		else {//if (newId != -1) {
    			for (int j = 0; j < bigList.size(); j++) {
    				ArrayList<Timeseries> currentArray = new ArrayList<Timeseries>();
    				currentArray = bigList.get(j);
    				if (currentArray.get(0).getIdd()== newId){
    					currentArray.add(data.get(i));	
    				}
    			}
    		}
    	}
    	
    	//LOG.fine("BigList size is " + bigList.size());
    	for (int i = 0; i < bigList.size(); i++) {
    		ArrayList<Timeseries> current = bigList.get(i);
    		for (int j = 0; j < current.size(); j++) {
    			int currentId = current.get(j).getIdd();
    			String currentLabel = current.get(j).getLabel();
    			//LOG.fine("Aha we got id " + currentId + "with label " + currentLabel);
    		}
    	}
    	
    	
    	
    	// sort lat/lng data
    	
    	// for every arrayList within the bigList, find lat and long timeseries;
    	// put the pairs in a big arrayList called latLongList
    	
        
       Timeseries series;
        
       latLongList = new ArrayList<ArrayList<Timeseries>>();
        
        for (int i = 0; i < bigList.size(); i++) {
        	ArrayList<Timeseries> curList =bigList.get(i);
        	ArrayList<Timeseries> latLong = new ArrayList<Timeseries>();
        	
        	boolean latSet = false;
    		boolean longSet = false;
    		boolean added = false;
    		
        	for (int j = 0; j < curList.size(); j ++) {
  
        		series = curList.get(j);
        		if (series.getLabel().endsWith("latitude")) {
        		//LOG.fine("Yes!!! latitude found");
                latTimeseries = series;
                
                latLong.add(0, series);
                
                latSet = true;
                
        		} else if (curList.get(j).getLabel().endsWith("longitude")) {
        			//LOG.fine("Yes!!! longitude found");
        			lngTimeseries = series;
        			if (!latLong.isEmpty()) {
        			latLong.add(1,series);
        			}
        			else {
        				latLong.add(series);
        			}
        			longSet = true;
        		}
        		if (latSet == true && longSet == true && added == false) {
        			latLongList.add(latLong);
        			
        			//LOG.fine("LatLong size is " + latLong.size());
        			//LOG.fine("Adding latLong arrayList number " + i);
        			added = true;
        			
        		}
        	}
        }
        
        LOG.fine("The length of latLongList is " + latLongList.size());
        
        //Timeseries ts;
        //for (int i = 0; i < data.length(); i++) {
           // ts = data.get(i);
            
            //LOG.fine("Label " + data.get(i).getLabel() + " ID " + 
            //data.get(i).getIdd());
            
            //LOG.fine("thats what we get" + ts.getLabel());
            
            //JsArray<DataPoint> tsData = ts.getData();
            
            /*for (int j = 0; j < tsData.length(); j++) {
            	String value = tsData.get(j).getRawValue();
            	//if (ts.getLabel().endsWith("latitude")|| ts.getLabel().endsWith("longitude")) {
            	/*Date thedate = tsData.get(j).getTimestamp();
            	Calendar cal = Calendar.getInstance();
            	cal.setTime(thedate);
            	
            	//GMT hour and minute
            	int month = cal.get (Calendar.MONTH) + 1;
            	int datenow = cal.get(Calendar.DATE);
            	int hour = cal.get(Calendar.HOUR_OF_DAY);
            	int min = cal.get(Calendar.MINUTE);*/
            	

            	//LOG.fine("this is " + ts.getLabel() + " and this data point has a value of " + value +
            	//		 " date "+ datenow + "." + month + " time " + hour + ":" + min);
            	//}
           // } */
            
           /* if (ts.getLabel().endsWith("latitude")) {
                latTimeseries = ts;
            } else if (ts.getLabel().endsWith("longitude")) {
                lngTimeseries = ts;
            }*/
       // }

        if (latTimeseries != null && lngTimeseries != null) {
            calcSliderRange();
            drawTrace();
            centerMap();
        }
    }

    private void updateTrace() {
    	
    	 int minTime = startSlider.getValue();
         int maxTime = endSlider.getValue();
         //boolean weirdSlider = false;
         
         int whichSlider = 0; 
         
         if (currentMinTime != minTime) {
         	whichSlider = 1;
         	//LOG.fine("Start value has changed, which slider is: " + whichSlider);
         	
         	currentMinTime = minTime;
         }
         
         else if (currentMaxTime != maxTime) {
         	whichSlider = 2;
         	//LOG.fine("End value has changed, which slider is: " + whichSlider);
         	currentMaxTime = maxTime;
         } else {
         	LOG.warning("cannot determine which slider");
         	return;
         }
         
          if (minTime > maxTime) {
         	//if (whichSlider != 1) newTraceEndIndex = traceStartIndex;
         	//if (whichSlider != 2) newTraceStartIndex = traceEndIndex;
         	//LOG.fine("Ẅeird slider!");// newTraceEndIndex is " + newTraceEndIndex);
         	//weirdSlider = true;
         	//return;
         }
         
        // LOG.fine( "updateTrace ");
    	
    	for (int k = 0; k < latLongList.size(); k++) {
    		latTimeseries = latLongList.get(k).get(0);
    		lngTimeseries = latLongList.get(k).get(1);
    		Polyline trace = polyList.get(k);
    		//LOG.fine("Trying to get PolyList.. " + k);
    		startMarker = startMarkerList.get(k);
    		endMarker = endMarkerList.get(k);
    		traceStartIndex = traceStartIndexList.get(k);
    		traceEndIndex = traceEndIndexList.get(k);
    		
    		LOG.fine("traceStartIndex for " + k + " is " + traceStartIndex + " traceEndIndex is " + traceEndIndex);

        if (null == trace || false == trace.isVisible()) {
            LOG.fine("updateTrace skipped: trace is not shown yet");
            return;
        }

        // get the sensor values
        JsArray<DataPoint> latValues = latTimeseries.getData();
        JsArray<DataPoint> lonValues = lngTimeseries.getData();
 
        
     // find the start end end indices of the trace in the sensor data array
        int newTraceStartIndex = 0, newTraceEndIndex = latValues.length() - 1;
        long timestamp;
        boolean done = false;
        
        for (int i = 0; i < latValues.length(); i++) {
            // get timestamp
            timestamp = latValues.get(i).getTimestamp().getTime() / 1000;
//changed first condition from >minTime
            
            
            if (i == latValues.length()- 1 && timestamp < minTime) {
            	// start index is not changed
            	// end index is not changed
            	newTraceStartIndex = latValues.length() -1;
            	newTraceEndIndex = latValues.length() -1 ;
            }
            
          
            if (whichSlider != 1) {
            	// start index is not changed
                // LOG.fine("start index is not changed: " + traceStartIndex);
            	newTraceStartIndex = traceStartIndex;
            } else if (timestamp > minTime && newTraceStartIndex == 0 && done == false) {
                // this is the first index with start of visible range
                newTraceStartIndex = i;  
                LOG.fine("changing newTraceStartIndex to " + i);
                done = true;
                
               // LOG.fine("At index " + i + "we're inside the visible range");
                storeStartIndex = newTraceStartIndex;
                
            }
            if (whichSlider != 2) {
            	// end index is not changed
                // LOG.fine("end index is not changed: " + traceEndIndex);
            	newTraceEndIndex = traceEndIndex;
            	
            } else if (timestamp > maxTime + 10) {
                // this is the first index after the end of visible range
            	// + 10 because if the values are equal for us, the computer 
            	// thinks the timestamp is a bit bigger
                LOG.fine ("timestamp is " + calculDate((int)timestamp) + "maxTime is " + calculDate(maxTime));
                
            	LOG.fine("Border reached: NewTraceEndIndex is " + newTraceEndIndex + "NewTraceStartIndex is " + 	
                newTraceStartIndex + "changing newTraceEndIndex to " + (i-1));
                newTraceEndIndex = i-1;
                storeEndIndex = newTraceEndIndex;
                break;
            } 
        }
        
 
       
        if (newTraceStartIndex > newTraceEndIndex) { 
        	
            //LOG.warning("Start index of trace is larger than end index?!");
            if (whichSlider != 2) {
            	//LOG.fine("First slider was moved too far");
            	newTraceStartIndex = newTraceEndIndex;
            	
            }
            else if (whichSlider!= 1) {
            	//LOG.fine("Second slider was moved too far");
            	newTraceEndIndex = newTraceStartIndex;
            }
            //return;
        }   
        
       LOG.fine("NewTraceStartIndex for " + k + " is " + newTraceStartIndex + " NewTraceEndIndex is " + newTraceEndIndex);
       
        // add vertices at START of trace if newTraceStart < traceStartIndex
        if (newTraceStartIndex < traceStartIndex) {
            
            FloatDataPoint lat;
            FloatDataPoint lon;
           // LOG.fine( "Add " + (traceStartIndex - newTraceStartIndex) + " vertices at start");
            for (int i = traceStartIndex - 1; i >= newTraceStartIndex; i--) {
                lat = latValues.get(i).cast();
                lon = lonValues.get(i).cast();
                if (whichSlider != 2) {
                trace.insertVertex(0, LatLng.newInstance(lat.getValue(), lon.getValue()));
                }
            }
                
        }

        // delete vertices at START of trace if newTraceStart > traceStartIndex
        if (newTraceStartIndex > traceStartIndex) {
           // LOG.fine( "Delete " + (newTraceStartIndex - traceStartIndex) +
           // " vertices at start");
            for (int i = traceStartIndex; i < newTraceStartIndex; i++) {
            	//if (trace.getVertexCount()> 1)
                trace.deleteVertex(0);
            }
        }

        // update start marker
        FloatDataPoint startLat = latValues.get(newTraceStartIndex).cast();
        FloatDataPoint startLon = lonValues.get(newTraceStartIndex).cast();
        LatLng startCoordinate = LatLng.newInstance(startLat.getValue(), startLon.getValue());
        startMarker.setLatLng(startCoordinate);
        startMarkerList.get(k).setLatLng(startCoordinate);

        // add vertices at END of trace if newTraceEnd > traceEndIndex
        if (newTraceEndIndex > traceEndIndex) {
            //LOG.fine( "Add " + (newTraceEndIndex - traceEndIndex) + " vertices at end");
            FloatDataPoint lat;
            FloatDataPoint lon;
            int vertexCount = trace.getVertexCount();
            for (int i = traceEndIndex + 1; i <= newTraceEndIndex; i++) {
                lat = latValues.get(i).cast();
                lon = lonValues.get(i).cast();
                trace.insertVertex(vertexCount,
                LatLng.newInstance(lat.getValue(), lon.getValue()));
                vertexCount++;
            }
        }

        // delete vertices at END of trace if newTraceEnd < traceEndIndex
         if (newTraceEndIndex < traceEndIndex) {
        	//int howManyToDelete = traceEndIndex - newTraceEndIndex;
        	//LOG.fine( "Delete " + (traceEndIndex - newTraceEndIndex) + " vertices at end");
            int currentCount = trace.getVertexCount();
        	//if (trace.getVertexCount() > 1)
            /*for (int i = 0; i < howManyToDelete -1; i++) {
            	trace.deleteVertex (traceEndIndex - i);
            }*/
            for (int i = traceEndIndex; i > newTraceEndIndex; i--) {
            	//if (trace.getVertexCount() > 1) {
            	
            	//while (trace.getVertexCount()== currentCount) {
            	trace.deleteVertex(currentCount-1);
            	//LOG.fine("trying to delete vertex " + currentCount);
            	currentCount--;
            	
            	//}
            	//LOG.fine("done deleting vertex " + i );

            	//}
            }
        }
        
        //LOG.fine( "old start: " + traceStartIndex + ", old end: " + traceEndIndex);
       // LOG.fine( "new start: " + newTraceStartIndex + ", new end: " + newTraceEndIndex +
        		//"vertex count is " + trace.getVertexCount());
        //LOG.fine("old trace start index is" + traceStartIndex + ", oldtrace end index is " + 
        		//traceEndIndex);
         int vertCount = trace.getVertexCount();
         LOG.fine ("vertex count is " + vertCount);
        
        // update end marker
        FloatDataPoint endLat = latValues.get(newTraceEndIndex).cast();
        FloatDataPoint endLon = lonValues.get(newTraceEndIndex).cast();
        LatLng endCoordinate = LatLng.newInstance(endLat.getValue(), endLon.getValue());
        endMarker.setLatLng(endCoordinate);
        endMarkerList.get(k).setLatLng(endCoordinate);

        // update trace indexes
        traceStartIndexList.set(k, newTraceStartIndex);
        traceEndIndexList.set(k, newTraceEndIndex);
        traceStartIndex = newTraceStartIndex;
        traceEndIndex = newTraceEndIndex;
        
    }
    } // close for latLongList
}