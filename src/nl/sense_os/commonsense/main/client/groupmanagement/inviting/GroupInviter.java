package nl.sense_os.commonsense.main.client.groupmanagement.inviting;

import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.groupmanagement.inviting.component.GxtInvitationCompleteDialog;
import nl.sense_os.commonsense.main.client.groupmanagement.inviting.component.GxtInvitationFailureDialog;
import nl.sense_os.commonsense.main.client.groupmanagement.inviting.component.GxtInviteUserDialog;
import nl.sense_os.commonsense.main.client.gxt.model.GxtGroup;
import nl.sense_os.commonsense.shared.client.communication.CommonSenseApi;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public class GroupInviter implements InviteUserView.Presenter {

	private static final Logger LOG = Logger.getLogger(GroupInviter.class.getName());

    private GxtGroup group;

    private InviteUserView inviteDialog;
    private InvitationCompleteView successDialog;
    private InvitationFailureView failureDialog;

    public GroupInviter(MainClientFactory clientFactory) {

	}

    private void inviteUser(String username) {

		// prepare request callback
        RequestCallback callback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
                onInviteFailure(-1, exception);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				int statusCode = response.getStatusCode();
				if (Response.SC_CREATED == statusCode) {
                    onInviteSuccess();
				} else {
                    onInviteFailure(statusCode, new Throwable(response.getStatusText()));
				}
			}
		};

		// send request
        CommonSenseApi.addGroupUser(callback, group.getId(), username);
	}

	@Override
    public void onCancelClick() {
        inviteDialog.hide();

        if (null != successDialog) {
            successDialog.hide();
            successDialog = null;
        }

        if (null != failureDialog) {
            failureDialog.hide();
            failureDialog = null;
        }
    }

    private void onInviteFailure(int code, Throwable error) {
        LOG.warning("Failed to add user to group! Code: " + code + " " + error);

        inviteDialog.setBusy(false);

        failureDialog = new GxtInvitationFailureDialog();
        failureDialog.setPresenter(this);
        failureDialog.show(code, error);
	}

    private void onInviteSuccess() {
        inviteDialog.setBusy(false);
        inviteDialog.hide();

        successDialog = new GxtInvitationCompleteDialog();
        successDialog.setPresenter(this);
        successDialog.show();
	}

    @Override
    public void onSubmitClick() {
        inviteDialog.setBusy(true);

        if (null != failureDialog) {
            failureDialog.hide();
            failureDialog = null;
        }

        String username = inviteDialog.getUsername();
        inviteUser(username);
    }

    public void start(GxtGroup group) {
        this.group = group;

        inviteDialog = new GxtInviteUserDialog();
        inviteDialog.setPresenter(this);
        inviteDialog.show();
    }

}
