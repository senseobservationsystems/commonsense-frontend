package nl.sense_os.commonsense.client.controllers.cors;

import java.util.List;

import nl.sense_os.commonsense.client.controllers.MySensorsController;

import com.extjs.gxt.ui.client.data.TreeModel;

public class MySensorsJsniRequests {

    // @formatter:off
    public native static void shareSensor(String url, String sessionId, String data, List<TreeModel> sensors,
            MySensorsController handler) /*-{
        var isIE8 = window.XDomainRequest ? true : false;
        var xhr = createCrossDomainRequest();

        function createCrossDomainRequest() {
            if (isIE8) { return new window.XDomainRequest(); } 
            else { return new XMLHttpRequest(); }
        }

        function readyStateHandler() {
            if (xhr.readyState == 4) {
                if (xhr.status == 201) { outputResult(); } 
                else if (xhr.status == 403) { outputIncorrect(); }
                else { outputError(); }
            }
        }

        function outputIncorrect() {
            handler.@nl.sense_os.commonsense.client.controllers.MySensorsController::shareSensorErrorCallback()();
        }

        function outputError() {
            handler.@nl.sense_os.commonsense.client.controllers.MySensorsController::shareSensorErrorCallback()();
        }

        function outputResult() {
            handler.@nl.sense_os.commonsense.client.controllers.MySensorsController::shareSensorCallback(Ljava/util/List;Ljava/lang/String;)(sensors,data);
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
}
