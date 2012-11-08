package nl.sense_os.commonsense.main.client.groupmanagement.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.groupmanagement.joining.component.GxtGroupDetailsFailureDialog;
import nl.sense_os.commonsense.main.client.groupmanagement.joining.component.GxtGroupJoinDialog;
import nl.sense_os.commonsense.main.client.groupmanagement.joining.component.GxtGroupListErrorDialog;
import nl.sense_os.commonsense.main.client.groupmanagement.joining.component.GxtGroupListWaitDialog;
import nl.sense_os.commonsense.main.client.groupmanagement.joining.component.GxtJoinFailureDialog;
import nl.sense_os.commonsense.main.client.groupmanagement.joining.component.GxtJoinSuccessDialog;
import nl.sense_os.commonsense.main.client.gxt.model.GxtGroup;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.model.GxtUser;
import nl.sense_os.commonsense.shared.client.communication.CommonSenseApi;
import nl.sense_os.commonsense.shared.client.communication.httpresponse.GetGroupDetailsResponse;
import nl.sense_os.commonsense.shared.client.communication.httpresponse.GetGroupsResponse;
import nl.sense_os.commonsense.shared.client.model.Group;
import nl.sense_os.commonsense.shared.client.util.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public class GroupJoiner implements GroupJoinView.Presenter {

	private static final Logger LOG = Logger.getLogger(GroupJoiner.class.getName());
	private static final int PER_PAGE = 1000;

    private GroupDetailsFailureView detailsFailureDialog;
    private JoinFailureView failureDialog;
    private GroupJoinView groupSelectionForm;
    private GroupListFailureView listFailureDialog;
    private JoinSuccessView successDialog;
    private GroupListWaitView waitDialog;

    public GroupJoiner(MainClientFactory clientFactory) {

	}

    private void getAllGroups(final List<GxtGroup> groups, final int page) {

		// prepare request callback
        RequestCallback callback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
                onAllGroupsFailure(-1, exception);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
                    onAllGroupsSuccess(response.getText(), groups, page);
				} else {
                    onAllGroupsFailure(statusCode, new Throwable(response.getStatusText()));
				}
			}
		};

		// send request
        CommonSenseApi.getAllGroups(callback, PER_PAGE, page);
	}

    private void getGroupDetails(GxtGroup group) {

		// prepare request callback
        RequestCallback callback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
                onGroupDetailsFailure(-1, exception);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
                    onGroupDetailsSuccess(response.getText());
				} else {
                    onGroupDetailsFailure(statusCode, new Throwable(response.getStatusText()));
				}
			}

		};

		// send request
		CommonSenseApi.getGroupDetails(callback, group.getId());
	}

    private void join(GxtGroup group, List<GxtSensor> sensors) {


		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
                onJoinFailure(-1, exception);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				int statusCode = response.getStatusCode();
				if (Response.SC_CREATED == statusCode) {
                    onJoinSuccess();
				} else {
                    onJoinFailure(statusCode, new Throwable(response.getStatusText()));
				}
			}
		};

        GxtUser user = Registry.<GxtUser> get(Constants.REG_USER);
        List<String> sensorIds = new ArrayList<String>();
        for (GxtSensor sensor : sensors) {
            sensorIds.add(sensor.getId());
        }

		// send request
        CommonSenseApi.joinGroup(reqCallback, group.getId(), user.getId(), sensorIds);
	}

    private void onAllGroupsComplete(List<GxtGroup> groups) {
        if (null != waitDialog) {
            waitDialog.hide();
            waitDialog = null;
        }

        // sensors
        List<GxtSensor> sensors = Registry.<List<GxtSensor>> get(Constants.REG_SENSOR_LIST);
        List<GxtSensor> ownedSensors = new ArrayList<GxtSensor>();
        GxtUser user = Registry.get(Constants.REG_USER);
        for (GxtSensor sensor : sensors) {
            if (sensor.getOwner().equals(user)) {
                ownedSensors.add(sensor);
            }
        }

        groupSelectionForm = new GxtGroupJoinDialog(groups, ownedSensors);
        groupSelectionForm.setPresenter(this);
        groupSelectionForm.show();
    }

    private void onAllGroupsFailure(int code, Throwable error) {
        if (null != waitDialog) {
            waitDialog.hide();
            waitDialog = null;
        }

        listFailureDialog = new GxtGroupListErrorDialog();
        listFailureDialog.setPresenter(this);
        listFailureDialog.show(code, error);
	}

    private void onAllGroupsSuccess(String response, List<GxtGroup> groups, int page) {

		// parse list of groups from the response
		int total = -1;
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			GetGroupsResponse jso = JsonUtils.unsafeEval(response);
			total = jso.getGroups().size();
			JsArray<Group> newGroups = jso.getRawGroups();
			for (int i = 0; i < newGroups.length(); i++) {
				groups.add(new GxtGroup(newGroups.get(i)));
			}
		}

		// check if there are more pages left
		if (total == PER_PAGE) {
			page++;
            getAllGroups(groups, page);
		} else {
            onAllGroupsComplete(groups);

		}
	}

    @Override
    public void onCancelClick() {
        groupSelectionForm.setBusy(false);
        groupSelectionForm.hide();

        if (null != detailsFailureDialog) {
            detailsFailureDialog.hide();
            detailsFailureDialog = null;
        }

        if (null != failureDialog) {
            failureDialog.hide();
            failureDialog = null;
        }

        if (null != listFailureDialog) {
            listFailureDialog.hide();
            listFailureDialog = null;
        }

        if (null != successDialog) {
            successDialog.hide();
            successDialog = null;
        }

        if (null != waitDialog) {
            waitDialog.hide();
            waitDialog = null;
        }
    }

    private void onGroupDetailsFailure(int code, Throwable error) {

        groupSelectionForm.setBusy(false);

        detailsFailureDialog = new GxtGroupDetailsFailureDialog();
        detailsFailureDialog.setPresenter(this);
        detailsFailureDialog.show(code, error);
	}

    @Override
    public void onGroupDetailsRequest(GxtGroup group) {
        groupSelectionForm.setBusy(true);
        getGroupDetails(group);
    }

    private void onGroupDetailsSuccess(String response) {
		Group group = null;
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			GetGroupDetailsResponse jso = JsonUtils.unsafeEval(response);
			group = jso.getGroup();
		}

		// convert to Ext
		GxtGroup gxtGroup = new GxtGroup(group);

        groupSelectionForm.setBusy(false);
        groupSelectionForm.setGroupDetails(gxtGroup);
	}

    private void onJoinFailure(int code, Throwable error) {
        LOG.warning("Failed to join group! Error: " + code + " " + error);

        groupSelectionForm.setBusy(false);

        failureDialog = new GxtJoinFailureDialog();
        failureDialog.setPresenter(this);
        failureDialog.show(code, error);
	}

    private void onJoinSuccess() {
        groupSelectionForm.setBusy(false);
        groupSelectionForm.hide();

        successDialog = new GxtJoinSuccessDialog();
        successDialog.setPresenter(this);
        successDialog.show();
	}

    @Override
    public void onSubmitClick() {
        groupSelectionForm.setBusy(true);

        GxtGroup group = groupSelectionForm.getGroup();
        List<GxtSensor> sensors = groupSelectionForm.getSharedSensors();
        join(group, sensors);
    }

    public void start() {

        waitDialog = new GxtGroupListWaitDialog();
        waitDialog.setPresenter(this);
        waitDialog.show();

        List<GxtGroup> groups = new ArrayList<GxtGroup>();
        int page = 0;
        getAllGroups(groups, page);
    }
}
