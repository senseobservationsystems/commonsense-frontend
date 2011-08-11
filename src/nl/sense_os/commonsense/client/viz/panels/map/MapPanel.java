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
    
    private String calculDate(long timestamp) {
        long mseconds = timestamp;
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
    	
    	//find the extremes of every trace
    	
    	double newLat_sw = 90;
    	double newLon_sw = 180;
    	
    	double newLat_ne = -90;
    	double newLon_ne = -180;
    	
    	for (int l = 0; l < bigList.size(); l++) {
    		Polyline trace = polyList.get(l);
	        LatLngBounds bounds = trace.getBounds();	       
	        
	        LatLng sw = bounds.getSouthWest();
	        double lat_sw = sw.getLatitude();
	        double lon_sw = sw.getLongitude();
	        if (lat_sw < newLat_sw) newLat_sw = lat_sw;
	        if (lon_sw < newLon_sw) newLon_sw = lon_sw;
	        
	        LatLng ne = bounds.getNorthEast();
	        double lat_ne = ne.getLatitude();
	        double lon_ne = ne.getLongitude();
	        if (lat_ne > newLat_ne) newLat_ne = lat_ne;
	        if (lon_ne > newLon_ne) newLon_ne = lon_ne;
   
	        
    	}
    	//make the new Bounds according to the extremes
    	
    	LatLng new_sw = LatLng.newInstance(newLat_sw, newLon_sw);
    	LatLng new_ne = LatLng.newInstance(newLat_ne, newLon_ne);
    	LatLngBounds newBounds = LatLngBounds.newInstance(new_sw, new_ne);
    	
    	 map.setCenter(newBounds.getCenter());
	     map.setZoomLevel(map.getBoundsZoomLevel(newBounds));
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
                                                          
                    if (timestamp != 0) {
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
    
    /** calculate distance and speed between adjacent points of the given ID, and select out only good points
     * 
     * @param latLongPoints
     * @param currentArray
     * @param r
     * @param goodStart
     * @return
     */
    private int filterPoints(ArrayList<Float> latPoints, ArrayList<Float> lonPoints, ArrayList <Long> timestamps, int r, int goodStart) {
	  	
		// create arrayLists to store latitudes, longitudes and timestamps of only "good points" for this ID
    	// create an arrayList of "bad points" indices that have to be discarded for this ID
    	// add the points before the goodStart to the badPoints arrayList
    	
		ArrayList<Float> IdGoodLat = new ArrayList<Float>();
		ArrayList<Float> IdGoodLon = new ArrayList<Float>();
		ArrayList <Long> IdGoodTimestamps = new ArrayList<Long>();	
		ArrayList<Integer> IdBadPoints = new ArrayList<Integer>();
		
		for (int s = 0; s < goodStart; s++ ) {
			IdBadPoints.add(s);
		}
		
		// initialize lat/long and time to the goodStart and store it as a "good" point			
		double latit = latPoints.get(goodStart);
		double longit = lonPoints.get(goodStart);
		long pointTimestamp = timestamps.get(goodStart);
		
		IdGoodLat.add(new Float (latit));
		IdGoodLon.add(new Float(longit));
		IdGoodTimestamps.add(pointTimestamp);
	
		LOG.fine("goodStart is "+ goodStart + " latit is " + latit + " longit is " + longit);

					
		for (int p = goodStart + 1; p < latPoints.size(); p++ ) {
			
			double newLatit = latPoints.get(p);
			double newLongit = lonPoints.get(p);
			long newPointTimestamp = timestamps.get(p);
			
			// convert values to radians			
			double radLongit = Math.toRadians(longit);
			double radLatit = Math.toRadians(latit);
			double radNewLongit = Math.toRadians(newLongit);
			double radNewLatit = Math.toRadians(newLatit);
			
			// calculate distance in km between two points
			double distance = Math.acos(Math.sin(radLatit)*Math.sin(radNewLatit) + 
					Math.cos(radLatit)*Math.cos(radNewLatit) * 
					Math.cos(radNewLongit-radLongit)) * R;
			
			// calculate time difference  and speed between the two points
			long timeDifference = newPointTimestamp - pointTimestamp;
			double hourDifference = timeDifference * 2.77777778 * 0.0000001;		
			double speed = -1; 
			
			if (hourDifference != 0) {
				speed = distance / hourDifference; 
				} 
			else 
				LOG.fine ("distance equals zero");
			
			//String pointDate = calculDate(timestamps.get(p));
			//String prevPointDate = calculDate (timestamps.get(p -1));
			
			if (speed > 250 && distance > 25 /* && hourDifference < 2*/ ) {
				
				Integer newBadPoint = new Integer(p);
				IdBadPoints.add(newBadPoint);
								
//				LOG.fine ("\nThere is nothing on earth that can move so fast!" + 
//						"\ndistance is " + distance + "hourDifference is " + hourDifference + "speed is " + speed + " date is " + pointDate +
//    					"\ncomparing timestamp " + pointDate + " to " + prevPointDate +
//    					"\npoint " + p + " of " + Id_names.get(r) + " will be discarded" +
//						" its newLatit is " + newLatit + " newLongit is " + newLongit);
				
				
			} else {
				
				// store lat-long coordinates and timestamp as a "good Point"				
				IdGoodLat.add(new Float(newLatit));
				IdGoodLon.add(new Float(newLongit));
				IdGoodTimestamps.add(newPointTimestamp);
				
				// the following points will be checked against this point
				latit = newLatit;
				longit = newLongit;
				pointTimestamp = newPointTimestamp;
					
			}
		} 

		
		// succeeded or not? are there more good points than bad points?
		
		int filterSucceeded = -1;
		if (IdGoodLat.size()> IdBadPoints.size()) {
			
			filterSucceeded = 1;
			
			// add the "good" and "bad" points of this ID to the global lists
			bigBadPoints.add(IdBadPoints);
			allGoodLat.add(IdGoodLat);
			allGoodLon.add(IdGoodLon);
			allGoodTimestamps.add(IdGoodTimestamps);
			
		}
		
		else filterSucceeded = 0;
		
		LOG.fine("badPoints length for " + Id_names.get(r) + " is " + IdBadPoints.size());
		LOG.fine("goodPoints length for " + Id_names.get(r) + " is " + IdGoodLat.size());
		
		return filterSucceeded;
		
	} 
     
    /** repeat the filterPoints function until the point filtering succeeds
     * 
     * @param latPoints
     * @param lonPoints
     * @param timestamps
     * @param r
     */
    private void repeatFilterPoints(ArrayList<Float> latPoints, ArrayList<Float> lonPoints, ArrayList<Long> timestamps, int r) {
			
			int goodStart = 0;
			boolean points_done = false;
							
			while (points_done == false) {
				
				int filter_succeeded = filterPoints (latPoints, lonPoints, timestamps, r, goodStart);
				
				if (filter_succeeded == 1) {
					LOG.fine ("Points filtering succeeded!");
					points_done = true;
					break;
				}
				
				else {
					if (goodStart == latPoints.size() -1) {
					
						LOG.fine ("Reached the end of the array and still didnt find any sensible data");
						break;
						}
					
					else goodStart++;
				}
			}
											
    	} 
    
    /**
     * group the incoming timeseries by their ID
     * @param data
     */
    private ArrayList<ArrayList<Timeseries>> groupTimeseriesById (JsArray<Timeseries> data) {
    	
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
        
        //LOG.fine("The biglist size is "+ bigList.size());
        return bigList;
        
    }
    
    @Override
    protected void onNewData(JsArray<Timeseries> data) {

        LOG.fine("Total data length equals this: .. " + data.length());
        // LOG.fine("Appending an array.. " + data.get(0).getLabel());
        // LOG.fine ("The ID equals.. " + data.get(0).getIdd());
        // LOG.fine("The last ID equals..." + data.get(data.length()-1).getIdd());
        
        
        // group the Timeseries objects by their ID (bigList contains all Timeseries arrayLists for each ID)        
        bigList = groupTimeseriesById(data);     
        LOG.fine("The biglist size is "+ bigList.size());
        
        
        // create latitude, longitude and timestamps arrayLists for each ID          
       
         for (int r = 0; r < bigList.size(); r++) {
       
        	// get all the Timeseries for this ID
        	ArrayList<Timeseries> IdTimeseriesArray = bigList.get(r);       	
        	LOG.fine ("LOOOKKK IdTimeseriesArray size is " + IdTimeseriesArray.size());
        	
        	// make arrayLists to store lat, long and timestamps for this ID
        	ArrayList<Float> latPoints = new ArrayList<Float>();
    		ArrayList<Float> lonPoints = new ArrayList<Float>();
    		ArrayList<Long> timestamps = new ArrayList<Long>();
    		
    		boolean latitudeFound = false, longitudeFound = false;
    		   		
	
    		//go through all the Timeseries and store latitude/longitude and timestamps for this ID
    		
        	for (int s = 0; s < IdTimeseriesArray.size(); s++ ) {
        		
        		Timeseries curTimeseries = IdTimeseriesArray.get(s);
        		
        		if (curTimeseries.getLabel().endsWith("latitude")) {
        			JsArray<DataPoint> currentLatValues = curTimeseries.getData();
        			//LOG.fine ("Latitude array found");
        			
        			for (int k = 0; k < currentLatValues.length(); k++ ) {
        				FloatDataPoint lat = currentLatValues.get(k).cast();
        				float latValue = new Float(lat.getValue());
        				long curTimestamp = new Long (lat.getTimestamp().getTime());
        				timestamps.add(curTimestamp);
        				latPoints.add(latValue);      				
        				//LOG.fine("the latitude equals " + latValue);
        			}
        			latitudeFound = true;
        		} 
        		
        		else if (curTimeseries.getLabel().endsWith("longitude")) {        			
        			JsArray<DataPoint> currentLonValues = curTimeseries.getData();
        			
        			for (int k = 0; k < currentLonValues.length(); k++ ) {
        				FloatDataPoint lon = currentLonValues.get(k).cast();
        				float lonValue = new Float (lon.getValue());
        				lonPoints.add(lonValue);       				
        				//LOG.fine("the longitude equals " + lonValue);
        			}
        			longitudeFound = true;
        		} 
        		
        		if (latitudeFound == true && longitudeFound == true) {
           		 
//        			LOG.fine ("The length of latPoints for " + Id_names.get(r) + " is " + latPoints.size() + 
//        					" The length of lonPoints  for " + Id_names.get(r) + " is " + lonPoints.size());     	
        			break;      			
        		}  
			
        	}        	

    		// try to filter points, starting with point 0, and see if we get more good points than bad.. otherwise we start from the next point.. and so on
    		
			if (latPoints.size() > 1) {							
				repeatFilterPoints(latPoints, lonPoints, timestamps, r);
			}							
         }
         
		
        if (!allGoodLat.isEmpty()) {
            calcSliderRange();
            drawTrace();
            centerMap();
        }   			
    }
    
    /** determine which slider has been moved
     * 
     * @param minTime
     * @param maxTime
     * @return
     */
    private int determineSlider(int minTime, int maxTime) {
    	
    	int whichSlider = 0;

        if (currentMinTime != minTime) {
            whichSlider = 1;           
            currentMinTime = minTime;
            
            // LOG.fine("Start value has changed, which slider is: " + whichSlider);
        }

        else if (currentMaxTime != maxTime) {
            whichSlider = 2;      
            currentMaxTime = maxTime;
            
            // LOG.fine("End value has changed, which slider is: " + whichSlider);
            
        } else {
            LOG.warning("cannot determine which slider");
           // return;
        }
        return whichSlider;

    }
       
    /** update trace indices
     * 
     * @param minTime
     * @param maxTime
     * @param whichSlider
     * @param k
     * @return
     */ 
    private ArrayList<Integer> updateTraceIndex(int minTime, int maxTime, int whichSlider, int k) {
    	
    	// get the sensor values
        ArrayList<Float> latValues = allGoodLat.get(k);
        
        // find the start end end indices of the trace in the sensor data array
        int newTraceStartIndex = 0, newTraceEndIndex = latValues.size() - 1;
        int timestamp;
        boolean done = false;
        
        for (int i = 0; i < latValues.size(); i++) {
            
        	timestamp = (int) (allGoodTimestamps.get(k).get(i)/1000l);
       	            	
        	if (timestamp > minTime && whichSlider != 2) {
        		newTraceStartIndex = i -1;
        		if (newTraceStartIndex < 0) newTraceStartIndex = 0;
        		newTraceEndIndex = traceEndIndex;
        		done = true;
        		//LOG.fine ("Changing startindex to " + newTraceStartIndex);
        		break;
        	}           	          	
        	
        	
        	else if (timestamp > maxTime && whichSlider != 1) {
        		newTraceEndIndex = i - 1;
        		newTraceStartIndex = traceStartIndex;
        		done = true;
        		//LOG.fine("Changing end index to " + (i - 1));    
        		break;          		       		
        	}
        	
        	else if (timestamp <= minTime && i == latValues.size() -1 && whichSlider != 2) {
        		newTraceStartIndex = latValues.size() -1;
        		newTraceEndIndex = traceEndIndex;
        		done = true;
        		//LOG.fine("Changing start index to end " + i);
        		break;	
        	}
        	
        	else if (timestamp <= maxTime && i == latValues.size() -1 && whichSlider != 1) {
        		newTraceEndIndex = latValues.size() -1;
        		newTraceStartIndex = traceStartIndex;  
        		done = true;
        		//LOG.fine("Changing end index to " + i);
        		break;		
        	} 	
        }
        
        if (done == false) LOG.fine ("weird stuff happening in updateTraceIndex.. ");

        if (newTraceStartIndex > newTraceEndIndex) {
            // LOG.warning("Start index of trace is larger than end index?!");
            if (whichSlider != 2) {
                //LOG.fine("First slider was moved too far");
                newTraceStartIndex = newTraceEndIndex;
            } else if (whichSlider != 1) {
                //LOG.fine("Second slider was moved too far");
                newTraceEndIndex = newTraceStartIndex;
            }            
        }
        
        //LOG.fine("NewTraceStartIndex for " + k + " is " + newTraceStartIndex + " NewTraceEndIndex is " + newTraceEndIndex);
        ArrayList<Integer> startEnd = new ArrayList<Integer>();
        startEnd.add(newTraceStartIndex);
        startEnd.add(newTraceEndIndex);
        return startEnd;
    }
    
    private void updateTrace() {

        int minTime = startSlider.getValue();     
        int maxTime = endSlider.getValue();
        //LOG.fine("MinTime is " + minTime + " " + calculDate(minTime) + " maxTime is " + maxTime + " " + calculDate(maxTime));
        
        int whichSlider = determineSlider(minTime, maxTime);        
        
        // LOG.fine( "updateTrace ");

        for (int k = 0; k < bigList.size(); k++) {
            
            Polyline trace = polyList.get(k);
            startMarker = startMarkerList.get(k);
            endMarker = endMarkerList.get(k);
            traceStartIndex = traceStartIndexList.get(k);
            traceEndIndex = traceEndIndexList.get(k);

            if (null == trace || false == trace.isVisible()) {
                LOG.fine("updateTrace skipped: trace is not shown yet");
                return;
            }

            // get the sensor values
            ArrayList<Float> latValues = allGoodLat.get(k);
            ArrayList<Float> lonValues = allGoodLon.get(k);
            
            // find the start and end indices of the trace in the sensor data array
            ArrayList<Integer> startEnd = updateTraceIndex(minTime, maxTime, whichSlider, k);
            
            int newTraceStartIndex = startEnd.get(0);
            int newTraceEndIndex = startEnd.get(1);  
            
            
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
                    currentCount--;                    
                }
            }

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