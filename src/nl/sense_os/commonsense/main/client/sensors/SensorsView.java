package nl.sense_os.commonsense.main.client.sensors;

import com.google.gwt.user.client.ui.IsWidget;

public interface SensorsView extends IsWidget {

	public interface Presenter {

	}

	void setPresenter(Presenter presenter);

    void foo();
}
