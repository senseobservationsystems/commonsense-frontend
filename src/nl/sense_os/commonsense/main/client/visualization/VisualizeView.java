package nl.sense_os.commonsense.main.client.visualization;

import com.google.gwt.user.client.ui.IsWidget;

public interface VisualizeView extends IsWidget {

	public interface Presenter {

		void refreshData();
	}
}
