package nl.sense_os.commonsense.main.client.sensormanagement;

import java.util.List;

import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;

import com.google.gwt.user.client.ui.IsWidget;

public interface VisualizationChooserView extends IsWidget {

	public void hideWindow();

	public void showWindow(List<GxtSensor> sensors);
}
