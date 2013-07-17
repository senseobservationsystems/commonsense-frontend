package nl.sense_os.commonsense.main.client.groups.list;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.communication.SessionManager;
import nl.sense_os.commonsense.common.client.communication.httpresponse.GetGroupUsersResponse;
import nl.sense_os.commonsense.common.client.communication.httpresponse.GetGroupsResponse;
import nl.sense_os.commonsense.common.client.model.Group;
import nl.sense_os.commonsense.common.client.model.User;
import nl.sense_os.commonsense.lib.client.communication.CommonSenseClient;
import nl.sense_os.commonsense.lib.client.communication.CommonSenseClient.Urls;
import nl.sense_os.commonsense.main.client.ext.model.ExtGroup;
import nl.sense_os.commonsense.main.client.ext.model.ExtUser;
import nl.sense_os.commonsense.main.client.groups.create.GroupCreateEvents;
import nl.sense_os.commonsense.main.client.groups.invite.GroupInviteEvents;
import nl.sense_os.commonsense.main.client.groups.join.GroupJoinEvents;
import nl.sense_os.commonsense.main.client.groups.leave.GroupLeaveEvents;
import nl.sense_os.commonsense.main.client.viz.tabs.VizEvents;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupController extends Controller {

	private static final Logger LOG = Logger.getLogger(GroupController.class.getName());
	private View tree;

	public GroupController() {

		registerEventTypes(GroupEvents.ShowGrid);

		// events to update the list of groups
		registerEventTypes(GroupEvents.LoadRequest, GroupEvents.ListUpdated, GroupEvents.Working);

		registerEventTypes(VizEvents.Show);
		registerEventTypes(GroupInviteEvents.InviteComplete);
		registerEventTypes(GroupCreateEvents.CreateComplete);
		registerEventTypes(GroupLeaveEvents.LeaveComplete);
		registerEventTypes(GroupJoinEvents.JoinSuccess);
	}

	/**
	 * Gets the members of a group (UserModels) from CommonSense, using an Ajax request. The
	 * response is handled by {@link #onGroupMembersSuccess(String, List, int, AsyncCallback)} or
	 * {@link #onGroupMembersFailure(List, int, AsyncCallback)}. If the members for all groups are
	 * complete, the requests are finished and {@link GroupEvents#ListUpdated} will be dispatched.
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
	private void getGroupMembers(final ExtGroup group, final AsyncCallback<List<ExtUser>> callback) {

		forwardToView(this.tree, new AppEvent(GroupEvents.Working));

		// prepare request properties
		final UrlBuilder urlBuilder = new UrlBuilder().setProtocol(CommonSenseClient.Urls.PROTOCOL)
				.setHost(CommonSenseClient.Urls.HOST);
		urlBuilder.setPath(Urls.PATH_GROUPS + "/" + group.getId() + "/users.json");
		final String url = urlBuilder.buildString();
		final String sessionId = SessionManager.getSessionId();

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("GET group users onError callback: " + exception.getMessage());
				onGroupMembersFailure(-1, group, callback);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("GET group users response received: " + response.getStatusText());
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onGroupMembersSuccess(response.getText(), group, callback);
				} else if (Response.SC_FORBIDDEN == statusCode) {
					onGroupMembersForbidden(group, callback);
				} else {
					LOG.warning("GET group users returned incorrect status: " + statusCode);
					onGroupMembersFailure(statusCode, group, callback);
				}
			}
		};

		// send request
		try {
			RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
			builder.setHeader("X-SESSION_ID", sessionId);
			builder.sendRequest(null, reqCallback);
		} catch (Exception e) {
			LOG.warning("GET group users request threw exception: " + e.getMessage());
			reqCallback.onError(null, e);
		}
	}

	private void onGroupMembersForbidden(ExtGroup group, AsyncCallback<List<ExtUser>> callback) {
		// user is not allowed to view the group members
		Dispatcher.forwardEvent(GroupEvents.ListUpdated);

		if (null != callback) {
			callback.onSuccess(new ArrayList<ExtUser>());
		}
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

		forwardToView(this.tree, new AppEvent(GroupEvents.Working));
		Registry.<List<ExtGroup>> get(
				nl.sense_os.commonsense.common.client.util.Constants.REG_GROUPS).clear();

		// prepare request properties
		final UrlBuilder urlBuilder = new UrlBuilder().setProtocol(CommonSenseClient.Urls.PROTOCOL)
				.setHost(CommonSenseClient.Urls.HOST);
		urlBuilder.setPath(Urls.PATH_GROUPS + ".json");
		final String url = urlBuilder.buildString();
		final String sessionId = SessionManager.getSessionId();

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("GET groups onError callback: " + exception.getMessage());
				onGroupsFailure(callback);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("GET groups response received: " + response.getStatusText());
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onGroupsSuccess(response.getText(), callback);
				} else {
					LOG.warning("GET groups returned incorrect status: " + statusCode);
					onGroupsFailure(callback);
				}
			}
		};

		// send request
		try {
			RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
			builder.setHeader("X-SESSION_ID", sessionId);
			builder.sendRequest(null, reqCallback);
		} catch (Exception e) {
			LOG.warning("GET groups request threw exception: " + e.getMessage());
			reqCallback.onError(null, e);
		}
	}

	@Override
	public void handleEvent(AppEvent event) {
		final EventType type = event.getType();

		/*
		 * Load list of groups
		 */
		if (type.equals(GroupEvents.LoadRequest)) {
			// LOG.fine( "LoadRequest");
			final Object loadConfig = event.getData("loadConfig");
			final AsyncCallback<List<ExtUser>> callback = event
					.<AsyncCallback<List<ExtUser>>> getData("callback");
			onLoadRequest(loadConfig, callback);

		} else

		/*
		 * Pass through to view
		 */
		{
			forwardToView(this.tree, event);
		}
	}

	@Override
	protected void initialize() {
		super.initialize();
		this.tree = new GroupGrid(this);
		Registry.register(nl.sense_os.commonsense.common.client.util.Constants.REG_GROUPS,
				new ArrayList<ExtGroup>());
	}

	private void onGroupMembersFailure(int code, ExtGroup group,
			AsyncCallback<List<ExtUser>> callback) {

		Dispatcher.forwardEvent(GroupEvents.ListUpdated);

		if (null != callback) {
			callback.onFailure(null);
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
		GetGroupUsersResponse jso = GetGroupUsersResponse.create(response).cast();
		if (null != jso) {
			users = jso.getUsers();
		}

		// add users to the group
		List<ExtUser> extUsers = new ArrayList<ExtUser>(users.size());
		for (User user : users) {
			extUsers.add(new ExtUser(user));
			group.add(new ExtUser(user));
		}

		Dispatcher.forwardEvent(GroupEvents.ListUpdated);

		if (null != callback) {
			callback.onSuccess(new ArrayList<ExtUser>(extUsers));
		}
	}

	private void onGroupsFailure(AsyncCallback<List<ExtUser>> callback) {
		Dispatcher.forwardEvent(GroupEvents.ListUpdated);

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
		com.google.gwt.core.client.JsArray<Group> groups = null;
		GetGroupsResponse jso = GetGroupsResponse.create(response).cast();
		if (null != response) {
			groups = jso.getRawGroups();
		}

		if (null != groups) {
			// convert to Ext
			List<ExtGroup> extGroups = new ArrayList<ExtGroup>(groups.length());
			for (int i = 0; i < groups.length(); i++) {
				extGroups.add(new ExtGroup(groups.get(i)));
			}

			Registry.<List<ExtGroup>> get(
					nl.sense_os.commonsense.common.client.util.Constants.REG_GROUPS).addAll(
					extGroups);
			Dispatcher.forwardEvent(GroupEvents.ListUpdated);

			callback.onSuccess(new ArrayList<ExtUser>(extGroups));
		}
	}

	private void onLoadRequest(Object loadConfig, AsyncCallback<List<ExtUser>> callback) {

		if (null == loadConfig) {
			getGroups(callback);

		} else if (loadConfig instanceof ExtGroup) {
			ExtGroup group = (ExtGroup) loadConfig;
			getGroupMembers(group, callback);

		} else {
			callback.onSuccess(new ArrayList<ExtUser>());
		}
	}
}
