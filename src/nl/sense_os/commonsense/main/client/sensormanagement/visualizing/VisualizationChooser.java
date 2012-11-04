package nl.sense_os.commonsense.main.client.sensormanagement.visualizing;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.sensormanagement.visualizing.VisualizationChooserView.Presenter;
import nl.sense_os.commonsense.main.client.shared.event.NewVisualizationEvent;
import nl.sense_os.commonsense.main.client.visualization.VisualizePlace;

public class VisualizationChooser implements Presenter {

    private static final Logger LOG = Logger.getLogger(VisualizationChooser.class.getName());
    private VisualizationChooserView view;
    private MainClientFactory clientFactory;

    public VisualizationChooser(MainClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void onVisualizationChoice(List<GxtSensor> sensors, int type, long start, long end,
            boolean subsample) {
        // hide chooser
        if (null != view) {
            view.hideWindow();
        } else {
            LOG.warning("Cannot hide visualization window!");
        }

        // fire event
        NewVisualizationEvent event = new NewVisualizationEvent(sensors, type, start, end,
                subsample);
        clientFactory.getEventBus().fireEvent(event);

        // change place
        clientFactory.getPlaceController().goTo(
                new VisualizePlace(sensors, type, start, end, subsample));
    }

    public void start(List<GxtSensor> sensors) {
        view = clientFactory.getVisualizationChooserView();
        view.setPresenter(this);
        view.showWindow(sensors);
    }
}
