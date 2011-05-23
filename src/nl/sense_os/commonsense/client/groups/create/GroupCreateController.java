package nl.sense_os.commonsense.client.groups.create;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.utility.Md5Hasher;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;

public class GroupCreateController extends Controller {

    private static final Logger logger = Logger.getLogger("GroupCreateController");
    private View creator;

    public GroupCreateController() {
        registerEventTypes(GroupCreateEvents.ShowCreator, GroupCreateEvents.CreateComplete,
                GroupCreateEvents.CreateRequested, GroupCreateEvents.CreateAjaxFailure,
                GroupCreateEvents.CreateAjaxSuccess);
    }

    private void createGroup(String name, String username, String password) {

        // prepare request properties
        final String method = "POST";
        final String url = Urls.GROUPS + ".json";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(GroupCreateEvents.CreateAjaxSuccess);
        final AppEvent onFailure = new AppEvent(GroupCreateEvents.CreateAjaxFailure);

        // prepare request body
        String body = "{\"group\":{";
        body += "\"name\":\"" + name + "\"";
        if (null != username) {
            body += ",\"username\":\"" + username + "\"";
            body += ",\"password\":\"" + Md5Hasher.hash(password) + "\"";
        }
        body += "}}";

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("session_id", sessionId);
        ajaxRequest.setData("body", body);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);
        Dispatcher.forwardEvent(ajaxRequest);
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(GroupCreateEvents.CreateRequested)) {
            // logger.fine( "CreateRequested");
            final String name = event.getData("name");
            final String username = event.getData("username");
            final String password = event.getData("password");
            createGroup(name, username, password);

        } else if (type.equals(GroupCreateEvents.CreateAjaxFailure)) {
            logger.warning("CreateAjaxFailure");
            onCreateFailure();

        } else if (type.equals(GroupCreateEvents.CreateAjaxSuccess)) {
            // logger.fine( "CreateAjaxSuccess");
            onCreateSuccess();

        } else

        /*
         * Pass through to view
         */
        {
            forwardToView(this.creator, event);

        }
    }

    private void onCreateSuccess() {
        Dispatcher.forwardEvent(GroupCreateEvents.CreateComplete);
    }

    private void onCreateFailure() {
        forwardToView(this.creator, new AppEvent(GroupCreateEvents.CreateFailed));
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.creator = new GroupCreator(this);
    }
}
