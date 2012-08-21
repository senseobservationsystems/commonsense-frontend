package nl.sense_os.commonsense.main.client.groupmanagement;

import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
import nl.sense_os.commonsense.main.client.groupmanagement.GroupManagementView.Presenter;

import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class GroupManagementActivity extends AbstractActivity implements Presenter {

	private static final Logger LOG = Logger.getLogger(GroupManagementActivity.class.getName());
	private MainClientFactory clientFactory;
	private GroupManagementView view;

	public GroupManagementActivity(GroupManagementPlace place, MainClientFactory clientFactory) {
		this.clientFactory = clientFactory;
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

	@Override
	public void loadData(AsyncCallback<ListLoadResult<ExtSensor>> callback, boolean force) {
		// TODO Auto-generated method stub

	}

}
