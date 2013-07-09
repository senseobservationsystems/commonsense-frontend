package nl.sense_os.commonsense.common.client.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class AlertDialogContent extends Composite implements Focusable {

	interface Binder extends UiBinder<Widget, AlertDialogContent> {
	}

	public interface Presenter {
		void dismissAlert();
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	private Presenter presenter;
	private int tabIndex;

	@UiField
	Label message;
	@UiField
	Button okButton;

	public AlertDialogContent() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public int getTabIndex() {
		return tabIndex;
	}

	@UiHandler("okButton")
	void onOkClick(ClickEvent event) {
		if (null != presenter) {
			presenter.dismissAlert();
		}
	}

	@Override
	public void setAccessKey(char key) {
		// do nothing
	}

	@Override
	public void setFocus(boolean focused) {
		okButton.setFocus(focused);
	}

	public void setMessage(String text) {
		this.message.setText(text);
	}

	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setTabIndex(int index) {
		tabIndex = index;
	}
}
