package nl.sense_os.commonsense.main.client.application.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class MainNavigationBar extends Composite {

	private static MainNavigationBarUiBinder uiBinder = GWT.create(MainNavigationBarUiBinder.class);

	interface MainNavigationBarUiBinder extends UiBinder<Widget, MainNavigationBar> {
	}

	public MainNavigationBar() {
		initWidget(uiBinder.createAndBindUi(this));
	}
}
