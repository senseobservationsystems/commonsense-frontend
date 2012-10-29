package nl.sense_os.commonsense.main.client.application.component;

import nl.sense_os.commonsense.shared.client.resource.CSResources;
import nl.sense_os.commonsense.shared.client.util.Constants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class MainNavigationBar extends Composite {

	private static MainNavigationBarUiBinder uiBinder = GWT.create(MainNavigationBarUiBinder.class);

	interface MainNavigationBarUiBinder extends UiBinder<Widget, MainNavigationBar> {
	}

	@UiField
	Label userLabel;
	@UiField
	Image logo;

	public MainNavigationBar() {
		initWidget(uiBinder.createAndBindUi(this));

		if (Constants.RC_MODE) {
			logo.setResource(CSResources.INSTANCE.logoTest());
		} else if (Constants.DEV_MODE) {
			logo.setResource(CSResources.INSTANCE.logoDev());
		}
	}

	public void setUserLabel(String label) {
		userLabel.setText(label);
	}
}
