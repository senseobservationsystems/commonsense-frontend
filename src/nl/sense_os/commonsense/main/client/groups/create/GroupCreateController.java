package nl.sense_os.commonsense.main.client.groups.create;

import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.communication.SessionManager;
import nl.sense_os.commonsense.common.client.constant.Urls;
import nl.sense_os.commonsense.main.client.ext.model.ExtGroup;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;

public class GroupCreateController extends Controller {

	private static final Logger LOG = Logger.getLogger(GroupCreateController.class.getName());

	public GroupCreateController() {
		// LOG.setLevel(Level.ALL);
		registerEventTypes(GroupCreateEvents.ShowCreator, GroupCreateEvents.CreateRequested);
	}

	private void createGroup(ExtGroup group, final GroupCreateView source) {

		// prepare request properties
		final Method method = RequestBuilder.POST;
		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
		urlBuilder.setPath(Urls.PATH_GROUPS + ".json");
		final String url = urlBuilder.buildString();
		final String sessionId = SessionManager.getSessionId();

		String body = "{\"group\":" + group.toJson() + "}";

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("POST group onError callback: " + exception.getMessage());
				onCreateFailure(source);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("POST group response received: " + response.getStatusText());
				int statusCode = response.getStatusCode();
				if (Response.SC_CREATED == statusCode) {
					onCreateSuccess(source);
				} else {
					LOG.warning("POST group returned incorrect status: " + statusCode);
					onCreateFailure(source);
				}
			}
		};

		// send request
		try {
			RequestBuilder builder = new RequestBuilder(method, url);
			builder.setHeader("X-SESSION_ID", sessionId);
			builder.setHeader("Content-Type", Urls.HEADER_JSON_TYPE);
			builder.sendRequest(body, reqCallback);
		} catch (Exception e) {
			LOG.warning("POST group request threw exception: " + e.getMessage());
			reqCallback.onError(null, e);
		}
	}

	@Override
	public void handleEvent(AppEvent event) {
		final EventType type = event.getType();

		if (type.equals(GroupCreateEvents.CreateRequested)) {
			LOG.finest("CreateRequested");
			final ExtGroup group = event.getData("group");
			GroupCreateView source = (GroupCreateView) event.getSource();
			createGroup(group, source);

		} else if (type.equals(GroupCreateEvents.ShowCreator)) {
			LOG.finest("NewCreator");
			// create new view
			GroupCreateView view = new GroupCreateView(this);
			forwardToView(view, event);

		} else {
			LOG.warning("Unxpected event: " + event);
		}
	}

	private void onCreateFailure(GroupCreateView source) {
		AppEvent event = new AppEvent(GroupCreateEvents.CreateFailed);
		forwardToView(source, event);
	}

	private void onCreateSuccess(GroupCreateView source) {
		AppEvent event = new AppEvent(GroupCreateEvents.CreateComplete);
		forwardToView(source, event);
		Dispatcher.forwardEvent(event);
	}
}
