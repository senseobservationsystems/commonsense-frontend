package nl.sense_os.commonsense.client.groups.invite;

import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;

public class InviteController extends Controller {

    private static final String TAG = "InviteController";
    private View inviter;

    public InviteController() {
        registerEventTypes(InviteEvents.ShowInviter, InviteEvents.InviteComplete,
                InviteEvents.InviteRequested, InviteEvents.InviteAjaxFailure,
                InviteEvents.InviteAjaxSuccess);
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(InviteEvents.InviteAjaxFailure)) {
            Log.w(TAG, "InviteAjaxFailure");
            onInviteFailure();
            forwardToView(this.inviter, new AppEvent(InviteEvents.InviteFailed));

        } else if (type.equals(InviteEvents.InviteAjaxSuccess)) {
            // Log.d(TAG, "InviteAjaxSuccess");
            onInviteSuccess();

        } else if (type.equals(InviteEvents.InviteRequested)) {
            // Log.d(TAG, "InviteRequested");
            final String groupId = event.getData("groupId");
            final String email = event.getData("username");
            inviteUser(groupId, email);

        } else

        /*
         * Pass through to view
         */
        {
            forwardToView(this.inviter, event);
        }
    }

    private void onInviteSuccess() {
        Dispatcher.forwardEvent(InviteEvents.InviteComplete);
    }

    private void onInviteFailure() {
        forwardToView(this.inviter, new AppEvent(InviteEvents.InviteFailed));
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.inviter = new GroupInviter(this);
    }

    private void inviteUser(String groupId, String username) {

        // prepare request properties
        final String method = "POST";
        final String url = Constants.URL_GROUPS + "/" + groupId + "/users.json";
        final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
        final AppEvent onSuccess = new AppEvent(InviteEvents.InviteAjaxSuccess);
        final AppEvent onFailure = new AppEvent(InviteEvents.InviteAjaxFailure);

        // prepare request body
        String body = "{\"user\":{\"username\":\"" + username + "\"}}";

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

}
