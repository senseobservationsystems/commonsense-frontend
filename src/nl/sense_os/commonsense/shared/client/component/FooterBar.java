package nl.sense_os.commonsense.shared.client.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class FooterBar extends Composite {

	private static FooterBarUiBinder uiBinder = GWT.create(FooterBarUiBinder.class);

	interface FooterBarUiBinder extends UiBinder<Widget, FooterBar> {
	}

	public FooterBar() {
		initWidget(uiBinder.createAndBindUi(this));
	}
}
