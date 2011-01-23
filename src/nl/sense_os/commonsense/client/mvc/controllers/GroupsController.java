package nl.sense_os.commonsense.client.mvc.controllers;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.mvc.events.GroupsEvents;
import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.views.GroupsView;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

public class GroupsController extends Controller {

    private static final String TAG = "GroupsController";
    private GroupsView groupView;

    private static native void requestGroups(String url, String sessionId, GroupsController handler) /*-{
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
            handler.@nl.sense_os.commonsense.client.mvc.controllers.GroupsController::handleGroupAuthError()()();
        }

        function outputError() {
            handler.@nl.sense_os.commonsense.client.mvc.controllers.GroupsController::handleGroupsFailed()();
        }

        function outputResult() {
            handler.@nl.sense_os.commonsense.client.mvc.controllers.GroupsController::handleGroupsResponse(Ljava/lang/String;)(xhr.responseText);
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
    
    public GroupsController() {
        registerEventTypes(GroupsEvents.GroupsNotUpdated, GroupsEvents.GroupsRequested, GroupsEvents.ShowGroups, GroupsEvents.GroupsUpdated);
        registerEventTypes(LoginEvents.LoggedOut);
    }
    
    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(GroupsEvents.GroupsRequested)) {
            Log.d(TAG, "GroupsRequested");
            onGroupsRequested(event);
        }
        forwardToView(this.groupView, event);
    }


    private void handleGroupAuthError() {
        Dispatcher.forwardEvent(GroupsEvents.GroupsNotUpdated);
    }

    private void handleGroupsFailed() {
        Dispatcher.forwardEvent(GroupsEvents.GroupsNotUpdated);
    }

    private void handleGroupsResponse(String response) {
        try {
            JSONObject obj = JSONParser.parseStrict(response).isObject();
            JSONArray groups = obj.get("groups").isArray();

            List<TreeModel> groupModels = new ArrayList<TreeModel>();
            for (int i = 0; i < groups.size(); i++) {

                JSONObject group = groups.get(i).isObject();

                TreeModel groupModel = new BaseTreeModel();
                groupModel.set("group_id", group.get("group_id").isString().stringValue());
                groupModel.set("user_id", group.get("user_id").isString().stringValue());

                groupModels.add(groupModel);
            }

            Dispatcher.forwardEvent(GroupsEvents.GroupsUpdated, groupModels);

        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException handling groups: " + e.getMessage());
            handleGroupsFailed();
        }
    }

    private void onGroupsRequested(AppEvent event) {
        String url = Constants.URL_GROUPS;
        String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        requestGroups(url, sessionId, this);
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.groupView = new GroupsView(this);
    }
}
