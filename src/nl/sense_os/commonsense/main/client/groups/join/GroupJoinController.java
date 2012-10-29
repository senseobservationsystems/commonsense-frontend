package nl.sense_os.commonsense.main.client.groups.join;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.gxt.model.GxtGroup;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.model.GxtUser;
import nl.sense_os.commonsense.shared.client.communication.SessionManager;
import nl.sense_os.commonsense.shared.client.communication.httpresponse.GetGroupDetailsResponse;
import nl.sense_os.commonsense.shared.client.communication.httpresponse.GetGroupsResponse;
import nl.sense_os.commonsense.shared.client.constant.Urls;
import nl.sense_os.commonsense.shared.client.model.Group;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class GroupJoinController extends Controller {

	private static final Logger LOG = Logger.getLogger(GroupJoinController.class.getName());
	private static final int PER_PAGE = 1000;

	public GroupJoinController() {
		registerEventTypes(GroupJoinEvents.Show, GroupJoinEvents.JoinRequest,
				GroupJoinEvents.AllGroupsRequest, GroupJoinEvents.GroupDetailsRequest);
	}

	private void getAllGroups(final List<GxtGroup> groups, final int page, final View source) {

		// prepare request properties
		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
		urlBuilder.setPath(Urls.PATH_GROUPS + "/all");
		urlBuilder.setParameter("per_page", "" + PER_PAGE);
		final String url = urlBuilder.buildString();
		final String sessionId = SessionManager.getSessionId();

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("GET all groups onError callback: " + exception.getMessage());
				onAllGroupsFailure(source);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("GET all groups response received: " + response.getStatusText());
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onAllGroupsSuccess(response.getText(), groups, page, source);
				} else {
					LOG.warning("GET all groups returned incorrect status: " + statusCode);
					onAllGroupsFailure(source);
				}
			}
		};

		// send request
		try {
			RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
			builder.setHeader("X-SESSION_ID", sessionId);
			builder.sendRequest(null, reqCallback);
		} catch (Exception e) {
			LOG.warning("GET all groups request threw exception: " + e.getMessage());
			reqCallback.onError(null, e);
		}
	}

	@Override
	public void handleEvent(AppEvent event) {
		EventType type = event.getType();

		if (type.equals(GroupJoinEvents.Show)) {
			LOG.finest("Create new group join view");
			View view = new GroupJoinView(this);
			forwardToView(view, event);

		} else if (type.equals(GroupJoinEvents.JoinRequest)) {
			LOG.finest("JoinRequest");
			GxtGroup group = event.getData("group");
			List<GxtSensor> sensors = event.getData("sensors");
			View source = (View) event.getSource();
			join(group, sensors, source);

		} else if (type.equals(GroupJoinEvents.AllGroupsRequest)) {
			LOG.finest("AllGroupsRequest");
			View source = (View) event.getSource();
			onAllGroupsRequest(source);

		} else if (type.equals(GroupJoinEvents.GroupDetailsRequest)) {
			LOG.finest("GroupDetailsRequest");
			GxtGroup group = event.getData("group");
			View source = (View) event.getSource();
			onGroupsDetailsRequest(group, source);

		} else {
			LOG.warning("Unexpected event: " + event);
		}
	}

	private void onGroupsDetailsRequest(GxtGroup group, final View source) {

		// prepare request properties
		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
		urlBuilder.setPath(Urls.PATH_GROUPS + "/" + group.getId() + ".json");
		final String url = urlBuilder.buildString();
		final String sessionId = SessionManager.getSessionId();

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("GET group details onError callback: " + exception.getMessage());
				onGroupDetailsFailure(source);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("GET group details response received: " + response.getStatusText());
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onGroupDetailsSuccess(response.getText(), source);
				} else {
					LOG.warning("GET group details returned incorrect status: " + statusCode);
					onGroupDetailsFailure(source);
				}
			}

		};

		// send request
		try {
			RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
			builder.setHeader("X-SESSION_ID", sessionId);
			builder.sendRequest(null, reqCallback);
		} catch (Exception e) {
			LOG.warning("GET group details request threw exception: " + e.getMessage());
			reqCallback.onError(null, e);
		}
	}

	private void onGroupDetailsSuccess(String response, View source) {
		Group group = null;
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			GetGroupDetailsResponse jso = JsonUtils.unsafeEval(response);
			group = jso.getGroup();
		}

		// convert to Ext
		GxtGroup gxtGroup = new GxtGroup(group);

		AppEvent event = new AppEvent(GroupJoinEvents.GroupDetailsSuccess);
		event.setData("group", gxtGroup);
		forwardToView(source, event);
	}

	private void onGroupDetailsFailure(View source) {
		forwardToView(source, new AppEvent(GroupJoinEvents.GroupDetailsFailure));
	}

	private void join(GxtGroup group, List<GxtSensor> sensors, final View source) {

		// prepare request properties
		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
		urlBuilder.setPath(Urls.PATH_GROUPS + "/" + group.getId() + "/users.json");
		final String url = urlBuilder.buildString();
		final String sessionId = SessionManager.getSessionId();

		GxtUser user = Registry
				.<GxtUser> get(nl.sense_os.commonsense.shared.client.util.Constants.REG_USER);

		JSONObject userJson = new JSONObject();
        userJson.put("id", new JSONString(user.getId()));
		userJson.put("username", new JSONString(user.getUsername()));
		JSONArray sensorsJson = new JSONArray();
		for (int i = 0; i < sensors.size(); i++) {
			GxtSensor sensor = sensors.get(i);
            sensorsJson.set(i, new JSONString(sensor.getId()));
		}
		JSONObject bodyJson = new JSONObject();
		bodyJson.put("user", userJson);
		bodyJson.put("sensors", sensorsJson);
		String body = bodyJson.toString();

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("POST group user onError callback: " + exception.getMessage());
				onJoinFailure(source);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("POST group user response received: " + response.getStatusText());
				int statusCode = response.getStatusCode();
				if (Response.SC_CREATED == statusCode) {
					onJoinSuccess(source);
				} else {
					LOG.warning("POST group user returned incorrect status: " + statusCode);
					onJoinFailure(source);
				}
			}
		};

		// send request
		try {
			RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url);
			builder.setHeader("X-SESSION_ID", sessionId);
			builder.sendRequest(body, reqCallback);
		} catch (Exception e) {
			LOG.warning("POST group user request threw exception: " + e.getMessage());
			reqCallback.onError(null, e);
		}
	}

	private void onJoinFailure(View source) {
		forwardToView(source, new AppEvent(GroupJoinEvents.JoinFailure));
	}

	private void onJoinSuccess(View source) {
		forwardToView(source, new AppEvent(GroupJoinEvents.JoinSuccess));
		Dispatcher.forwardEvent(GroupJoinEvents.JoinSuccess);
	}

	private void onAllGroupsFailure(View source) {
		forwardToView(source, new AppEvent(GroupJoinEvents.AllGroupsFailure));
	}

	private void onAllGroupsRequest(View source) {
		List<GxtGroup> groups = new ArrayList<GxtGroup>();
		int page = 0;
		getAllGroups(groups, page, source);
	}

	private void onAllGroupsSuccess(String response, List<GxtGroup> groups, int page, View source) {

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
			getAllGroups(groups, page, source);
		} else {
			AppEvent event = new AppEvent(GroupJoinEvents.AllGroupsSuccess);
			event.setData("groups", groups);
			forwardToView(source, event);
		}
	}
}
