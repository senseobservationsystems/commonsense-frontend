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
    private int currentMinTime;
    private int currentMaxTime;
    
    // store all the id's that we get
    ArrayList<Integer> Id_names = new ArrayList<Integer>();

    ArrayList<ArrayList<Timeseries>> latLongList;
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

        // bring all latitude timeseries lists together in one arrayList
        ArrayList<Timeseries> allLatSeries = new ArrayList<Timeseries>();

        // DataPoints with minimum and maximum slider values-to-be
        DataPoint min_i;
        DataPoint max_i;

        for (int m = 0; m < latLongList.size(); m++) {
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
            // JsArray<DataPoint> values = latTimeseries.getData();
            DataPoint v = values.get(0);

            // was: int min = ..
            int localMin = (int) Math.floor(v.getTimestamp().getTime() / 1000l);
            if (localMin < min) {
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

        // LOG.fine("And the start slider value is " + startSlider.getValue());
        // LOG.fine("Calculate slider range: min is " + min + " max " + max);
        // LOG.fine("The minimum date is " + min_i.getTimestamp().getTime());
        // LOG.fine("The maximum date is " + max_i.getTimestamp().getTime());
        LOG.fine("Minimum date found is " + calculDate(min_i) + " maximum date found is " 
        		+ calculDate(max_i));
//        LOG.fine("The min according to slider is " + startSlider.onFormatValue(min) + 
//        "the max according to slider is " + endSlider.onFormatValue(max));
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
        
        LOG.fine("Initial MinTime is " + minTime + " " + calculDate(minTime) + " maxTime is " + maxTime + " " + calculDate(maxTime));
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
            /*JsArray<DataPoint> latValues = latTimeseries.getData();
            JsArray<DataPoint> lngValues = lngTimeseries.getData();
            */
            
            ArrayList<Float> latValues = allGoodLat.get(l);
            ArrayList<Float> lonValues = allGoodLon.get(l);
//            Here, latValues has changed from an array to arrayList, so check for size/length()

            LOG.fine("Number of points: " + latValues.size());
            LOG.fine("BadPoints length for " + Id_names.get(l) + "is " + bigBadPoints.get(l).size());
            ArrayList<Integer> curBadPoints = bigBadPoints.get(l);

            // Draw the filtered points.
            if (latValues.size() > 0 && maxTime > minTime) {
                LatLng[] points = new LatLng[latValues.size()];

                traceStartIndex = -1;
                traceEndIndex = -1;
                int lastPoint = -1;
                //FloatDataPoint lat;
                //FloatDataPoint lng;
                double lat;
                double lng;
                
                for (int i = 0, j = 0; i < latValues.size(); i++) {
                    //lat = latValues.get(i).cast();
                    //lng = lngValues.get(i).cast();
                	lat = latValues.get(i);
                	lng = lonValues.get(i);

                    // timestamp in secs
                	long timestamp = allGoodTimestamps.get(l).get(i)/1000;
                    //long timestamp = lat.getTimestamp().getTime() / 1000;
                   // ArrayList<Integer> badPointsList = bigBadPoints.get(l);
                    
                    // changed condition next line
                    if (/* timestamp >= minTime && timestamp < maxTime */timestamp != 0) {
                        // update indices
                        lastPoint = j;
                        traceEndIndex = i;
                        if (-1 == traceStartIndex) {
                            traceStartIndex = i;
                        }
                        // store coordinate
                        //LatLng coordinate = LatLng.newInstance(lat.getValue(), lng.getValue());
                        LatLng coordinate = LatLng.newInstance(lat, lng);
                        points[j] = coordinate;
                        j++;
                    }
                }

                traceStartIndexList.add(traceStartIndex);
                traceEndIndexList.add(traceEndIndex);

//                LOG.fine("traceStartIndex for " + " is " + traceStartIndex + " traceEndIndex is "
//                        + traceEndIndex);

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
        // storeStartIndex = 0;
        // storeEndIndex = 0;
        // currentMinTime = 0;
        // currentMaxTime = 0;
        // weirdSlider = false;
        // initMaxTime = 0;

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

        // LOG.fine("Appending an array.. " + data.get(0).getLabel());
        // LOG.fine ("The ID equals.. " + data.get(0).getIdd());
        // LOG.fine("The last ID equals..." + data.get(data.length()-1).getIdd());

        // create an ArrayList to store multiple arrayLists, to group the Timeseries
        // objects by their ID

        ArrayList<ArrayList<Timeseries>> bigList = new ArrayList<ArrayList<Timeseries>>();
        
        

        
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

            else {// if (newId != -1) {
                for (int j = 0; j < bigList.size(); j++) {
                    ArrayList<Timeseries> currentArray = new ArrayList<Timeseries>();
                    currentArray = bigList.get(j);
                    if (currentArray.get(0).getIdd() == newId) {
                        currentArray.add(data.get(i));
                    }
                }
            }
        }
        // for every ID in the bigList, get the lat and long timeseries, and check the distance
        // between points in those timeseries
        
        LOG.fine("The biglist size is "+ bigList.size());
        
       
         for (int r = 0; r < bigList.size(); r++) {
       
        	// get all the Timeseries for a given ID
        	ArrayList<Timeseries> currentArray = bigList.get(r);
        	
        	LOG.fine ("LOOOKKK currentArray size is " + currentArray.size());
        	
    		boolean latitudeFound = false;
    		boolean longitudeFound = false;
    		
    		ArrayList<Float> latPoints = new ArrayList<Float>();
    		ArrayList<Float> lonPoints = new ArrayList<Float>();
    		
    		// for each ID, go through all the Timeseries and check if they are latitude/longitude
        	for (int s = 0; s < currentArray.size(); s++ ) {
        		Timeseries curTimeseries = currentArray.get(s);
 
        		
        		// create a new arrayList to store lat-long pairs for each index of data point
        		
        		
        		if (curTimeseries.getLabel().endsWith("latitude")) {
        			
        			//get the latitude values of the current Timeseries
        			JsArray<DataPoint> currentLatValues = curTimeseries.getData();
        			//LOG.fine ("Latitude array found");
        			
        			for (int k = 0; k < currentLatValues.length(); k++ ) {

        				FloatDataPoint lat = currentLatValues.get(k).cast();
        				float latValue = new Float(lat.getValue());
        				// add latitude value to the pair
        				latPoints.add(latValue);
        				latitudeFound = true;
        				//LOG.fine("the latitude equals " + latValue);
        			}
        		} // close if ends with latitude
        		
        		else if (curTimeseries.getLabel().endsWith("longitude")) {
        			//get the latitude values of the current Timeseries
        			JsArray<DataPoint> currentLonValues = curTimeseries.getData();
        			
        			for (int k = 0; k < currentLonValues.length(); k++ ) {
        				FloatDataPoint lon = currentLonValues.get(k).cast();
        				float lonValue = new Float (lon.getValue());
        				
        				// add the longitude value to the arrayList of latLong pairs
        				lonPoints.add(lonValue);
        				longitudeFound = true;
        				//LOG.fine("the longitude equals " + lonValue);
        			}
        		} // close if ends with longitude
        		
        		if (latitudeFound == true && longitudeFound == true) {
           		 
//        			LOG.fine ("The length of latPoints for " + r + " is " + latPoints.size() + 
//        					" The length of lonPoints  for " + r + " is " + lonPoints.size());
        			break;
        			
        		}  // close if 
			
        	} // close for (all Timeseries for a given ID; now we created the latitude and longitude arrayLists)
        	
        	// make an arrayList to store lat-long pairs for this ID
    		ArrayList<ArrayList<Float>> latLongPoints = new ArrayList<ArrayList<Float>>();
    		int latLength = latPoints.size();
    		
    		// add the pair to the arrayList of latLong pairs
    		for (int n = 0; n < latLength; n++) {
    			ArrayList<Float> tempList = new ArrayList<Float>();
    			tempList.add(latPoints.get(n));
    			tempList.add(lonPoints.get(n));
    			latLongPoints.add(tempList);

    		}
    		
    		LOG.fine ("latLongPoints length for " + Id_names.get(r) + " is " + latLongPoints.size());
    		
    		// calculate distance in km between adjacent points of the given ID
    		
    		final int R = 6371; // km; the earth radius
			
			if (latLongPoints.size() > 1) {
			// initialize latitude and longitude values to the first point
				
			// create an arrayList of "bad points" that have to be discarded for a given ID
			ArrayList<Integer> badPoints = new ArrayList<Integer>();
				
			//create an arrayList to store lat-long pairs with only "good points" for a given ID
			ArrayList <ArrayList<Float>> IdGoodPoints = new ArrayList<ArrayList<Float>>();
			
			// create three arrayLists to store latitudes, longitudes and timestamps of only "good points"
			ArrayList<Float> IdGoodLat = new ArrayList<Float>();
			ArrayList<Float> IdGoodLon = new ArrayList<Float>();
			ArrayList <Long> IdGoodTimestamps = new ArrayList<Long>();	
			
							
			double latit = latLongPoints.get(0).get(0);
			double longit = latLongPoints.get(0).get(1);
			long pointTimestamp = currentArray.get(0).getData().get(0).getTimestamp().getTime();
			
			
			// we just assume the first point is "good" and add it to all the good arrayLists
			ArrayList<Float> firstGoodPoint = new ArrayList<Float>();
			firstGoodPoint.add(new Float (latit));
			firstGoodPoint.add(new Float (longit));
			IdGoodLat.add(new Float (latit));
			IdGoodLon.add(new Float(longit));
			IdGoodPoints.add(firstGoodPoint);
			LOG.fine("IdGoodPoints size is " + IdGoodPoints.size());
			IdGoodTimestamps.add(pointTimestamp);
					
				
    		for (int p = 1; p < latLongPoints.size(); p++ ) {
		
    			double newLatit = latLongPoints.get(p).get(0);
    			double newLongit = latLongPoints.get(p).get(1);
    			long newPointTimestamp = currentArray.get(0).getData().get(p).getTimestamp().getTime();
    			
    			double radLongit = Math.toRadians(longit);
    			double radLatit = Math.toRadians(latit);
    			double radNewLongit = Math.toRadians(newLongit);
    			double radNewLatit = Math.toRadians(newLatit);
    			
    			// distance in km between two points
    			double distance = Math.acos(Math.sin(radLatit)*Math.sin(radNewLatit) + 
    					Math.cos(radLatit)*Math.cos(radNewLatit) * 
    					Math.cos(radNewLongit-radLongit)) * R;
    						
    			long timeDifference = newPointTimestamp - pointTimestamp;
    			double hourDifference = timeDifference * 2.77777778 * 0.0000001;
    			double speed = -1; 
    			
    			// speed in km/h
    			if (hourDifference != 0) {
    				speed = distance / hourDifference; 
    				} 
    			else 
    				LOG.fine ("distance equals zero");
    			
    			String pointDate = calculDate (currentArray.get(0).getData().get(p));
    			String prevPointDate = calculDate (currentArray.get(0).getData().get(p-1));
    			
    			
    			
    			if (speed > 500 && distance > 20) {
    				LOG.fine ("There is nothing on earth that can move so fast!" + 
    						"\ndistance is " + distance + "hourDifference is " + hourDifference + "speed is " + speed + " date is " + pointDate +
        					"\ncomparing timestamp " + pointDate + " to " + prevPointDate);
    				LOG.fine("point " + p + " of " + Id_names.get(r) + " will be discarded" +
    						" its newLatit is " + newLatit + " newLongit is " + newLongit);
    				Integer newBadPoint = new Integer(p);
    				badPoints.add(newBadPoint);
    				
    			} else {
    				/*
    				LOG.fine("point is " + p + " latit is " + latit + " longit is " + longit + 
    						" newLatit is " + newLatit + " newLongit is " + newLongit + 
    						" speed is " + speed + " distance is " + distance + " pointDate is " + pointDate);
    				*/
    				latit = newLatit;
    				longit = newLongit;
    				pointTimestamp = newPointTimestamp;
    				
    				// make an arrayList to store lat-long coordinates and timestamp as a "good Point"
    				ArrayList<Float> goodPoint = new ArrayList<Float>();
    				
    				IdGoodLat.add(new Float(newLatit));
    				IdGoodLon.add(new Float(newLongit));
    				IdGoodTimestamps.add(newPointTimestamp);
    				
    				goodPoint.add(new Float (newLatit));
    				goodPoint.add(new Float (newLongit));
    				IdGoodPoints.add(goodPoint);
    				
    			}
    			} // close if (latLongPoints size > 1)
    			
    			// add the "good" and "bad" points to the global lists
    			bigBadPoints.add(badPoints);
    			allGoodLat.add(IdGoodLat);
    			allGoodLon.add(IdGoodLon);
    			allGoodPoints.add(IdGoodPoints);
    			allGoodTimestamps.add(IdGoodTimestamps);
    			
    			LOG.fine("badPoints length for " + Id_names.get(r) + " is " + badPoints.size());
    			LOG.fine("goodPoints length for " + Id_names.get(r) + " is " + IdGoodPoints.size());
        		
        	} // close for current ID
			
			
        	
        	
         } // close for bigList, the entire list of IDs
    		
    
        			// example data: rotterdam latitude 51.9166667 Longitude: 4.5
        			// Amsterdam Latitude: 52.35 Longitude: 4.9166667
        			
        			/*latit = 51.9166667;
        			longit = 4.5;
        			newLatit = 52.35;
        			newLongit = 4.9166667;*/
         
        			//LOG.fine("The distance between the points is " + distance + "km, and the speed is " + speed + "kmh");


        // sort lat/lng data

        // for every arrayList within the bigList, find lat and long timeseries;
        // put the pairs in a big arrayList called latLongList

        Timeseries series;

        latLongList = new ArrayList<ArrayList<Timeseries>>();

        for (int i = 0; i < bigList.size(); i++) {
            ArrayList<Timeseries> curList = bigList.get(i);
            ArrayList<Timeseries> latLong = new ArrayList<Timeseries>();
            LOG.fine("The old size is " + curList.size());

            boolean latSet = false;
            boolean longSet = false;
            boolean added = false;

            for (int j = 0; j < curList.size(); j++) {

                series = curList.get(j);
                if (series.getLabel().endsWith("latitude")) {
                    // LOG.fine("Yes!!! latitude found");
                    latTimeseries = series;

                    latLong.add(0, series);

                    latSet = true;

                } else if (curList.get(j).getLabel().endsWith("longitude")) {
                    // LOG.fine("Yes!!! longitude found");
                    lngTimeseries = series;
                    if (!latLong.isEmpty()) {
                        latLong.add(1, series);
                    } else {
                        latLong.add(series);
                    }
                    longSet = true;
                }
                if (latSet == true && longSet == true && added == false) {
                    latLongList.add(latLong);

                    // LOG.fine("LatLong size is " + latLong.size());
                    // LOG.fine("Adding latLong arrayList number " + i);
                    added = true;

                }
            }
        }

        //LOG.fine("The length of latLongList is " + latLongList.size());

        

        if (latTimeseries != null && lngTimeseries != null) {
            calcSliderRange();
            drawTrace();
            centerMap();
        }
    			
    }

    private void updateTrace() {

        int minTime = startSlider.getValue();     
        int maxTime = endSlider.getValue();
 
//        LOG.fine("MinTime is " + minTime + " " + calculDate(minTime) + " maxTime is " + maxTime + " " + calculDate(maxTime));
        

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

        for (int k = 0; k < latLongList.size(); k++) {
            latTimeseries = latLongList.get(k).get(0);
            lngTimeseries = latLongList.get(k).get(1);
            Polyline trace = polyList.get(k);
            // LOG.fine("Trying to get PolyList.. " + k);
            startMarker = startMarkerList.get(k);
            endMarker = endMarkerList.get(k);
            traceStartIndex = traceStartIndexList.get(k);
            traceEndIndex = traceEndIndexList.get(k);

//            LOG.fine("traceStartIndex for " + k + " is " + traceStartIndex + " traceEndIndex is "
//                    + traceEndIndex);

            if (null == trace || false == trace.isVisible()) {
                LOG.fine("updateTrace skipped: trace is not shown yet");
                return;
            }

            // get the sensor values
            /*
            JsArray<DataPoint> latValues = latTimeseries.getData();
            JsArray<DataPoint> lonValues = lngTimeseries.getData();
			*/
            
            ArrayList<Float> latValues = allGoodLat.get(k);
            ArrayList<Float> lonValues = allGoodLon.get(k);
            
            // find the start end end indices of the trace in the sensor data array
            int newTraceStartIndex = 0, newTraceEndIndex = latValues.size() - 1;
            long timestamp;
            boolean done = false;
            boolean done_end = false;

            for (int i = 0; i < latValues.size(); i++) {
                // get timestamp
            	timestamp = allGoodTimestamps.get(k).get(i)/1000;
//            	LOG.fine ("Timestamp is " + timestamp + " " + calculDate((int)timestamp) + " minTime is " + minTime + " " + calculDate (minTime) +
//            			" maxTime is " + maxTime + " " + calculDate (maxTime));
                //timestamp = latValues.get(i).getTimestamp().getTime() / 1000;
                // changed first condition from >minTime

                if (i == latValues.size() - 1 && timestamp < minTime) {
                    // start index is not changed
                    // end index is not changed
                    newTraceStartIndex = latValues.size() - 1;
                    newTraceEndIndex = latValues.size() - 1;
//                    LOG.fine(" end index is not changed: " + newTraceEndIndex);
                } 

                if (whichSlider != 1) {
                    // start index is not changed
                    // LOG.fine("start index is not changed: " + traceStartIndex);
                    newTraceStartIndex = traceStartIndex;
                   
                } else if (i == 0 && timestamp == minTime) {
                	// if whichSlider is 1 and it is moved back to the start, change the startIndex to 0
//                	LOG.fine ("Moved back to the start: setting traceStartIndex to 0");
                	newTraceStartIndex = 0;
                	done = true;
                	
            	} else if (whichSlider != 2 && timestamp > minTime && newTraceStartIndex == 0 && done == false) {
                    // this is the first index with start of visible range
                    newTraceStartIndex = i; 
//                    LOG.fine("changing newTraceStartIndex to " + i);
                    done = true;
                    // LOG.fine("At index " + i + "we're inside the visible range");
                    // storeStartIndex = newTraceStartIndex;
                }
                
                
                
                if (whichSlider != 2) {
                    // end index is not changed
                    // LOG.fine("end index is not changed: " + traceEndIndex);
                    newTraceEndIndex = traceEndIndex;

                } else if (i == latValues.size() - 1 && timestamp == maxTime) {
                	// if whichSlider is 2 and it is moved back to the end, change the endIndex to latValues.size - 1
//                	LOG.fine ("Moved back to the end: setting TraceEndIndex to " + (latValues.size()-1));
                	done_end = true;
                	
                } else if (whichSlider != 1 && i == 0 && timestamp == maxTime) {
                	newTraceEndIndex = 0;
                	done_end = true;
//                	LOG.fine ("Moved back to the start with the second slider: setting TraceEndIndex to 0");
                	
                }
                
                else if (whichSlider != 1 && timestamp > maxTime + 10 && done_end == false) {
                    // this is the first index after the end of visible range
                    // + 10 because if the values are equal for us, the computer
                    // thinks the timestamp is a bit bigger
//                  LOG.fine("time is " + calculDate((int) timestamp) + " maxTime is "
//                            + calculDate(maxTime));
                  
//                  LOG.fine("timestamp is " + timestamp + " maxTime is " + maxTime);

//                   LOG.fine("Border reached: NewTraceEndIndex is " + newTraceEndIndex
//                            + " NewTraceStartIndex is " + newTraceStartIndex
//                            + " changing newTraceEndIndex to " + (i - 1));
                    newTraceEndIndex = i - 1;
                    done_end = true;
                    // storeEndIndex = newTraceEndIndex;
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

            //LOG.fine("NewTraceStartIndex for " + k + " is " + newTraceStartIndex
             //       + " NewTraceEndIndex is " + newTraceEndIndex);

            // add vertices at START of trace if newTraceStart < traceStartIndex
            if (newTraceStartIndex < traceStartIndex) {

                //FloatDataPoint lat;
                //FloatDataPoint lon;
                double lat;
                double lon;
                // LOG.fine( "Add " + (traceStartIndex - newTraceStartIndex) +
                // " vertices at start");
                for (int i = traceStartIndex - 1; i >= newTraceStartIndex; i--) {
                    //lat = latValues.get(i).cast();
                    //lon = lonValues.get(i).cast();  
                    lat = latValues.get(i);
                    lon = lonValues.get(i);  
                    if (whichSlider != 2) {
                        //trace.insertVertex(0, LatLng.newInstance(lat.getValue(), lon.getValue()));
                        trace.insertVertex(0, LatLng.newInstance(lat, lon));
                    }
                }

            }

            // delete vertices at START of trace if newTraceStart > traceStartIndex
            if (newTraceStartIndex > traceStartIndex) {
                // LOG.fine( "Delete " + (newTraceStartIndex - traceStartIndex) +
                // " vertices at start");
                for (int i = traceStartIndex; i < newTraceStartIndex; i++) {
                    // if (trace.getVertexCount()> 1)
                    trace.deleteVertex(0);
                }
            }

            // update start marker
            //FloatDataPoint startLat = latValues.get(newTraceStartIndex).cast();
            //FloatDataPoint startLon = lonValues.get(newTraceStartIndex).cast();
            double startLat = latValues.get(newTraceStartIndex);
            double startLon = lonValues.get(newTraceStartIndex);
            //LatLng startCoordinate = LatLng.newInstance(startLat.getValue(), startLon.getValue());
            LatLng startCoordinate = LatLng.newInstance(startLat, startLon);
            startMarker.setLatLng(startCoordinate);
            startMarkerList.get(k).setLatLng(startCoordinate);

            // add vertices at END of trace if newTraceEnd > traceEndIndex
            if (newTraceEndIndex > traceEndIndex) {
                // LOG.fine( "Add " + (newTraceEndIndex - traceEndIndex) + " vertices at end");
                //FloatDataPoint lat;
                //FloatDataPoint lon;
                double lat;
                double lon;
                int vertexCount = trace.getVertexCount();
                for (int i = traceEndIndex + 1; i <= newTraceEndIndex; i++) {
                    //lat = latValues.get(i).cast();
                    //lon = lonValues.get(i).cast();
                    lat = latValues.get(i);
                    lon = lonValues.get(i);
                    trace.insertVertex(vertexCount,
                            //LatLng.newInstance(lat.getValue(), lon.getValue()));
                    		LatLng.newInstance(lat, lon));
                    vertexCount++;
                }
            }

            // delete vertices at END of trace if newTraceEnd < traceEndIndex
            if (newTraceEndIndex < traceEndIndex) {
                // int howManyToDelete = traceEndIndex - newTraceEndIndex;
                // LOG.fine( "Delete " + (traceEndIndex - newTraceEndIndex) + " vertices at end");
                int currentCount = trace.getVertexCount();
                // if (trace.getVertexCount() > 1)
                /*
                 * for (int i = 0; i < howManyToDelete -1; i++) { trace.deleteVertex (traceEndIndex
                 * - i); }
                 */
                for (int i = traceEndIndex; i > newTraceEndIndex; i--) {
                    // if (trace.getVertexCount() > 1) {

                    // while (trace.getVertexCount()== currentCount) {
                    trace.deleteVertex(currentCount - 1);
                    // LOG.fine("trying to delete vertex " + currentCount);
                    currentCount--;

                    // }
                    // LOG.fine("done deleting vertex " + i );

                    // }
                }
            }

            // LOG.fine( "old start: " + traceStartIndex + ", old end: " + traceEndIndex);
            LOG.fine( "new start: " + newTraceStartIndex + ", new end: " + newTraceEndIndex +
            "vertex count is " + trace.getVertexCount());
            // LOG.fine("old trace start index is" + traceStartIndex + ", oldtrace end index is " +
            // traceEndIndex);
            int vertCount = trace.getVertexCount();
            //LOG.fine("vertex count is " + vertCount);

            // update end marker
            //FloatDataPoint endLat = latValues.get(newTraceEndIndex).cast();
           // FloatDataPoint endLon = lonValues.get(newTraceEndIndex).cast();
            
            double endLat = latValues.get(newTraceEndIndex);
            double endLon = lonValues.get(newTraceEndIndex);
            
            LatLng endCoordinate = LatLng.newInstance(endLat, endLon);
            //LatLng endCoordinate = LatLng.newInstance(endLat.getValue(), endLon.getValue());
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