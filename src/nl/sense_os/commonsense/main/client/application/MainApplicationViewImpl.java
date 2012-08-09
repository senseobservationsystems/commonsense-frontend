package nl.sense_os.commonsense.main.client.application;

import nl.sense_os.commonsense.common.client.event.CurrentUserChangedEvent;
import nl.sense_os.commonsense.main.client.application.component.MainNavigationBar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class MainApplicationViewImpl extends Composite implements MainApplicationView {

	private static MainViewUiBinder uiBinder = GWT.create(MainViewUiBinder.class);

	interface MainViewUiBinder extends UiBinder<Widget, MainApplicationViewImpl> {
	}

	@UiField
	SimplePanel appWidget;
	@UiField
	MainNavigationBar navBar;

	public MainApplicationViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public SimplePanel getActivityPanel() {
		return appWidget;
	}

	@Override
	public void onCurrentUserChanged(CurrentUserChangedEvent event) {
		navBar.setUserLabel(event.getUser().getUsername());
	}
}
