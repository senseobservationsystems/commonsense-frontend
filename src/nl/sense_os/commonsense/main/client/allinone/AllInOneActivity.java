package nl.sense_os.commonsense.main.client.allinone;

import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.MainClientFactory;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class AllInOneActivity extends AbstractActivity implements AllInOneView.Presenter {

	private static final Logger LOG = Logger.getLogger(AllInOneActivity.class.getName());

	/**
	 * Used to obtain views, eventBus, placeController. Alternatively, could be injected via GIN.
	 */
	private final MainClientFactory clientFactory;

	public AllInOneActivity(AllInOnePlace place, MainClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		LOG.info("Start 'sensors' activity");

		AllInOneView view = clientFactory.getSensorsView();

		LayoutContainer parent = clientFactory.getMainView().getActivityPanelGxt();
		parent.removeAll();
		parent.setLayout(new FitLayout());
		parent.add(view.asWidget());

		view.setPresenter(this);
		parent.layout();
	}
}
