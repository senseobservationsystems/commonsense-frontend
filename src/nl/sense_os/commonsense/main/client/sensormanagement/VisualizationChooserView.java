package nl.sense_os.commonsense.main.client.sensormanagement;

import java.util.List;

import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;

import com.google.gwt.user.client.ui.IsWidget;

public interface VisualizationChooserView extends IsWidget {

	public interface Presenter {
		void onVisualizationChoice(List<GxtSensor> sensors, int type, long start, long end,
				boolean subsample);
	}

	void hideWindow();

	void setPresenter(Presenter presenter);

	void showWindow(List<GxtSensor> sensors);
}
