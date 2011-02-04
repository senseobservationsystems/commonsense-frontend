package nl.sense_os.commonsense.client.controllers.cors;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.controllers.StateController;
import nl.sense_os.commonsense.client.utility.Log;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class StatesJsniRequests {

    private static final String TAG = "StatesJsniRequests";

    /**
     * Adds a service to a sensor on CommonSense. Calls back to
     * {@link StateController#createCallback())}, or
     * {@link StateController#createServiceErrorCallback()}.
     * 
     * @param url
     *            URL of /sensors/<id>/services POST method
     * @param sessionId
     * @param data
     *            String with JSON encoded parameters to POST
     * @param handler
     *            StateController object to handle the callbacks
     * @see <a href="http://goo.gl/ajJWN">Making cross domain JavaScript requests</a>
     */
    // @formatter:off
    public native static void connectService(String url, String sessionId, String data,
            StateController handler) /*-{
        var isIE8 = window.XDomainRequest ? true : false;
        var xhr = createCrossDomainRequest();

        function createCrossDomainRequest() {
            if (isIE8) { return new window.XDomainRequest(); } 
            else { return new XMLHttpRequest(); }
        }

        function readyStateHandler() {
            if (xhr.readyState == 4) {
            if (xhr.status == 200) { outputResult(); } 
            else if (xhr.status == 403) { outputIncorrect(); }
            else { outputError(); }
            }
        }

        function outputIncorrect() {
            handler.@nl.sense_os.commonsense.client.controllers.StateController::connectServiceErrorCallback()();
        }

        function outputError() {
            handler.@nl.sense_os.commonsense.client.controllers.StateController::connectServiceErrorCallback()();
        }

        function outputResult() {
            handler.@nl.sense_os.commonsense.client.controllers.StateController::connectServiceCallback()();
        }

        if (xhr) {
            if (isIE8) {
                xhr.open("POST", url + ".json" + "?session_id=" + sessionId);
                xhr.onload = outputResult;
                xhr.onerror = outputIncorrect;
                xhr.ontimeout = outputError;
                xhr.send(data);
            } else {
                xhr.open('POST', url + ".json", true);
                xhr.onreadystatechange = readyStateHandler;
                xhr.setRequestHeader("X-SESSION_ID", sessionId);
                xhr.setRequestHeader("Content-Type","application/json");
                xhr.send(data);
            }
        } else {
            outputError();
        }
    }-*/;
    // @formatter:on

    /**
     * Create a service at CommonSense. Calls back to
     * {@link StateController#createServiceCallback()}, or
     * {@link StateController#createServiceErrorCallback()}.
     * 
     * @param url
     *            URL of /sensor POST method
     * @param sessionId
     * @param data
     *            String with JSON encoded parameters to POST
     * @param handler
     *            StateController object to handle the callbacks
     * @see <a href="http://goo.gl/ajJWN">Making cross domain JavaScript requests</a>
     */
    // @formatter:off
    public native static void createService(String url, String sessionId, String data,
            StateController handler) /*-{
        var isIE8 = window.XDomainRequest ? true : false;
        var xhr = createCrossDomainRequest();

        function createCrossDomainRequest() {
            if (isIE8) { return new window.XDomainRequest(); } 
            else { return new XMLHttpRequest(); }
        }

        function readyStateHandler() {
            if (xhr.readyState == 4) {
                if (xhr.status == 200) { outputResult(); } 
                else if (xhr.status == 403) { outputIncorrect(); }
                else { outputError(); }
            }
        }

        function outputIncorrect() {
            handler.@nl.sense_os.commonsense.client.controllers.StateController::createServiceErrorCallback()();
        }

        function outputError() {
            handler.@nl.sense_os.commonsense.client.controllers.StateController::createServiceErrorCallback()();
        }

        function outputResult() {
            handler.@nl.sense_os.commonsense.client.controllers.StateController::createServiceCallback()();
        }

        if (xhr) {
            if (isIE8) {
                xhr.open("POST", url + ".json" + "?session_id=" + sessionId);
                xhr.onload = outputResult;
                xhr.onerror = outputIncorrect;
                xhr.ontimeout = outputError;
                xhr.send(data);
            } else {
                xhr.open('POST', url + ".json", true);
                xhr.onreadystatechange = readyStateHandler;
                xhr.setRequestHeader("X-SESSION_ID", sessionId);
                xhr.setRequestHeader("Content-Type","application/json");
                xhr.send(data);
            }
        } else {
            outputError();
        }
    }-*/;
    // @formatter:on

    /**
     * Disconnects a service from a sensor on CommonSense. Calls back to
     * {@link StateController#disconnectServiceErrorCallback())}, or
     * {@link StateController#disconnectServiceErrorCallback()}.
     * 
     * @param url
     *            URL of /sensor/<id>/service/<id> DELETE method
     * @param sessionId
     * @param handler
     *            StateController object to handle the callbacks
     * @see <a href="http://goo.gl/ajJWN">Making cross domain JavaScript requests</a>
     */
    // @formatter:off
    public native static void disconnectService(String url, String sessionId,
            StateController handler) /*-{
        var isIE8 = window.XDomainRequest ? true : false;
        var xhr = createCrossDomainRequest();

        function createCrossDomainRequest() {
            if (isIE8) { return new window.XDomainRequest(); } 
            else { return new XMLHttpRequest(); }
        }

        function readyStateHandler() {
            if (xhr.readyState == 4) {
                if (xhr.status == 200) { outputResult(); } 
                else if (xhr.status == 403) { outputIncorrect(); }
                else { outputError(); }
            }
        }

        function outputIncorrect() {
            handler.@nl.sense_os.commonsense.client.controllers.StateController::disconnectServiceErrorCallback()();
        }

        function outputError() {
            handler.@nl.sense_os.commonsense.client.controllers.StateController::disconnectServiceErrorCallback()();
        }

        function outputResult() {
            handler.@nl.sense_os.commonsense.client.controllers.StateController::disconnectServiceCallback()();
        }

        if (xhr) {
            if (isIE8) {
                xhr.open("GET", url + "?session_id=" + sessionId + "&_METHOD=DELETE");
                xhr.onload = outputResult;
                xhr.onerror = outputIncorrect;
                xhr.ontimeout = outputError;
                xhr.send();
            } else {
                xhr.open("DELETE", url, true);
                xhr.onreadystatechange = readyStateHandler;
                xhr.setRequestHeader("X-SESSION_ID", sessionId);
                xhr.setRequestHeader("Content-Type","application/json");
                xhr.send();
            }
        } else {
            outputError();
        }
    }-*/;
    // @formatter:on

    /**
     * Gets a service's methods from CommonSense. Calls back to
     * {@link StateController#getServiceMethodsCallback())}, or
     * {@link StateController#getServiceMethodsErrorCallback()}.
     * 
     * @param url
     *            URL of /sensors/<id>/services POST method
     * @param sessionId
     * @param data
     *            String with JSON encoded parameters to POST
     * @param handler
     *            StateController object to handle the callbacks
     * @see <a href="http://goo.gl/ajJWN">Making cross domain JavaScript requests</a>
     */
    // @formatter:off
    public native static void getServiceMethods(String url, String sessionId,
            StateController handler) /*-{
        var isIE8 = window.XDomainRequest ? true : false;
        var xhr = createCrossDomainRequest();

        function createCrossDomainRequest() {
            if (isIE8) { return new window.XDomainRequest(); } 
            else { return new XMLHttpRequest(); }
        }

        function readyStateHandler() {
            if (xhr.readyState == 4) {
                if (xhr.status == 200) { outputResult(); } 
                else if (xhr.status == 403) { outputIncorrect(); }
                else { outputError(); }
            }
        }

        function outputIncorrect() {
            handler.@nl.sense_os.commonsense.client.controllers.StateController::getServiceMethodsErrorCallback()();
        }

        function outputError() {
            handler.@nl.sense_os.commonsense.client.controllers.StateController::getServiceMethodsErrorCallback()();
        }

        function outputResult() {
            @nl.sense_os.commonsense.client.controllers.cors.StatesJsniRequests::parseServiceMethods(Ljava/lang/String;Lnl/sense_os/commonsense/client/controllers/StateController;)(xhr.responseText, handler);
        }

        if (xhr) {
            if (isIE8) {
                xhr.open("GET", url + "?session_id=" + sessionId);
                xhr.onload = outputResult;
                xhr.onerror = outputIncorrect;
                xhr.ontimeout = outputError;
                xhr.send();
            } else {
                xhr.open('GET', url, true);
                xhr.onreadystatechange = readyStateHandler;
                xhr.setRequestHeader("X-SESSION_ID", sessionId);
                xhr.setRequestHeader("Accept","application/json");
                xhr.send();
            }
        } else {
            outputError();
        }
    }-*/;
    // @formatter:on

    /**
     * Calls a service method at CommonSense. Calls back to
     * {@link StateController#invokeMethodCallback(String)}, or
     * {@link StateController#invokeMethodErrorCallback()}.
     * 
     * @param url
     *            URL of /sensor/<id>/services/<id>/$method GET method
     * @param sessionId
     * @param handler
     *            StateController object to handle the callbacks
     * @see <a href="http://goo.gl/ajJWN">Making cross domain JavaScript requests</a>
     */
    // @formatter:off
    public native static void invokeGetMethod(String url, String sessionId,
            StateController handler) /*-{
        var isIE8 = window.XDomainRequest ? true : false;
        var xhr = createCrossDomainRequest();

        function createCrossDomainRequest() {
            if (isIE8) { return new window.XDomainRequest(); } 
            else { return new XMLHttpRequest(); }
        }

        function readyStateHandler() {
            if (xhr.readyState == 4) {
                if (xhr.status == 200) { outputResult(); } 
                else if (xhr.status == 403) { outputIncorrect(); }
                else { outputError(); }
            }
        }

        function outputIncorrect() {
            handler.@nl.sense_os.commonsense.client.controllers.StateController::invokeMethodErrorCallback()();
        }

        function outputError() {
            handler.@nl.sense_os.commonsense.client.controllers.StateController::invokeMethodErrorCallback()();
        }

        function outputResult() {
            @nl.sense_os.commonsense.client.controllers.cors.StatesJsniRequests::parseMethodResult(Ljava/lang/String;Lnl/sense_os/commonsense/client/controllers/StateController;)(xhr.responseText, handler);
        }

        if (xhr) {
            if (isIE8) {
                xhr.open("GET", url + ".json" + "?session_id=" + sessionId);
                xhr.onload = outputResult;
                xhr.onerror = outputIncorrect;
                xhr.ontimeout = outputError;
                xhr.send();
            } else {
                xhr.open('GET', url + ".json", true);
                xhr.onreadystatechange = readyStateHandler;
                xhr.setRequestHeader("X-SESSION_ID", sessionId);
                xhr.setRequestHeader("Accept","application/json");
                xhr.send();
            }
        } else {
            outputError();
        }
    }-*/;
    // @formatter:on

    /**
     * Calls a service method at CommonSense. Calls back to
     * {@link StateController#invokeMethodCallback(String)}, or
     * {@link StateController#invokeMethodErrorCallback()}.
     * 
     * @param url
     *            URL of /sensor/<id>/services/<id>/$method POST method
     * @param sessionId
     * @param data
     *            String with JSON encoded parameters to POST
     * @param handler
     *            StateController object to handle the callbacks
     * @see <a href="http://goo.gl/ajJWN">Making cross domain JavaScript requests</a>
     */
    // @formatter:off
    public native static void invokePostMethod(String url, String sessionId, String data,
            StateController handler) /*-{
        var isIE8 = window.XDomainRequest ? true : false;
        var xhr = createCrossDomainRequest();

        function createCrossDomainRequest() {
            if (isIE8) { return new window.XDomainRequest(); } 
            else { return new XMLHttpRequest(); }
        }

        function readyStateHandler() {
            if (xhr.readyState == 4) {
                if (xhr.status == 200) { outputResult(); } 
                else if (xhr.status == 403) { outputIncorrect(); }
                else { outputError(); }
            }
        }

        function outputIncorrect() {
            handler.@nl.sense_os.commonsense.client.controllers.StateController::invokeMethodErrorCallback()();
        }

        function outputError() {
            handler.@nl.sense_os.commonsense.client.controllers.StateController::invokeMethodErrorCallback()();
        }

        function outputResult() {
            @nl.sense_os.commonsense.client.controllers.cors.StatesJsniRequests::parseMethodResult(Ljava/lang/String;Lnl/sense_os/commonsense/client/controllers/StateController;)(xhr.responseText, handler);
        }

        if (xhr) {
            if (isIE8) {
                xhr.open("POST", url + ".json" + "?session_id=" + sessionId);
                xhr.onload = outputResult;
                xhr.onerror = outputIncorrect;
                xhr.ontimeout = outputError;
                xhr.send(data);
            } else {
                xhr.open('POST', url + ".json", true);
                xhr.onreadystatechange = readyStateHandler;
                xhr.setRequestHeader("X-SESSION_ID", sessionId);
                xhr.setRequestHeader("Content-Type","application/json");
                xhr.send(data);
            }
        } else {
            outputError();
        }
    }-*/;
    // @formatter:on

    private static void parseServiceMethods(String response, StateController handler) {
        if (response != null) {
            // try to get "methods" array
            JSONObject json = JSONParser.parseStrict(response).isObject();
            JSONValue jsonVal = json.get("methods");
            if (null != jsonVal) {
                JSONArray jsonArray = jsonVal.isArray();
                if (null != jsonArray) {
                    List<ModelData> methods = new ArrayList<ModelData>();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject methodObj = jsonArray.get(i).isObject();

                        ModelData m = new BaseModelData();
                        m.set("name", methodObj.get("name").isString().stringValue());
                        m.set("return", methodObj.get("return value").isString().stringValue());
                        JSONArray params = methodObj.get("parameters").isArray();
                        List<String> paramsList = new ArrayList<String>();
                        for (int j = 0; j < params.size(); j++) {
                            paramsList.add(params.get(j).isString().stringValue());
                        }
                        m.set("parameters", paramsList);
                        methods.add(m);
                    }
                    handler.getServiceMethodsCallback(methods);
                } else {
                    Log.e(TAG,
                            "Error parsing service methods response: \"methods\" is not a JSON Array");
                    handler.getServiceMethodsErrorCallback();
                }
            } else {
                Log.e(TAG, "Error parsing service methods response: \"methods\" is is not found");
                handler.getServiceMethodsErrorCallback();
            }
        } else {
            Log.e(TAG, "Error parsing service methods response: response=null");
            handler.getServiceMethodsErrorCallback();
        }
    }

    private static void parseMethodResult(String response, StateController handler) {
        if (response != null) {
            // try to get "methods" array
            JSONObject json = JSONParser.parseStrict(response).isObject();
            JSONValue jsonVal = json.get("result");
            if (null != jsonVal) {
                JSONString jsonResult = jsonVal.isString();
                if (null != jsonResult) {
                    String result = jsonResult.stringValue();
                    handler.invokeMethodCallback(result);
                } else {
                    Log.e(TAG,
                            "Error parsing service methods response: \"result\" is not a JSON String");
                    handler.invokeMethodErrorCallback();
                }
            } else {
                Log.e(TAG, "Error parsing service methods response: \"result\" is is not found");
                handler.invokeMethodErrorCallback();
            }
        } else {
            Log.e(TAG, "Error parsing service methods response: response=null");
            handler.invokeMethodErrorCallback();
        }
    }
}
