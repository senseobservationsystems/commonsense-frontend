package nl.sense_os.commonsense.shared.client.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class UnsupportedBrowserComponent extends Composite {

	private static UnsupportedBrowserComponentUiBinder uiBinder = GWT
			.create(UnsupportedBrowserComponentUiBinder.class);

	interface UnsupportedBrowserComponentUiBinder extends
			UiBinder<Widget, UnsupportedBrowserComponent> {
	}

	public UnsupportedBrowserComponent() {
		initWidget(uiBinder.createAndBindUi(this));
	}
}
