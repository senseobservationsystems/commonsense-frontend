package nl.sense_os.commonsense.client.viz.panels.map;

import java.util.ArrayList;
import java.math.*;
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
	private static final double R = 6371;
    private MapWidget map;
    private DateSlider startSlider;
    private DateSlider endSlider;
    //private Timeseries latTimeseries;
    //private Timeseries lngTimeseries;
    private Marker startMarker;
    private Marker endMarker;
    private Polyline trace;
    private int traceStartIndex;
    private int traceEndIndex;
    private int currentMinTime;
    private int currentMaxTime;
    
    // store all the id's that we get
    ArrayList<Integer> Id_names = new ArrayList<Integer>();
    
    // create an arrayList with timeseries for all the IDs
    ArrayList<ArrayList<Timeseries>> bigList;
    // create an arrayList to store multiple arrayLists, with "bad points" to be discarded for each ID
    ArrayList <ArrayList<Integer>> bigBadPoints = new ArrayList<ArrayList<Integer>>();
    // create two arrayLists to store all good lat-long pairs and timestamps for all the IDs
    ArrayList <ArrayList<ArrayList<Float>>> allGoodPoints = new ArrayList <ArrayList<ArrayList<Float>>>();
    
    ArrayList <ArrayList<Float>> allGoodLat = new ArrayList<ArrayList<Float>>();
    ArrayList <ArrayList<Float>> allGoodLon = new ArrayList<ArrayList<Float>>();
    ArrayList <ArrayList<Long>> allGoodTimestamps = new ArrayList<ArrayList<Long>>();
     
    ArrayList<Polyline> polyList = new ArrayList<Polyline>();
    ArrayList<Marker> startMarkerList = new ArrayList<Marker>();
    ArrayList<Marker> endMarkerList = new ArrayList<Marker>();
    ArrayList<Integer> traceStartIndexList = new ArrayList<Integer>();
    ArrayList<Integer> traceEndIndexList = new ArrayList<Integer>();
    ArrayList<String> traceColorList = new ArrayList<String>();

    public MapPanel(List<SensorModel> sensors, long start, long end, boolean subsample, String title) {
        super();

        setHeading("My map: " + title);
        setLayout(new BorderLayout());
        setId("viz-map-" + title);
        LOG.setLevel(Level.ALL);
        initSliders();
        initMapWidget();

        visualize(sensors, start, end, subsample);
    }

    /**
     * Calculates the slider parameters for a set of sensor values.
     * 
     * @param data
     *            JsonValueModels with positional sensor data over time
     */

    public final DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);

    private String calculDate(DataPoint v) {
        long mseconds = v.getTimestamp().getTime();
        String formatDate = format.format(new Date(mseconds));
        return formatDate;
    }

    private String calculDate(int sliderValue) {
        long mseconds = sliderValue * 1000l;
        String formatDate = format.format(new Date(mseconds));
        return formatDate;
    }

    private void calcSliderRange() {

        
        // Points with minimum and maximum slider values-to-be 
        
        int min = Integer.MAX_VALUE;
        int max = 0;

        
        for (int n = 0; n < bigList.size(); n++) {
        	
        	ArrayList<Long> curTimestamps = allGoodTimestamps.get(n);
        	
        	int localMin = (int) Math.floor(curTimestamps.get(0) / 1000l);
        	int localMax = (int) Math.ceil(curTimestamps.get(curTimestamps.size() -1) / 1000l);
        	
        	if (localMin < min) {
                min = localMin;               
        	}

        	if (localMax > max) {
                max = localMax;    
        	}      
        }

        //LOG.fine("Calculate slider range : max found is " + max);
        int interval = (max - min) / 25;
        //max = min + 25 * interval;
        
        //LOG.fine("Calculate slider range: max assigned is " + max);

        startSlider.setMinValue(min);
        startSlider.setMaxValue(max);
        startSlider.setIncrement(interval);
        startSlider.disableEvents(true);
        startSlider.setValue(min - 100000);
        startSlider.enableEvents(true);
        // if you set the value to min, the slider starts with the second value
        // for some reason; so i set it to min - 100000, then it equals min anyway       

        endSlider.setMinValue(min);
        endSlider.setMaxValue(max);
        endSlider.setIncrement(interval);
        endSlider.disableEvents(true);
        endSlider.setValue(max + 100000);
        endSlider.enableEvents(true);
        // if you set the value to max, the slider starts with the second value
        // for some reason; so i add something to the max, then it equals max anyway
 
        LOG.fine("Minimum date found is " + calculDate(min) + " maximum date found is " + calculDate(max));
        //LOG.fine("The min according to slider is " + startSlider.onFormatValue(min) + "the max according to slider is " + endSlider.onFormatValue(max));
        //LOG.fine("And the start slider value is " + startSlider.getValue() + " " + calculDate(startSlider.getValue()));
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
        traceColorList.add("#E9967A");
        String traceColor;

        // clean the map -
        // clearOverlays was not commented! did that to keep the first markers in place
        // map.clearOverlays();
        trace = null;

        // get the time window for the trace from the sliders
        int minTime = startSlider.getValue();
        int maxTime = endSlider.getValue();
        
        //LOG.fine("Initial MinTime is " + minTime + " " + calculDate(minTime) + " maxTime is " + maxTime + " " + calculDate(maxTime));
        currentMinTime = startSlider.getValue();
        currentMaxTime = endSlider.getValue();

        for (int l = 0; l < bigList.size(); l++) {

            traceColor = traceColorList.get(l);          

            // get the sensor values 
            ArrayList<Float> latValues = allGoodLat.get(l);           
            ArrayList<Float> lonValues = allGoodLon.get(l);
            
            LOG.fine("Number of points to draw: " + latValues.size());
            //LOG.fine("BadPoints length for " + Id_names.get(l) + "is " + bigBadPoints.get(l).size());            

            // Draw the filtered points.
            if (latValues.size() > 0 && maxTime > minTime) {
                LatLng[] points = new LatLng[latValues.size()];
                
                traceStartIndex = -1;
                traceEndIndex = -1;
                int lastPoint = -1;
                double lat;
                double lng;
                
                for (int i = 0, j = 0; i < latValues.size(); i++) {
                   
                	lat = latValues.get(i);
                	lng = lonValues.get(i);

                    // timestamp in secs
                	long timestamp = allGoodTimestamps.get(l).get(i)/1000;
                	//LOG.fine ("The timestamp for point " + i + " is " + timestamp);
                                                          
                    if (/* timestamp >= minTime && timestamp < maxTime */timestamp != 0) {
                        // update indices
                        lastPoint = j;
                        traceEndIndex = i;
                        if (-1 == traceStartIndex) {
                            traceStartIndex = i;
                        }
                        // store coordinate
                        LatLng coordinate = LatLng.newInstance(lat, lng);
                        points[j] = coordinate;
                        j++;
                    }
                }

                traceStartIndexList.add(traceStartIndex);
                traceEndIndexList.add(traceEndIndex);

//               LOG.fine("traceStartIndex for " + " is " + traceStartIndex + " traceEndIndex is " + traceEndIndex);

                // Add the first marker
                final MarkerOptions markerOptions = MarkerOptions.newInstance();
                startMarker = new Marker(points[0], markerOptions);
                startMarkerList.add(startMarker);
                map.addOverlay(startMarker);
                // LOG.fine("Drawing start marker");

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
    
    private int filterPoints(ArrayList<ArrayList<Float>> latLongPoints, ArrayList<Timeseries> currentArray, int r, int goodStart) {
    	
    	// calculate distance in km between adjacent points of the given ID, and select out only good points
    	
    	// create an arrayList to store lat-long pairs with only "good points" for a given ID
		// create an arrayList of "bad points" indices that have to be discarded for a given ID
    	ArrayList <ArrayList<Float>> IdGoodPoints = new ArrayList<ArrayList<Float>>();
		ArrayList<Integer> badPoints = new ArrayList<Integer>();
		
		
		// goodStart is the first "good point" that we try; if function does not succeed, goodStart+1 in onNewData
		// add the points before the goodStart to the badPoints arrayList
		for (int s = 0; s < goodStart; s++ ) {
			badPoints.add(s);
		}
		
		// create three arrayLists to store latitudes, longitudes and timestamps of only "good points"
		ArrayList<Float> IdGoodLat = new ArrayList<Float>();
		ArrayList<Float> IdGoodLon = new ArrayList<Float>();
		ArrayList <Long> IdGoodTimestamps = new ArrayList<Long>();				
	
		// initialize latitude and longitude values to the goodStart				
		double latit = latLongPoints.get(goodStart).get(0);
		double longit = latLongPoints.get(goodStart).get(1);
		long pointTimestamp = currentArray.get(0).getData().get(0).getTimestamp().getTime();
		LOG.fine("goodStart is "+ goodStart + " latit is " + latit + " longit is " + longit);
		
		double radLongit = Math.toRadians(longit);
		double radLatit = Math.toRadians(latit);
						
		// store lat-long coordinates and timestamps as a "good" point
		ArrayList<Float> firstGoodPoint = new ArrayList<Float>();
		
		IdGoodLat.add(new Float (latit));
		IdGoodLon.add(new Float(longit));
		IdGoodTimestamps.add(pointTimestamp);
		
		firstGoodPoint.add(new Float (latit));
		firstGoodPoint.add(new Float (longit));	
		IdGoodPoints.add(firstGoodPoint);		
		
		//LOG.fine("IdGoodPoints size is " + IdGoodPoints.size());
		
					
		for (int p = goodStart + 1; p < latLongPoints.size(); p++ ) {

			double newLatit = latLongPoints.get(p).get(0);
			double newLongit = latLongPoints.get(p).get(1);
			long newPointTimestamp = currentArray.get(0).getData().get(p).getTimestamp().getTime();
			
			double radNewLongit = Math.toRadians(newLongit);
			double radNewLatit = Math.toRadians(newLatit);
			
			// distance in km between two points
			double distance = Math.acos(Math.sin(radLatit)*Math.sin(radNewLatit) + 
					Math.cos(radLatit)*Math.cos(radNewLatit) * 
					Math.cos(radNewLongit-radLongit)) * R;
			
			// time difference between the two points
			long timeDifference = newPointTimestamp - pointTimestamp;
			double hourDifference = timeDifference * 2.77777778 * 0.0000001;
					
			// speed in km/h			
			double speed = -1; 
			if (hourDifference != 0) {
				speed = distance / hourDifference; 
				} 
			else 
				LOG.fine ("distance equals zero");
			
				
			if (speed > 350 && distance > 25 /* && hourDifference < 2*/ ) {
				
				Integer newBadPoint = new Integer(p);
				badPoints.add(newBadPoint);
				String pointDate = calculDate(currentArray.get(0).getData().get(p));
				String prevPointDate = calculDate (currentArray.get(0).getData().get(p -1));
				
				LOG.fine ("\nThere is nothing on earth that can move so fast!" + 
						"\ndistance is " + distance + "hourDifference is " + hourDifference + "speed is " + speed + " date is " + pointDate +
    					"\ncomparing timestamp " + pointDate + " to " + prevPointDate +
    					"\npoint " + p + " of " + Id_names.get(r) + " will be discarded" +
						" its newLatit is " + newLatit + " newLongit is " + newLongit);
				
				
			} else {
				
				// the following points will be checked against this point
				latit = newLatit;
				longit = newLongit;
				pointTimestamp = newPointTimestamp;
				
				// store lat-long coordinates and timestamp as a "good Point"
				ArrayList<Float> goodPoint = new ArrayList<Float>();
				
				IdGoodLat.add(new Float(newLatit));
				IdGoodLon.add(new Float(newLongit));
				IdGoodTimestamps.add(newPointTimestamp);
				
				goodPoint.add(new Float (newLatit));
				goodPoint.add(new Float (newLongit));
				IdGoodPoints.add(goodPoint);
				
//				test for weird Qatar latitudes				
//				if (newLatit < 35)
//				{
//				LOG.fine("point is " + p + " latit is " + latit + " longit is " + longit + 
//						" newLatit is " + newLatit + " newLongit is " + newLongit + 
//						" speed is " + speed + " distance is " + distance + " pointDate is " + pointDate);
//				}
			
			}
		} 
		
		
		
		// succeeded or not? are there more good points than bad points?
		
		int filterSucceeded = -1;
		if (IdGoodPoints.size()> badPoints.size()) {
			
			filterSucceeded = 1;
			
			// add the "good" and "bad" points to the global lists
			bigBadPoints.add(badPoints);
			allGoodLat.add(IdGoodLat);
			allGoodLon.add(IdGoodLon);
			allGoodPoints.add(IdGoodPoints);
			allGoodTimestamps.add(IdGoodTimestamps);
			
//			for (int j = 0; j < IdGoodPoints.size(); j++) {
//				double testlat = IdGoodPoints.get(j).get(0);
//				double testlon = IdGoodPoints.get(j).get(1);
//				if (testlat < 35)
//				LOG.fine("good point " + j + " " + testlat + " " + testlon);
//			}
			
//			for (int m = 0; m < badPoints.size(); m ++) {
//				int testBadPoint = badPoints.get(m);
//				LOG.fine(" bad point " + testBadPoint);
//			}
			
			
		}
		else filterSucceeded = 0;
		
		LOG.fine("badPoints length for " + Id_names.get(r) + " is " + badPoints.size());
		LOG.fine("goodPoints length for " + Id_names.get(r) + " is " + IdGoodPoints.size());
		
		return filterSucceeded;
	} // close for current ID
    

    @Override
    protected void onNewData(JsArray<Timeseries> data) {

        LOG.fine("Total data length equals this: .. " + data.length());

        // LOG.fine("Appending an array.. " + data.get(0).getLabel());
        // LOG.fine ("The ID equals.. " + data.get(0).getIdd());
        // LOG.fine("The last ID equals..." + data.get(data.length()-1).getIdd());
        // create an ArrayList to store multiple arrayLists, to group the Timeseries
        // objects by their ID

        bigList = new ArrayList<ArrayList<Timeseries>>();

        // if the ID is new, add it to the id list, create an arraylist for Timeseries with
        // that ID, and include that arraylist into the Big Arraylist

        for (int i = 0; i < data.length(); i++) {
            int newId = data.get(i).getIdd();
            if (!Id_names.contains(newId)) { // && newId!= null) {
                // LOG.fine ("New Id found " + newId);
                Id_names.add(newId);
                ArrayList<Timeseries> currentList = new ArrayList<Timeseries>();
                currentList.add(data.get(i));
                bigList.add(currentList);

            }
         // if the ID is not new, add the Timeseries to the first arraylist within the
         // Big Arraylist which has the same ID

            else {
                for (int j = 0; j < bigList.size(); j++) {
                    ArrayList<Timeseries> currentArray = new ArrayList<Timeseries>();
                    currentArray = bigList.get(j);
                    if (currentArray.get(0).getIdd() == newId) {
                        currentArray.add(data.get(i));
                    }
                }
            }
        }
        
        LOG.fine("The biglist size is "+ bigList.size());
        
        
        
        // for every ID in the bigList, get the lat and long timeseries, and check the distance between points            
       
         for (int r = 0; r < bigList.size(); r++) {
       
        	// get all the Timeseries for a given ID
        	ArrayList<Timeseries> currentArray = bigList.get(r);       	
        	LOG.fine ("LOOOKKK currentArray size is " + currentArray.size());
        	
        	ArrayList<Float> latPoints = new ArrayList<Float>();
    		ArrayList<Float> lonPoints = new ArrayList<Float>();
    		boolean latitudeFound = false;
    		boolean longitudeFound = false;
    		   		
	
    		// for each ID, go through all the Timeseries and check if they are latitude/longitude
        	for (int s = 0; s < currentArray.size(); s++ ) {
        		
        		Timeseries curTimeseries = currentArray.get(s);
        		
        		if (curTimeseries.getLabel().endsWith("latitude")) {
        			JsArray<DataPoint> currentLatValues = curTimeseries.getData();
        			//LOG.fine ("Latitude array found");
        			
        			for (int k = 0; k < currentLatValues.length(); k++ ) {
        				FloatDataPoint lat = currentLatValues.get(k).cast();
        				float latValue = new Float(lat.getValue());
        				latPoints.add(latValue);
        				latitudeFound = true;
        				//LOG.fine("the latitude equals " + latValue);
        			}
        		} 
        		
        		else if (curTimeseries.getLabel().endsWith("longitude")) {        			
        			JsArray<DataPoint> currentLonValues = curTimeseries.getData();
        			
        			for (int k = 0; k < currentLonValues.length(); k++ ) {
        				FloatDataPoint lon = currentLonValues.get(k).cast();
        				float lonValue = new Float (lon.getValue());
        				lonPoints.add(lonValue);
        				longitudeFound = true;
        				//LOG.fine("the longitude equals " + lonValue);
        			}
        		} 
        		
        		if (latitudeFound == true && longitudeFound == true) {
           		 
//        			LOG.fine ("The length of latPoints for " + r + " is " + latPoints.size() + 
//        					" The length of lonPoints  for " + r + " is " + lonPoints.size());
        			break;        			
        		}  
			
        	} // now we created the latitude and longitude arrayLists, latPoints and lonPoints, for this ID

        	// make an arrayList to store lat-long pairs for this ID
    		ArrayList<ArrayList<Float>> latLongPoints = new ArrayList<ArrayList<Float>>();
    		int latLength = latPoints.size(); 
    		
    		// add the pair to the arrayList of latLong pairs
    		for (int n = 0; n < latLength; n++) {
    			ArrayList<Float> tempList = new ArrayList<Float>();
    			tempList.add(latPoints.get(n));
    			tempList.add(lonPoints.get(n));
    			latLongPoints.add(tempList);

    		} // latLongPoints contains all lat-long pairs for this ID
    		
    		LOG.fine ("latLongPoints length for " + Id_names.get(r) + " is " + latLongPoints.size());
    		
    		
    		// we try to filter points, starting with point 0, and see if we get more good points than bad.. otherwise we start from the next point.. and so on
			if (latLongPoints.size() > 1) {	
						
				int goodStart = 0;
				boolean points_done = false;
								
				while (points_done == false) {
					
					int filter_succeeded = filterPoints (latLongPoints, currentArray, r, goodStart);
					
					if (filter_succeeded == 1) {
						LOG.fine ("Points filtering succeeded!");
						points_done = true;
						break;
					}
					
					else {
						if (goodStart == latLongPoints.size()) {
						
							LOG.fine ("Reached the end of the array and still didnt find any sensible data");
							break;
							}
						
						else goodStart++;
					}
				}
								
				
        	} // done with current ID 
 	
         } // close for bigList, the entire list of IDs    	                 
		
        if (!allGoodPoints.isEmpty()/*latTimeseries != null && lngTimeseries != null*/) {
            calcSliderRange();
            drawTrace();
            centerMap();
        }   			
    }

    private void updateTrace() {

        int minTime = startSlider.getValue();     
        int maxTime = endSlider.getValue();
        //LOG.fine("MinTime is " + minTime + " " + calculDate(minTime) + " maxTime is " + maxTime + " " + calculDate(maxTime));
        

        int whichSlider = 0;

        if (currentMinTime != minTime) {
            whichSlider = 1;
            // LOG.fine("Start value has changed, which slider is: " + whichSlider);
            currentMinTime = minTime;
        }

        else if (currentMaxTime != maxTime) {
            whichSlider = 2;
            // LOG.fine("End value has changed, which slider is: " + whichSlider);
            currentMaxTime = maxTime;
        } else {
            LOG.warning("cannot determine which slider");
            return;
        }

        
        // LOG.fine( "updateTrace ");

        for (int k = 0; k < bigList.size(); k++) {
            
            Polyline trace = polyList.get(k);
            startMarker = startMarkerList.get(k);
            endMarker = endMarkerList.get(k);
            traceStartIndex = traceStartIndexList.get(k);
            traceEndIndex = traceEndIndexList.get(k);
            
// 			LOG.fine("Trying to get PolyList.. " + k);

//          LOG.fine("traceStartIndex for " + k + " is " + traceStartIndex + " traceEndIndex is "
//                    + traceEndIndex);

            if (null == trace || false == trace.isVisible()) {
                LOG.fine("updateTrace skipped: trace is not shown yet");
                return;
            }

            // get the sensor values
            ArrayList<Float> latValues = allGoodLat.get(k);
            ArrayList<Float> lonValues = allGoodLon.get(k);
            
            // find the start end end indices of the trace in the sensor data array
            int newTraceStartIndex = 0, newTraceEndIndex = latValues.size() - 1;
            int timestamp;
            boolean done = false;
            boolean done_end = false;

            for (int i = 0; i < latValues.size(); i++) {
                
            	timestamp = (int) (allGoodTimestamps.get(k).get(i)/1000l);
//            	LOG.fine ("Timestamp is " + timestamp + " " + calculDate((int)timestamp) + " minTime is " + minTime + " " + calculDate (minTime) +
//            			" maxTime is " + maxTime + " " + calculDate (maxTime));
                //timestamp = latValues.get(i).getTimestamp().getTime() / 1000;              
            	
                if (i == latValues.size() - 1 && timestamp < minTime) {
                    // start index is not changed
                    // end index is not changed
                    newTraceStartIndex = latValues.size() - 1;
                    newTraceEndIndex = latValues.size() - 1;
                    //LOG.fine(" end index is not changed: " + newTraceEndIndex);
                } 

                if (whichSlider != 1) {
                    // start index is not changed
                    // LOG.fine("start index is not changed: " + traceStartIndex);
                    newTraceStartIndex = traceStartIndex;
                   
                } else if (i == 0 && timestamp == minTime) {
                	// if whichSlider is 1 and it is moved back to the start, change the startIndex to 0
                	//LOG.fine ("Moved back to the start: setting traceStartIndex to 0");
                	newTraceStartIndex = 0;
                	done = true;
                	
            	} else if (whichSlider != 2 && i == latValues.size() -1 && timestamp == minTime) {
            		newTraceStartIndex = latValues.size()-1;
            		//LOG.fine ("Moved start slider to the end; change the start index");
            		done = true;
            	}
                               
                else if (whichSlider != 2 && timestamp > minTime && newTraceStartIndex == 0 && done == false) {
                    // this is the first index with start of visible range
                    newTraceStartIndex = i; 
                    //LOG.fine("changing newTraceStartIndex to " + i);
                    done = true;
                    // LOG.fine("At index " + i + "we're inside the visible range");                    
                }
    
                if (whichSlider != 2) {
                    // end index is not changed
                    // LOG.fine("end index is not changed: " + traceEndIndex);
                    newTraceEndIndex = traceEndIndex;

                } else if (i == latValues.size() - 1 && timestamp == maxTime) {
                	// if whichSlider is 2 and it is moved back to the end, change the endIndex to latValues.size - 1        	
                	done_end = true;
                	//LOG.fine ("Moved back to the end: setting TraceEndIndex to " + (latValues.size()-1));
                	
                } else if (whichSlider != 1 && i == 0 && timestamp == maxTime) {
                	newTraceEndIndex = 0;
                	done_end = true;
                	//LOG.fine ("Moved back to the start with the second slider: setting TraceEndIndex to 0"); 	
                }
                
                else if (whichSlider != 1 && timestamp > maxTime && done_end == false) {
                    // this is the first index after the end of visible range                    
                    newTraceEndIndex = i - 1;
                    done_end = true;
//                  LOG.fine("time is " + calculDate((int) timestamp) + " maxTime is " + calculDate(maxTime));                  
//                  LOG.fine("timestamp is " + timestamp + " maxTime is " + maxTime);
//                  LOG.fine("Border reached: NewTraceEndIndex is " + newTraceEndIndex
//                            + " NewTraceStartIndex is " + newTraceStartIndex
//                            + " changing newTraceEndIndex to " + (i - 1));
                    break;
                }
            }

            if (newTraceStartIndex > newTraceEndIndex) {
                // LOG.warning("Start index of trace is larger than end index?!");
                if (whichSlider != 2) {
                    // LOG.fine("First slider was moved too far");
                    newTraceStartIndex = newTraceEndIndex;
                } else if (whichSlider != 1) {
                    // LOG.fine("Second slider was moved too far");
                    newTraceEndIndex = newTraceStartIndex;
                }
                // return;
            }
            //LOG.fine("NewTraceStartIndex for " + k + " is " + newTraceStartIndex + " NewTraceEndIndex is " + newTraceEndIndex);
              
            
            // add vertices at START of trace if newTraceStart < traceStartIndex
            if (newTraceStartIndex < traceStartIndex) {           	
            	double lat;
                double lon;
                // LOG.fine( "Add " + (traceStartIndex - newTraceStartIndex) + " vertices at start");
                for (int i = traceStartIndex - 1; i >= newTraceStartIndex; i--) {  
                    lat = latValues.get(i);
                    lon = lonValues.get(i);  
                    if (whichSlider != 2) {                   	
                        trace.insertVertex(0, LatLng.newInstance(lat, lon));
                    }
                }
            }

            // delete vertices at START of trace if newTraceStart > traceStartIndex
            if (newTraceStartIndex > traceStartIndex) {
                // LOG.fine( "Delete " + (newTraceStartIndex - traceStartIndex) + " vertices at start");
                for (int i = traceStartIndex; i < newTraceStartIndex; i++) {                   
                    trace.deleteVertex(0);
                }
            }

            // update start marker           
            double startLat = latValues.get(newTraceStartIndex);
            double startLon = lonValues.get(newTraceStartIndex);            
            LatLng startCoordinate = LatLng.newInstance(startLat, startLon);
            startMarker.setLatLng(startCoordinate);
            startMarkerList.get(k).setLatLng(startCoordinate);

            // add vertices at END of trace if newTraceEnd > traceEndIndex
            if (newTraceEndIndex > traceEndIndex) {
                // LOG.fine( "Add " + (newTraceEndIndex - traceEndIndex) + " vertices at end"); 
                double lat;
                double lon;
                int vertexCount = trace.getVertexCount();
                for (int i = traceEndIndex + 1; i <= newTraceEndIndex; i++) {                    
                    lat = latValues.get(i);
                    lon = lonValues.get(i);
                    trace.insertVertex(vertexCount,LatLng.newInstance(lat, lon));
                    vertexCount++;
                }
            }

            // delete vertices at END of trace if newTraceEnd < traceEndIndex
            if (newTraceEndIndex < traceEndIndex) {
            	// LOG.fine( "Delete " + (traceEndIndex - newTraceEndIndex) + " vertices at end");
                int currentCount = trace.getVertexCount();
                
                for (int i = traceEndIndex; i > newTraceEndIndex; i--) {               
                	trace.deleteVertex(currentCount - 1);
                    // LOG.fine("trying to delete vertex " + currentCount);
                    currentCount--;
                    // LOG.fine("done deleting vertex " + i );    
                }
            }

            // LOG.fine( "old start: " + traceStartIndex + ", old end: " + traceEndIndex);
            // LOG.fine("old trace start index is" + traceStartIndex + ", oldtrace end index is " +
            // traceEndIndex);
//            LOG.fine( "new start: " + newTraceStartIndex + ", new end: " + newTraceEndIndex +
//            "vertex count is " + trace.getVertexCount());

            // update end marker 
            double endLat = latValues.get(newTraceEndIndex);
            double endLon = lonValues.get(newTraceEndIndex); 
            LatLng endCoordinate = LatLng.newInstance(endLat, endLon);
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