package nl.sense_os.commonsense.main.client.groupmanagement.creating;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.groupmanagement.creating.component.GxtCreationFailureDialog;
import nl.sense_os.commonsense.main.client.groupmanagement.creating.component.GxtCreationSuccessDialog;
import nl.sense_os.commonsense.main.client.groupmanagement.creating.component.GxtGroupCreatorForm;
import nl.sense_os.commonsense.shared.client.communication.CommonSenseApi;
import nl.sense_os.commonsense.shared.client.model.Group;
import nl.sense_os.commonsense.shared.client.util.Md5Hasher;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public class GroupCreator implements GroupCreatorView.Presenter {

    private static final Logger LOG = Logger.getLogger(GroupCreator.class.getName());
    private GroupCreatorView creatorForm;
    private CreationSuccessView successDialog;
    private CreationFailureView failureDialog;

    public GroupCreator(MainClientFactory clientFactory) {

    }

    private void createGroup(Group group) {

        // prepare request callback
        RequestCallback callback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                onCreateFailure(-1, exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                int statusCode = response.getStatusCode();
                if (Response.SC_CREATED == statusCode) {
                    onCreateSuccess();
                } else {
                    onCreateFailure(statusCode, new Throwable(response.getStatusText()));
                }
            }
        };

        // send request
        CommonSenseApi.createGroup(callback, group);
    }

    public Group getGroupDetails() {
        Group group = JavaScriptObject.createObject().cast();
        group.setName(creatorForm.getGroupName());
        group.setDescription(creatorForm.getGroupDescription());

        String preset = creatorForm.getPresetChoice();
        if ("private".equals(preset)) {
            LOG.fine("private preset");
            group.setHidden(true);
            group.setPublic(false);
            group.setAnonymous(false);

            String clearPass = creatorForm.getAccessPassword();
            String hashedPass = Md5Hasher.hash(clearPass);
            group.setAccessPassword(hashedPass);

        } else if ("anonymous".equals(preset)) {
            LOG.fine("anonymous preset");
            group.setHidden(false);
            group.setPublic(true);
            group.setAnonymous(true);

        } else if ("community".equals(preset)) {
            LOG.fine("community preset");
            group.setHidden(false);
            group.setPublic(true);
            group.setAnonymous(false);

        } else if ("custom".equals(preset)) {
            LOG.fine("custom preset");
            group.setHidden(creatorForm.isGroupHidden());
            group.setPublic(creatorForm.isGroupPublic());
            group.setAnonymous(creatorForm.isGroupAnonymous());

            String accessPass = creatorForm.getAccessPassword();
            if (null != accessPass) {
                String hashedPass = Md5Hasher.hash(accessPass);
                group.setAccessPassword(hashedPass);
            }

            List<String> reqSensors = creatorForm.getReqSensors();
            if (null != reqSensors) {
                JsArrayString reqArray = JavaScriptObject.createArray().cast();
                for (String req : reqSensors) {
                    reqArray.push(req);
                }
                group.setReqSensors(reqArray);
            }

            List<String> optSensors = creatorForm.getOptSensors();
            if (null != optSensors) {
                JsArrayString optArray = JavaScriptObject.createArray().cast();
                for (String opt : optSensors) {
                    optArray.push(opt);
                }
                group.setOptSensors(optArray);
            }

            if (!creatorForm.isGroupAnonymous()) {
                group.setShowIdReq(creatorForm.isUserIdRequired());
                group.setShowUsernameReq(creatorForm.isUsernameRequired());
                group.setShowFirstNameReq(creatorForm.isFirstNameRequired());
                group.setShowSurnameReq(creatorForm.isSurnameRequired());
                group.setShowEmailReq(creatorForm.isEmailRequired());
                group.setShowPhoneReq(creatorForm.isPhoneRequired());
            }

            // third form: member rights
            group.setAllowReadSensors(creatorForm.isReadSensors());
            group.setAllowCreateSensors(creatorForm.isCreateSensors());
            group.setAllowDeleteSensors(creatorForm.isDeleteSensors());
            group.setAllowReadUsers(creatorForm.isReadMembers());
            group.setAllowCreateUsers(creatorForm.isCreateMembers());
            group.setAllowDeleteUsers(creatorForm.isDeleteMembers());

            // fourth form: group username
            if (creatorForm.isGroupLogin()) {
                group.setUsername(creatorForm.getGroupUsername());
                String groupPass = creatorForm.getGroupPassword();
                String hashedPass = Md5Hasher.hash(groupPass);
                group.setPassword(hashedPass);
            }

        } else {
            LOG.warning("Unexpected group preset selection: " + preset);
        }

        return group;
    }

    @Override
    public void onCancelClick() {
        creatorForm.setBusy(false);
        creatorForm.hide();

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
    public void onCreateClick() {
        creatorForm.setBusy(true);

        if (null != failureDialog) {
            failureDialog.hide();
            failureDialog = null;
        }

        Group group = getGroupDetails();
        createGroup(group);
    }

    private void onCreateFailure(int code, Throwable error) {
        creatorForm.setBusy(false);

        failureDialog = new GxtCreationFailureDialog();
        failureDialog.setPresenter(this);
        failureDialog.show(code, error);
    }

    private void onCreateSuccess() {
        creatorForm.setBusy(false);
        creatorForm.hide();

        successDialog = new GxtCreationSuccessDialog();
        successDialog.setPresenter(this);
        successDialog.show();
    }

    public void start() {
        creatorForm = new GxtGroupCreatorForm();
        creatorForm.setPresenter(this);
        creatorForm.show();
    }
}
