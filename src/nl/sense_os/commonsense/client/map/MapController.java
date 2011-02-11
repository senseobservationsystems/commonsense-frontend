package nl.sense_os.commonsense.client.map;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.controllers.VizController;
import nl.sense_os.commonsense.client.controllers.cors.DataJsniRequests;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.TagModel;
import nl.sense_os.commonsense.shared.sensorvalues.BooleanValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.FloatValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.JsonValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.SensorValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.StringValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.TaggedDataModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.dev.jjs.ast.js.JsonObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class MapController extends Controller {

    private static final String TAG = "MapController";	
	private MapView mapView;
    
	public MapController() {
		registerEventTypes(MapEvents.LoadMap, MapEvents.ShowMap);
	}

	public void initialize() {
		super.initialize();
		this.mapView = new MapView(this);
	}
	
	@Override
	public void handleEvent(AppEvent event) {
		EventType evtType = event.getType();
		
		if (evtType.equals(MapEvents.LoadMap)) {
			requestData(event);
			
		} else if (evtType.equals(MapEvents.ShowMap)) {
			// @@ TODO:
			// 1- retrieve the data from the event
			// 2- parse the data
			// 3- use the parsed data to create the map
			String response = event.getData("response");
			JSONObject data = parseData(response);
			
			//forwardToView(this.mapView, event);
			forwardToView(this.mapView, MapEvents.CreateMap, data);
		}
	}
	
	/**
	 * Retrieve a list of sensors with their positions.
	 * 
	 * @return
	 */
	private List<TreeModel> getSensorsData() {
		// @@ TODO: implement this!!!
		return new ArrayList<TreeModel>();
	}
	
	private void showMap() {
		// @@ TODO: implement this!!!
	}
	
	/**
	 * Set the event with the required parameters for the AJAX request and dispatch
	 * the event. The AJAX Controller will be listening to this event and it will 
	 * get this request. Then, it will make the request and redirect the result to 
	 * this Controller by using the ShowMap event type.
	 *  
	 * @param event
	 */
    private void requestData(AppEvent event) {
    	// @@ FIXME    	
    	
    	Log.d(TAG, "requestData");
    	
    	HashMap<String, Object> params = new HashMap<String, Object>();
    	
        //SensorValueModel[] pagedValues = new SensorValueModel[0];
        int page = 0;

        TreeModel sensor = event.getData("tag");
        
        String url = Constants.URL_DATA.replace("<id>", "" + sensor.<String> get("id"));
        url += "?page=" + page;
        url += "&per_page=" + 1000;
        url += "&start_date=" + event.<Double> getData("startDate");
        url += "&end_date=" + event.<Double> getData("endDate");
        
        String owner = sensor.get("alias");
        if (null != owner) {
            url += "&alias=" + owner;
        }

        String sessionId = Registry.get(Constants.REG_SESSION_ID);        
        
        params.put("id", sensor.<String> get("id"));
        params.put("url", url);
        params.put("session_id", sessionId);
        // Tell the AJAX Controller to dispatch this event after getting the response.
        params.put("forward_evt", MapEvents.ShowMap);

        //DataJsniRequests.requestData(url, sessionId, sensor, page, pagedValues, this);
       
        // This is the way to tell the AJAX Controller to make a request.
        Dispatcher.forwardEvent(AjaxEvents.OnRequest, params);
    }
    
    // @@ TODO: implement this!!!
    private JSONObject parseData(String response) {
    	return new JSONObject();
    	
    	/*
        SensorValueModel[] values = null;
        try {
            JSONObject obj = JSONParser.parseStrict(response).isObject();
            JSONArray data = obj.get("data").isArray();

            Log.d(TAG, "Received " + data.size() + " sensor data points");

            values = new SensorValueModel[data.size()];
            JSONObject datapoint;
            double decimalTime;
            Date timestamp;
            String rawValue;
            String cleanValue;
            SensorValueModel value;
            for (int i = 0; i < data.size(); i++) {

                datapoint = data.get(i).isObject();

                // parse time
                decimalTime = Double.parseDouble(datapoint.get("date").isString().stringValue());
                timestamp = new Date((long) (decimalTime * 1000));

                // get value (always a String initially)
                rawValue = datapoint.get("value").isString().stringValue();
                cleanValue = rawValue.replaceAll("//", "");

                if ((cleanValue.charAt(0) == '{')
                        && (cleanValue.charAt(cleanValue.length() - 1) == '}')) {
                    JSONObject jsonValue = JSONParser.parseStrict(cleanValue).isObject();
                    if (null != jsonValue) {
                        // Log.d(TAG, "JsonValue");

                        HashMap<String, Object> fields = new HashMap<String, Object>();
                        for (String fieldKey : jsonValue.keySet()) {
                            JSONValue fieldValue = jsonValue.get(fieldKey);

                            JSONNumber numberField = fieldValue.isNumber();
                            if (null != numberField) {
                                fields.put(fieldKey, numberField.doubleValue());
                                continue;
                            }

                            JSONString stringField = fieldValue.isString();
                            if (null != stringField) {
                                fields.put(fieldKey, stringField.stringValue());
                                continue;
                            }
                            fields.put(fieldKey, fieldValue.toString());
                        }
                        value = new JsonValueModel(timestamp, fields);
                        values[i] = value;
                        continue;
                    }
                }

                try {
                    double doubleValue = Double.parseDouble(cleanValue);
                    // Log.d(TAG, "FloatValue");
                    value = new FloatValueModel(timestamp, doubleValue);
                    values[i] = value;
                    continue;
                } catch (NumberFormatException e) {
                    // do nothing
                }

                boolean boolValue = Boolean.parseBoolean(cleanValue);
                if (!boolValue && cleanValue.equalsIgnoreCase("false")) {
                    // Log.d(TAG, "BooleanValue");
                    value = new BooleanValueModel(timestamp, boolValue);
                    values[i] = value;
                    continue;
                }

                // Log.d(TAG, "StringValue");
                value = new StringValueModel(timestamp, cleanValue);
                values[i] = value;
                continue;
            }

        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException parsing sensor data: " + e.getMessage());
            handler.onDataFailed(tag);
        }

        // append the parsed values to any previous pages of values
        SensorValueModel[] allValues = values;
        if (pagedValues != null) {
            allValues = new SensorValueModel[pagedValues.length + values.length];
            System.arraycopy(pagedValues, 0, allValues, 0, pagedValues.length);
            System.arraycopy(values, 0, allValues, pagedValues.length, values.length);
        }

        if (values.length < 1000) {
            Log.d(TAG, "completed getting all pages of data");

            TagModel mdl = new TagModel(tag.<String> get("name") + "/", 0, 0, TagModel.TYPE_SENSOR);
            TaggedDataModel taggedData = new TaggedDataModel(mdl, allValues);
            handler.onDataReceived(taggedData);
        } else {
            // exactly 1000 values? see if there are more pages
            pagedValues = allValues;
            page++;

            String sessionId = Registry.get(Constants.REG_SESSION_ID);
            url = url.replaceAll("\\?page=\\d+&", "\\?page=" + page + "&");
            Log.d(TAG, "new url: " + url);
            DataJsniRequests.requestData(url, sessionId, tag, page, pagedValues, handler);
        }
        */
    }
    
}
