package nl.sense_os.commonsense.login.client.application;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class LoginApplicationViewImpl extends Composite implements LoginApplicationView {

	private static MainViewUiBinder uiBinder = GWT.create(MainViewUiBinder.class);

	interface MainViewUiBinder extends UiBinder<Widget, LoginApplicationViewImpl> {
	}

	@UiField
	SimplePanel appWidget;

	public LoginApplicationViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public SimplePanel getActivityPanel() {
		return appWidget;
	}
}
