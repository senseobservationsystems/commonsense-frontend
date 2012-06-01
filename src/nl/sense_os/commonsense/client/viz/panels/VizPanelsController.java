package nl.sense_os.commonsense.client.viz.panels;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.states.feedback.FeedbackEvents;
import nl.sense_os.commonsense.client.viz.data.DataEvents;
import nl.sense_os.commonsense.client.viz.panels.map.MapVizView;
import nl.sense_os.commonsense.client.viz.panels.table.TableVizView;
import nl.sense_os.commonsense.client.viz.panels.timeline.TimeLineVizView;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;

public class VizPanelsController extends Controller {

    private static final Logger LOG = Logger.getLogger(VizPanelsController.class.getName());

    public VizPanelsController() {
        registerEventTypes(VizPanelEvents.ShowTimeLine, VizPanelEvents.ShowTable,
                VizPanelEvents.ShowMap, VizPanelEvents.ShowNetwork, FeedbackEvents.ShowFeedback);
        registerEventTypes(DataEvents.DataReceived);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(VizPanelEvents.ShowMap)) {
            LOG.finest("Create new map visualization view...");
            MapVizView view = new MapVizView(this);
            forwardToView(view, event);

        } else if (type.equals(VizPanelEvents.ShowNetwork)) {
            LOG.warning("ShowNetwork not implemented");

        } else if (type.equals(VizPanelEvents.ShowTable)) {
            LOG.finest("Create new table visualization view...");
            TableVizView view = new TableVizView(this);
            forwardToView(view, event);

        } else if (type.equals(VizPanelEvents.ShowTimeLine)) {
            LOG.finest("Create new timeline visualization view...");
            TimeLineVizView view = new TimeLineVizView(this);
            forwardToView(view, event);

        } else if (type.equals(FeedbackEvents.ShowFeedback)) {
            LOG.finest("Create new feedback visualization view...");
            // TODO

        } else {
            LOG.severe("Unexpected event: " + event);
        }
    }
}
