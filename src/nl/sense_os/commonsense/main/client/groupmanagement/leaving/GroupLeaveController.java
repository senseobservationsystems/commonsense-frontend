package nl.sense_os.commonsense.main.client.groupmanagement.leaving;

import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.groupmanagement.leaving.component.GxtConfirmLeaveDialog;
import nl.sense_os.commonsense.main.client.groupmanagement.leaving.component.GxtLeavingCompleteDialog;
import nl.sense_os.commonsense.main.client.groupmanagement.leaving.component.GxtLeavingFailureDialog;
import nl.sense_os.commonsense.main.client.gxt.model.GxtGroup;
import nl.sense_os.commonsense.main.client.gxt.model.GxtUser;
import nl.sense_os.commonsense.shared.client.communication.CommonSenseApi;
import nl.sense_os.commonsense.shared.client.util.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public class GroupLeaveController implements ConfirmGroupLeaveView.Presenter {

    private static final Logger LOG = Logger.getLogger(GroupLeaveController.class.getName());
    private GxtGroup group;
    private ConfirmGroupLeaveView confirmDialog;
    private LeavingCompleteView successDialog;
    private LeavingFailureView failureDialog;

    public GroupLeaveController(MainClientFactory clientFactory) {

    }

    private void leave(GxtGroup group) {

        GxtUser user = Registry.get(Constants.REG_USER);

        // prepare request callback
        RequestCallback callback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                onLeaveFailure(-1, exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("DELETE group response received: " + response.getStatusText());
                int statusCode = response.getStatusCode();
                if (Response.SC_OK == statusCode) {
                    onLeaveSuccess();
                } else {
                    onLeaveFailure(statusCode, new Throwable(response.getStatusText()));
                }
            }
        };

        // send request
        CommonSenseApi.removeGroupUser(callback, group.getId(), user.getId());
    }

    @Override
    public void onCancelClick() {
        confirmDialog.hide();

        if (null != successDialog) {
            successDialog.hide();
            successDialog = null;
        }

        if (null != failureDialog) {
            failureDialog.hide();
            failureDialog = null;
        }
    }

    @Override
    public void onLeaveClick() {
        leave(group);
    }

    private void onLeaveFailure(int code, Throwable error) {
        confirmDialog.setBusy(false);

        failureDialog = new GxtLeavingFailureDialog();
        failureDialog.setPresenter(this);
        failureDialog.show(code, error);
    }

    private void onLeaveSuccess() {
        confirmDialog.setBusy(false);
        confirmDialog.hide();

        successDialog = new GxtLeavingCompleteDialog();
        successDialog.setPresenter(this);
        successDialog.show();
    }

    public void start(GxtGroup group) {
        this.group = group;

        confirmDialog = new GxtConfirmLeaveDialog();
        confirmDialog.setPresenter(this);
        confirmDialog.show(group.getName());
    }
}
