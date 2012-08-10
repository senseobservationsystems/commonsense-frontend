package nl.sense_os.commonsense.main.client.allinone;

import com.google.gwt.user.client.ui.IsWidget;

public interface AllInOneView extends IsWidget {

	public interface Presenter {

	}

	void setPresenter(Presenter presenter);
}
