package nl.sense_os.commonsense.client.mvc.controllers;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.MainEvents;
import nl.sense_os.commonsense.client.mvc.events.VizEvents;
import nl.sense_os.commonsense.client.mvc.views.VizView;
import nl.sense_os.commonsense.client.services.TagServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.TagModel;
import nl.sense_os.commonsense.shared.sensorvalues.BooleanValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.FloatValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.JsonValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.SensorValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.StringValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.TaggedDataModel;

public class VizController extends Controller {

    private static final String TAG = "VizController";

    private static native void requestData(String url, String sessionId, TreeModel tag,
            VizController handler) /*-{
        var isIE8 = window.XDomainRequest ? true : false;
        var xhr = createCrossDomainRequest();

        function createCrossDomainRequest() {
            if (isIE8) { return new window.XDomainRequest(); } 
            else { return new XMLHttpRequest(); }
        }

        function readyStateHandler() {
            if (xhr.readyState == 4) {
                if (xhr.status == 200) { outputResult(); } 
                else if (xhr.status == 403) { outputAuthentication(); } 
                else { outputError(); }
            }
        }

        function outputAuthentication() {
            handler.@nl.sense_os.commonsense.client.mvc.controllers.VizController::handleDataAuthError()();
        }

        function outputError() {
            handler.@nl.sense_os.commonsense.client.mvc.controllers.VizController::handleDataFailed(Lcom/extjs/gxt/ui/client/data/TreeModel;)(tag);
        }

        function outputResult() {
            handler.@nl.sense_os.commonsense.client.mvc.controllers.VizController::handleDataResponse(Ljava/lang/String;Lcom/extjs/gxt/ui/client/data/TreeModel;)(xhr.responseText,tag);
        }

        if (xhr) {
            if (isIE8) {
                url = url + "&session_id=" + sessionId;
                xhr.open("GET", url);
                xhr.onload = outputResult;
                xhr.onerror = outputError;
                xhr.send();
            } else {
                xhr.open('GET', url, true);
                xhr.onreadystatechange = readyStateHandler;
                xhr.setRequestHeader("X-SESSION_ID",sessionId);
                xhr.send();
            }
        } else {
            outputError();
        }
    }-*/;
    
    private static native void requestGroups(String url, String sessionId, VizController handler) /*-{
        var isIE8 = window.XDomainRequest ? true : false;
        var xhr = createCrossDomainRequest();

        function createCrossDomainRequest() {
            if (isIE8) { return new window.XDomainRequest(); } 
            else { return new XMLHttpRequest(); }
        }

        function readyStateHandler() {
            if (xhr.readyState == 4) {
                if (xhr.status == 200) { outputResult(); } 
                else if (xhr.status == 403) { outputAuthentication(); } 
                else { outputError(); }
            }
        }

        function outputAuthentication() {
            handler.@nl.sense_os.commonsense.client.mvc.controllers.VizController::handleGroupAuthError()()();
        }

        function outputError() {
            handler.@nl.sense_os.commonsense.client.mvc.controllers.VizController::handleGroupsFailed()();
        }

        function outputResult() {
            handler.@nl.sense_os.commonsense.client.mvc.controllers.VizController::handleGroupsResponse(Ljava/lang/String;)(xhr.responseText);
        }

        if (xhr) {
            if (isIE8) {
                url = url + "&session_id=" + sessionId;
                xhr.open("GET", url);
                xhr.onload = outputResult;
                xhr.onerror = outputError;
                xhr.send();
            } else {
                xhr.open('GET', url, true);
                xhr.onreadystatechange = readyStateHandler;
                xhr.setRequestHeader("X-SESSION_ID",sessionId);
                xhr.send();
            }
        } else {
            outputError();
        }
    }-*/;

    VizView vizView;

    public VizController() {
        registerEventTypes(MainEvents.Init);
        registerEventTypes(LoginEvents.LoggedIn, LoginEvents.LoggedOut);
        registerEventTypes(VizEvents.GroupsRequested, VizEvents.GroupsNotUpdated,
                VizEvents.GroupsUpdated);
        registerEventTypes(VizEvents.TagsRequested, VizEvents.TagsNotUpdated, VizEvents.TagsUpdated);
        registerEventTypes(VizEvents.DataRequested, VizEvents.DataNotReceived, VizEvents.DataReceived);
    }

