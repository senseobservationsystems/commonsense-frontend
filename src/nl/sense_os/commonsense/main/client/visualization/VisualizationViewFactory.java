package nl.sense_os.commonsense.main.client.visualization;

import java.util.HashMap;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.shared.event.NewVisualizationEvent;
import nl.sense_os.commonsense.main.client.visualization.component.map.MapVisualization;
import nl.sense_os.commonsense.main.client.visualization.component.table.TableVisualization;
import nl.sense_os.commonsense.main.client.visualization.component.timeline.TimelineVisualization;

public class VisualizationViewFactory {

    private static final Logger LOG = Logger.getLogger(VisualizationViewFactory.class.getName());
    private static final HashMap<String, VisualizationView> MAP = new HashMap<String, VisualizationView>();
    private static VisualizationViewFactory INSTANCE;

    public static VisualizationViewFactory getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new VisualizationViewFactory();
        }
        return INSTANCE;
    }

    private VisualizationViewFactory() {
        // private constructor to enforce singleton pattern
    }

    public VisualizationView getView(VisualizePlace place) {

        // see if the visualization is already in our map
        String token = new VisualizePlace.Tokenizer().getToken(place);
        VisualizationView view = MAP.get(token);

        if (null == view) {

            // create new view based on the visualization type
            switch (place.getType()) {
            case NewVisualizationEvent.TIMELINE:
                view = new TimelineVisualization(place.getSensors(), place.getStart(),
                        place.getEnd(), place.isSubsample());
                break;

            case NewVisualizationEvent.TABLE:
                view = new TableVisualization(place.getSensors(), place.getStart(), place.getEnd(),
                        place.isSubsample());
                break;
            case NewVisualizationEvent.MAP:
                view = new MapVisualization(place.getSensors(), place.getStart(), place.getEnd(),
                        place.isSubsample());
                break;
            default:
                LOG.warning("Unsupported visualization!");
            }

            // store the view for reuse
            MAP.put(token, view);
        }

        return view;
    }
}
