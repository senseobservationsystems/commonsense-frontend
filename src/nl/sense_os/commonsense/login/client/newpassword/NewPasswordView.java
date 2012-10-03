package nl.sense_os.commonsense.login.client.newpassword;

import nl.sense_os.commonsense.common.client.component.Resettable;

import com.google.gwt.user.client.ui.IsWidget;

public interface NewPasswordView extends IsWidget, Resettable {

	public interface Presenter {

		void cancel();

		void submit(String password);
	}

	void setPresenter(Presenter presenter);
}
