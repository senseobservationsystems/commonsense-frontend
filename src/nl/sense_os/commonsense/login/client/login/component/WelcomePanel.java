package nl.sense_os.commonsense.login.client.login.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class WelcomePanel extends Composite {

	private static WelcomePanelUiBinder uiBinder = GWT.create(WelcomePanelUiBinder.class);

	interface WelcomePanelUiBinder extends UiBinder<Widget, WelcomePanel> {
	}

	public WelcomePanel() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}
