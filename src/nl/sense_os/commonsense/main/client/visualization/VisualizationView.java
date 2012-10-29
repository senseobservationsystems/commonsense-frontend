package nl.sense_os.commonsense.main.client.visualization;

import nl.sense_os.commonsense.shared.client.model.Timeseries;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface VisualizationView extends IsWidget {

	public interface Presenter {

        /**
         * Gets the data for the view the very first time
         */
        void getData();

        /**
         * Silently refreshes the data for the view
         */
		void refreshData();
	}

    /**
     * Callback to notify the panel that it is being displayed
     * 
     * @param parent
     */
    void onShow(Widget parent);

    /**
     * @param presenter
     */
	void setPresenter(Presenter presenter);

    /**
     * Tells the view to visualize the given data
     * 
     * @param data
     */
	void visualize(JsArray<Timeseries> data);
}
