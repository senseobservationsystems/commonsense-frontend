package nl.sense_os.commonsense.main.client.environment;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.communication.CommonSenseApi;
import nl.sense_os.commonsense.common.client.communication.httpresponse.GetEnvironmentsResponse;
import nl.sense_os.commonsense.common.client.model.Environment;
import nl.sense_os.commonsense.common.client.util.Constants;
import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.env.create.EnvCreateEvents;
import nl.sense_os.commonsense.main.client.env.list.EnvEvents;
import nl.sense_os.commonsense.main.client.env.view.EnvViewEvents;
import nl.sense_os.commonsense.main.client.ext.model.ExtEnvironment;
import nl.sense_os.commonsense.main.client.ext.model.ExtGroup;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class EnvironmentManagementActivity extends AbstractActivity implements
		EnvironmentManagementView.Presenter {

	private static final Logger LOG = Logger.getLogger(EnvironmentManagementActivity.class
			.getName());
	private MainClientFactory clientFactory;
	private EnvironmentManagementView view;

	public EnvironmentManagementActivity(EnvironmentManagementPlace place,
			MainClientFactory clientFactory) {
		this.clientFactory = clientFactory;
		Registry.register(Constants.REG_ENVIRONMENT_LIST, new ArrayList<ExtGroup>());
	}

	private void delete(final ExtEnvironment environment) {

		// prepare request callback
		RequestCallback callback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				onDeleteFailure(-1, exception);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onDeleteSuccess(environment);
				} else {
					onDeleteFailure(statusCode, new Throwable(response.getStatusText()));
				}
			}
		};

		CommonSenseApi.deleteEnvironment(callback, environment.getId());
	}

	@Override
	public void loadData(final AsyncCallback<ListLoadResult<ExtEnvironment>> callback) {

		view.setBusy(true);

		Registry.<List<ExtEnvironment>> get(Constants.REG_ENVIRONMENT_LIST).clear();

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				onListFailure(-1, exception, callback);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onListSuccess(response.getText(), callback);
				} else if (Response.SC_NO_CONTENT == statusCode) {
					onListSuccess(null, callback);
				} else {
					onListFailure(statusCode, new Throwable(response.getStatusText()), callback);
				}
			}
		};

		CommonSenseApi.getEnvironments(reqCallback, null, null);
	}

	@Override
	public void onCreateClick() {
		Dispatcher.forwardEvent(EnvCreateEvents.ShowCreator);
	}

	private void onDeleteFailure(int code, Throwable error) {
		LOG.warning("Failed to delete environment! Code: " + code + " " + error.getMessage());

		// TODO inform the user of the error
	}

	private void onDeleteSuccess(ExtEnvironment environment) {

		// update sensor library
		List<ExtSensor> library = Registry
				.<List<ExtSensor>> get(nl.sense_os.commonsense.common.client.util.Constants.REG_SENSOR_LIST);
		for (ExtSensor sensor : library) {
			if (sensor.getEnvironment() != null && sensor.getEnvironment().equals(environment)) {
				sensor.setEnvironment(null);
			}
		}

		// update global environment list
		Registry.<List<ExtEnvironment>> get(
				nl.sense_os.commonsense.common.client.util.Constants.REG_ENVIRONMENT_LIST).remove(
				environment);

		Dispatcher.forwardEvent(EnvEvents.DeleteSuccess);
	}

	private void onListFailure(int code, Throwable error,
			AsyncCallback<ListLoadResult<ExtEnvironment>> callback) {
		LOG.warning("Failed to get enviroments! Code: " + code + " " + error.getMessage());

		view.setBusy(false);

		if (null != callback) {
			callback.onFailure(null);
		}
	}

	private void onListSuccess(String response,
			AsyncCallback<ListLoadResult<ExtEnvironment>> callback) {

		// parse the list of environments from the response
		List<Environment> environments = new ArrayList<Environment>();
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			GetEnvironmentsResponse jso = JsonUtils.unsafeEval(response);
			environments = jso.getEnvironments();
		}

		List<ExtEnvironment> extEnvironments = new ArrayList<ExtEnvironment>();
		for (Environment e : environments) {
			extEnvironments.add(new ExtEnvironment(e));
		}

		Registry.<List<ExtEnvironment>> get(Constants.REG_ENVIRONMENT_LIST).addAll(extEnvironments);

		view.setBusy(false);

		if (null != callback) {
			callback.onSuccess(new BaseListLoadResult<ExtEnvironment>(extEnvironments));
		}
	}

	@Override
	public void onDeleteClick(final ExtEnvironment environment) {
		MessageBox.confirm(null, "Are you sure you want to remove this environment?",
				new Listener<MessageBoxEvent>() {

					@Override
					public void handleEvent(MessageBoxEvent be) {
						Button clicked = be.getButtonClicked();
						if ("yes".equalsIgnoreCase(clicked.getText())) {
							delete(environment);
						}
					}
				});
	}

	@Override
	public void onEditClick(ExtEnvironment environment) {
		// TODO

	}

	@Override
	public void onViewClick(ExtEnvironment environment) {
		AppEvent viewEvent = new AppEvent(EnvViewEvents.Show);
		viewEvent.setData("environment", environment);
		Dispatcher.forwardEvent(viewEvent);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		LOG.info("Starting 'environmentmanagement' activity");

		view = clientFactory.getEnvironmentManagementView();
		view.setPresenter(this);

		LayoutContainer parent = clientFactory.getMainView().getActivityPanelGxt();
		parent.removeAll();
		parent.add(view.asWidget());
		parent.layout();
	}
}