    private void handleGroupAuthError() {
        Dispatcher.forwardEvent(VizEvents.GroupsNotUpdated);
    }

    private void handleDataAuthError() {
        Dispatcher.forwardEvent(VizEvents.DataNotReceived);
    }

    private void handleDataFailed(TreeModel tag) {
        Dispatcher.forwardEvent(VizEvents.DataNotReceived);
    }

    private void handleDataResponse(String response, TreeModel tag) {
        try {
            JSONObject obj = JSONParser.parseStrict(response).isObject();
            JSONArray data = obj.get("data").isArray();

            Log.d(TAG, "Received " + data.size() + " sensor data points");

            SensorValueModel[] values = new SensorValueModel[data.size()];
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

            if (values.length > 0) {
                String typeString = "";
                switch (values[0].getType()) {
                case SensorValueModel.BOOL:
                    typeString = "BOOL";
                    break;
                case SensorValueModel.FLOAT:
                    typeString = "FLOAT";
                    break;
                case SensorValueModel.JSON:
                    typeString = "JSON";
                    break;
                case SensorValueModel.STRING:
                    typeString = "STRING";
                    break;
                }
                Log.d(TAG, "Data type: " + typeString);
            }

            TagModel mdl = new TagModel(tag.<String> get("name") + "/", 0, 0, TagModel.TYPE_SENSOR);
            TaggedDataModel taggedData = new TaggedDataModel(mdl, values);
            Dispatcher.forwardEvent(VizEvents.DataReceived, taggedData);

        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException handling sensor data: " + e.getMessage());
            handleDataFailed(tag);
        }        
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType eventType = event.getType();
        if (eventType.equals(VizEvents.GroupsRequested)) {
            Log.d(TAG, "onGroupsRequested");
            onGroupsRequested(event);
        } else if (eventType.equals(VizEvents.TagsRequested)) {
            Log.d(TAG, "onTagsRequested");
            onTagsRequested(event);
        } else if (eventType.equals(VizEvents.DataRequested)) {
            Log.d(TAG, "onDataRequested");
            onDataRequested(event);
        } else {
            forwardToView(vizView, event);
        }
    }

    private void handleGroupsFailed() {
        Dispatcher.forwardEvent(VizEvents.GroupsNotUpdated);
    }

    private void handleGroupsResponse(String response) {
        try {
            JSONObject obj = JSONParser.parseStrict(response).isObject();
            JSONArray groups = obj.get("groups").isArray();
            
            List<TreeModel> groupModels = new ArrayList<TreeModel>();
            for (int i=0; i < groups.size(); i++) {
                
                JSONObject group = groups.get(i).isObject();
                
                TreeModel groupModel = new BaseTreeModel();
                groupModel.set("group_id", group.get("group_id").isString().stringValue());
                groupModel.set("user_id", group.get("user_id").isString().stringValue());
                
                groupModels.add(groupModel);
            }
            
            Dispatcher.forwardEvent(VizEvents.GroupsUpdated, groupModels);
        
        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException handling groups: " + e.getMessage());
            handleGroupsFailed();
        }  
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.vizView = new VizView(this);
    }

    private void onDataRequested(AppEvent event) {
        TreeModel tag = event.getData("tag");
        String url = Constants.URL_DATA.replace("<id>", "" + tag.<String> get("id"));
        url += "?per_page=" + 1000;
        url += "&start_date=" + event.<Double> getData("startDate");
        url += "&end_date=" + event.<Double> getData("endDate");
        String sessionId = Registry.get(Constants.REG_SESSION_ID);
        
        tag.set("retryCount", 0);
        requestData(url, sessionId, tag, this);
    }
    
    private void onGroupsRequested(AppEvent event) {        
        String url = Constants.URL_GROUPS;
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        requestGroups(url, sessionId, this);
    }
    
    private void onTagsRequested(AppEvent event) {        
        TagServiceAsync service = Registry.<TagServiceAsync> get(Constants.REG_TAG_SVC);
        String sessionId = Registry.get(Constants.REG_SESSION_ID);
        AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(VizEvents.TagsNotUpdated, caught);
            }

            @Override
            public void onSuccess(List<TreeModel> result) {
                Dispatcher.forwardEvent(VizEvents.TagsUpdated, result);
            }
        };
        service.getTags(sessionId, callback);
    }
}
