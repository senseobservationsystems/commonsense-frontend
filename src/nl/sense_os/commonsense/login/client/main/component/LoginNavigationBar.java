package nl.sense_os.commonsense.login.client.main.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class LoginNavigationBar extends Composite {

	private static NavigationBarUiBinder uiBinder = GWT.create(NavigationBarUiBinder.class);

	interface NavigationBarUiBinder extends UiBinder<Widget, LoginNavigationBar> {
	}

	public LoginNavigationBar() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}
