package nl.sense_os.commonsense.client.viz.panels;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.states.feedback.FeedbackEvents;
import nl.sense_os.commonsense.client.viz.data.DataEvents;

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
        LOG.finest("Create new visualization view...");
        VizView view = new VizView(this);
        forwardToView(view, event);
    }
}
