package nl.sense_os.commonsense.main.client.sensors;

import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.MainClientFactory;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class SensorsActivity extends AbstractActivity implements SensorsView.Presenter {

	private static final Logger LOG = Logger.getLogger(SensorsActivity.class.getName());

	/**
	 * Used to obtain views, eventBus, placeController. Alternatively, could be injected via GIN.
	 */
	private final MainClientFactory clientFactory;

	public SensorsActivity(SensorsPlace place, MainClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		LOG.info("Start 'sensors' activity");

		SensorsView view = clientFactory.getSensorsView();

		LayoutContainer parent = clientFactory.getMainView().getActivityPanelGxt();
		parent.removeAll();
		parent.setLayout(new FitLayout());
		parent.add(view.asWidget());

		view.setPresenter(this);
		parent.layout();

        view.asWidget().setSize("100%", "100%");
        view.foo();
	}
}
