package nl.sense_os.commonsense.main.client.groupmanagement;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.communication.CommonSenseApi;
import nl.sense_os.commonsense.common.client.communication.httpresponse.GetGroupUsersResponse;
import nl.sense_os.commonsense.common.client.communication.httpresponse.GetGroupsResponse;
import nl.sense_os.commonsense.common.client.model.Group;
import nl.sense_os.commonsense.common.client.model.User;
import nl.sense_os.commonsense.common.client.util.Constants;
import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.ext.model.ExtGroup;
import nl.sense_os.commonsense.main.client.ext.model.ExtUser;
import nl.sense_os.commonsense.main.client.groupmanagement.GroupManagementView.Presenter;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class GroupManagementActivity extends AbstractActivity implements Presenter {

	private static final Logger LOG = Logger.getLogger(GroupManagementActivity.class.getName());
	private MainClientFactory clientFactory;
	private GroupManagementView view;

	public GroupManagementActivity(GroupManagementPlace place, MainClientFactory clientFactory) {
		this.clientFactory = clientFactory;
		Registry.register(Constants.REG_GROUPS, new ArrayList<ExtGroup>());
	}

	/**
	 * Gets the members of a group (UserModels) from CommonSense, using an Ajax request. The
	 * response is handled by {@link #onGroupMembersSuccess(String, List, int, AsyncCallback)} or
	 * {@link #onGroupMembersFailure(List, int, AsyncCallback)}. If the members for all groups are
	 * complete, the requests are finished.
	 * 
	 * @param count
	 *            Count for the number of groups that already have members. The details are
	 *            requested one at a time, so this method is called once for each group.
	 * @param groups
	 *            List of group details that were received earlier.
	 * @param callback
	 *            Optional callback for a DataProxy. Will be called when the list of sensors is
	 *            complete.
	 */
	private void getGroupUsers(final ExtGroup group, final AsyncCallback<List<ExtUser>> callback) {

		// notify the view
		view.setBusy(true);

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				onGroupUsersFailure(-1, exception, group, callback);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onGroupMembersSuccess(response.getText(), group, callback);
				} else if (Response.SC_FORBIDDEN == statusCode) {
					onGroupUsersForbidden(group, callback);
				} else {
					onGroupUsersFailure(statusCode, new Throwable(response.getStatusText()), group,
							callback);
				}
			}
		};

		CommonSenseApi.getGroupUsers(reqCallback, group.getId(), null, null);
	}

	/**
	 * Gets a list of groups that the user is a member of, using an Ajax request to CommonSense. The
	 * response is handled by {@link #onGroupsSuccess(String, AsyncCallback)} or
	 * {@link #onGroupsFailure(AsyncCallback)}. Afterwards, the members of the group are fetched by
	 * {@link #getGroupMembers(int, List, AsyncCallback)}.
	 * 
	 * @param callback
	 *            Optional callback for a DataProxy. Will be called when the list of sensors is
	 *            complete.
	 */
	private void getGroups(final AsyncCallback<List<ExtUser>> callback) {
		LOG.fine("Get groups");

		// notify view
		view.setBusy(true);

		// clear registry
		Registry.<List<ExtGroup>> get(Constants.REG_GROUPS).clear();

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				onGroupsFailure(-1, exception, callback);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onGroupsSuccess(response.getText(), callback);
				} else {
					onGroupsFailure(statusCode, new Throwable(response.getStatusText()), callback);
				}
			}
		};

		CommonSenseApi.getGroups(reqCallback, null, null);
	}

	@Override
	public void loadData(AsyncCallback<List<ExtUser>> callback, Object loadConfig) {

		if (null == loadConfig) {
			getGroups(callback);

		} else if (loadConfig instanceof ExtGroup) {
			ExtGroup group = (ExtGroup) loadConfig;
			getGroupUsers(group, callback);

		} else {
			callback.onSuccess(new ArrayList<ExtUser>());
		}
	}

	private void onGroupUsersFailure(int code, Throwable error, ExtGroup group,
			AsyncCallback<List<ExtUser>> callback) {
		LOG.warning("Failed to get group " + group.getId() + " users! Code: " + code + " "
				+ error.getMessage());

		view.setBusy(false);

		if (null != callback) {
			callback.onFailure(null);
		}
	}

	private void onGroupUsersForbidden(ExtGroup group, AsyncCallback<List<ExtUser>> callback) {
		// user is not allowed to view the group members
		view.setBusy(false);

		if (null != callback) {
			callback.onSuccess(new ArrayList<ExtUser>());
		}
	}

	/**
	 * Handles the response from CommonSense to the request for group members. Parses the JSON array
	 * with user information, and calls back to {@link #getGroupMembers(int, List, AsyncCallback)}
	 * to get the members for the next group.
	 * 
	 * @param response
	 *            Response from CommonSense (JSON String).
	 * @param details
	 *            List of group details that were received earlier. The new details will be added to
	 *            this list.
	 * @param count
	 *            Count for the number of groups that already have members.
	 * @param callback
	 *            Optional callback for a DataProxy. Will be called when the list of groups is
	 *            complete.
	 */
	private void onGroupMembersSuccess(String response, ExtGroup group,
			AsyncCallback<List<ExtUser>> callback) {

		// parse list of users from the response
		List<User> users = new ArrayList<User>();
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			GetGroupUsersResponse jso = JsonUtils.unsafeEval(response);
			users = jso.getUsers();
		}

		// add users to the group
		List<ExtUser> extUsers = new ArrayList<ExtUser>(users.size());
		for (User user : users) {
			extUsers.add(new ExtUser(user));
		}

		view.setBusy(false);

		if (null != callback) {
			callback.onSuccess(new ArrayList<ExtUser>(extUsers));
		}
	}

	private void onGroupsFailure(int code, Throwable error, AsyncCallback<List<ExtUser>> callback) {
		LOG.warning("Failed to get groups! Code: " + code + " " + error);

		view.setBusy(false);

		if (null != callback) {
			callback.onFailure(null);
		}
	}

	/**
	 * Handles the response from CommonSense to the request for groups. Parses the JSON array with
	 * group details, and calls through to {@link #getGroupMembers(int, List, AsyncCallback)}.
	 * 
	 * @param response
	 *            Response from CommonSense (JSON String).
	 * @param callback
	 *            Optional callback for a DataProxy. Will be called when the list of groups is
	 *            complete.
	 */
	private void onGroupsSuccess(String response, AsyncCallback<List<ExtUser>> callback) {

		// parse list of groups from the response
		JsArray<Group> groups = null;
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			GetGroupsResponse jso = JsonUtils.unsafeEval(response);
			groups = jso.getRawGroups();
		}

		if (null != groups) {
			// convert to Ext
			List<ExtGroup> extGroups = new ArrayList<ExtGroup>(groups.length());
			for (int i = 0; i < groups.length(); i++) {
				extGroups.add(new ExtGroup(groups.get(i)));
			}

			Registry.<List<ExtGroup>> get(Constants.REG_GROUPS).addAll(extGroups);

			view.setBusy(false);

			callback.onSuccess(new ArrayList<ExtUser>(extGroups));

		} else {
			onGroupsFailure(-1, new Throwable("No groups"), callback);
		}
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		LOG.info("Starting 'groupmanagement' activity");

		view = clientFactory.getGroupManagementView();
		view.setPresenter(this);

		LayoutContainer parent = clientFactory.getMainView().getActivityPanelGxt();
		parent.removeAll();
		parent.add(view.asWidget());
		parent.layout();
	}
}
