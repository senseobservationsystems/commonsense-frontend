package nl.sense_os.commonsense.client.controllers.cors;

import nl.sense_os.commonsense.client.controllers.FeedbackController;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;

/**
 * Helper class for cross-origin feedback requests using the JSNI. Has strict ties to
 * {@link FeedbackController}.
 */
public class FeedbackJsniRequests {

    private static final String TAG = "FeedbackJsniRequests";

    // @formatter:off
    public static native void manualLearn(String url, String sessionId, String data, 
            TreeModel service, String label, ModelData[] feedback, FeedbackController handler) /*-{
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
            handler.@nl.sense_os.commonsense.client.controllers.FeedbackController::onFeedbackFailed(Lcom/extjs/gxt/ui/client/data/TreeModel;Ljava/lang/String;[Lcom/extjs/gxt/ui/client/data/ModelData;)(service, label, feedback);
        }

        function outputError() {
            handler.@nl.sense_os.commonsense.client.controllers.FeedbackController::onFeedbackFailed(Lcom/extjs/gxt/ui/client/data/TreeModel;Ljava/lang/String;[Lcom/extjs/gxt/ui/client/data/ModelData;)(service, label, feedback);
        }

        function outputResult() {
            handler.@nl.sense_os.commonsense.client.controllers.FeedbackController::onFeedbackMarked(Lcom/extjs/gxt/ui/client/data/TreeModel;Ljava/lang/String;[Lcom/extjs/gxt/ui/client/data/ModelData;)(service, label, feedback);
        }

        if (xhr) {
            if (isIE8) {
                xhr.open("POST", url + ".json" + "&session_id=" + sessionId);
                xhr.onload = outputResult;
                xhr.onerror = outputIncorrect;
                xhr.ontimeout = outputError;
                xhr.send(data);
            } else {
                xhr.open('POST', url + ".json", true);
                xhr.onreadystatechange = readyStateHandler;
                xhr.setRequestHeader("X-SESSION_ID",sessionId);
                xhr.setRequestHeader("Content-Type","application/json");
                xhr.send(data);
            }
        } else {
            outputError();
        }
    }-*/;
    // @formatter:on
}
