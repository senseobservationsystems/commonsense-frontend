package nl.sense_os.commonsense.common.client.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class AlertDialogContent extends Composite {

	interface Binder extends UiBinder<Widget, AlertDialogContent> {
	}

	public interface Presenter {
		void dismissAlert();
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	Label message;

	private Presenter presenter;

	public AlertDialogContent() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiHandler("okButton")
	void onOkClick(ClickEvent event) {
		if (null != presenter) {
			presenter.dismissAlert();
		}
	}

	public void setMessage(String text) {
		this.message.setText(text);
	}

	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
}
