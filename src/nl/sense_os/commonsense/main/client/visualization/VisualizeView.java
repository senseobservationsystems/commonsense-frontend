package nl.sense_os.commonsense.main.client.visualization;

import nl.sense_os.commonsense.main.client.event.NewSensorDataEvent;

import com.google.gwt.user.client.ui.IsWidget;

public interface VisualizeView extends IsWidget, NewSensorDataEvent.Handler {

	public interface Presenter {

		void refreshData();
	}
}
