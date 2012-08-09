package nl.sense_os.commonsense.login.client.main.component;

import nl.sense_os.commonsense.common.client.resource.CSResources;
import nl.sense_os.commonsense.common.client.util.Constants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class LoginNavigationBar extends Composite {

	private static NavigationBarUiBinder uiBinder = GWT.create(NavigationBarUiBinder.class);

	interface NavigationBarUiBinder extends UiBinder<Widget, LoginNavigationBar> {
	}

	@UiField
	Image logo;

	public LoginNavigationBar() {
		initWidget(uiBinder.createAndBindUi(this));

		if (Constants.RC_MODE) {
			logo.setResource(CSResources.INSTANCE.logoTest());
		} else if (Constants.DEV_MODE) {
			logo.setResource(CSResources.INSTANCE.logoDev());
		}
	}
}
