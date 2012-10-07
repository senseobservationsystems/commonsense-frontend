package nl.sense_os.commonsense.main.client.visualization;

import nl.sense_os.commonsense.common.client.model.Timeseries;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.IsWidget;

public interface VisualizationView extends IsWidget {

	public interface Presenter {

		void refreshData();
	}

	void setPresenter(Presenter presenter);

	void visualize(JsArray<Timeseries> data);
}
