package nl.sense_os.commonsense.client.mvc.controllers;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.MainEvents;
import nl.sense_os.commonsense.client.mvc.events.VizEvents;
import nl.sense_os.commonsense.client.mvc.views.VizView;
import nl.sense_os.commonsense.client.services.TagServiceAsync;
import nl.sense_os.commonsense.shared.Constants;

public class VizController extends Controller {

    private static final String TAG = "VizController";

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
            handler.@nl.sense_os.commonsense.client.mvc.controllers.VizController::onGroupsAuthError()()();
        }

        function outputError() {
            handler.@nl.sense_os.commonsense.client.mvc.controllers.VizController::onGroupsFailed()();
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
                VizEvents.GroupsUpdated, VizEvents.TagsRequested, VizEvents.TagsNotUpdated,
                VizEvents.TagsUpdated);
    }

    private void getGroups(AppEvent event) {
        String url = Constants.URL_GROUPS;
        String sessionId = Registry.get(Constants.REG_SESSION_ID);
        requestGroups(url, sessionId, this);
    }

    private void getTags(AppEvent event) {
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

    @Override
    public void handleEvent(AppEvent event) {
        EventType eventType = event.getType();
        if (eventType.equals(VizEvents.GroupsRequested)) {
            getGroups(event);
        } else if (eventType.equals(VizEvents.TagsRequested)) {
            getTags(event);
        } else {
            forwardToView(vizView, event);
        }
    }

    private void handleGroupsResponse(String response) {
        Dispatcher.forwardEvent(VizEvents.GroupsUpdated, response);
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.vizView = new VizView(this);
    }

    private void onGroupsAuthError() {
        Dispatcher.forwardEvent(VizEvents.GroupsNotUpdated);
    }

    private void onGroupsFailed() {
        Dispatcher.forwardEvent(VizEvents.GroupsNotUpdated);
    }
}
