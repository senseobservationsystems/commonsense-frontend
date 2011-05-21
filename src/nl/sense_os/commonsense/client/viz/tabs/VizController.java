package nl.sense_os.commonsense.client.viz.tabs;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.states.feedback.FeedbackEvents;
import nl.sense_os.commonsense.client.viz.choice.VizTypeChooser;
import nl.sense_os.commonsense.shared.constants.Keys;

import com.chap.links.client.Timeline;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.user.client.Timer;
import com.google.gwt.visualization.client.VisualizationUtils;

public class VizController extends Controller {

    private static final Logger logger = Logger.getLogger("VizController");

    private View vizView;
    private View typeChooser;
    private boolean isVizApiLoaded;

    public VizController() {
        registerEventTypes(MainEvents.Init);
        registerEventTypes(LoginEvents.LoggedOut);

        registerEventTypes(VizEvents.Show);
        registerEventTypes(VizEvents.ShowTypeChoice, VizEvents.TypeChoiceCancelled);
        registerEventTypes(VizEvents.ShowTimeLine, VizEvents.ShowTable, VizEvents.ShowMap,
                VizEvents.ShowNetwork, FeedbackEvents.ShowFeedback);
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(VizEvents.ShowTypeChoice) || type.equals(VizEvents.TypeChoiceCancelled)) {
            forwardToView(this.typeChooser, event);

        } else {
            forwardToView(this.vizView, event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.vizView = new VizView(this);
        this.typeChooser = new VizTypeChooser(this);

        loadMapsApi();
        loadVizApi();
    }

    /**
     * Loads the Google Maps API when the controller is initialized. If loading fails, a popup
     * window is shown.
     */
    private void loadMapsApi() {

        // Asynchronously load the Maps API.
        if (Maps.isLoaded()) {
            logger.fine("Google Maps API already loaded");
            return;
        }

        final String url = GWT.getModuleBaseURL();
        String key = Keys.MAPS_KEY_STABLE;
        if (url.contains("common-sense-test")) {
            logger.info("Using Google Maps API key for \'common-sense-test\'...");
            key = Keys.MAPS_KEY_TEST;
        }
        Maps.loadMapsApi(key, "2", false, new Runnable() {

            @Override
            public void run() {
                // logger.fine( "Google Maps API (version " + Maps.getVersion() + ") loaded...");
            }
        });

        // double check that the API has been loaded within 10 seconds
        new Timer() {

            @Override
            public void run() {
                if (false == Maps.isLoaded()) {
                    MessageBox.confirm(null, "Google Maps API not loaded, retry?",
                            new Listener<MessageBoxEvent>() {

                                @Override
                                public void handleEvent(MessageBoxEvent be) {
                                    final Button b = be.getButtonClicked();
                                    if ("yes".equalsIgnoreCase(b.getText())) {
                                        loadMapsApi();
                                    }
                                }
                            });
                }
            }
        }.schedule(1000 * 10);
    }

    private void loadVizApi() {

        // Load the visualization API
        this.isVizApiLoaded = false;
        final Runnable vizCallback = new Runnable() {

            @Override
            public void run() {
                // logger.fine( "Google Visualization API loaded...");
                isVizApiLoaded = true;
            }
        };
        VisualizationUtils.loadVisualizationApi(vizCallback, Timeline.PACKAGE);

        // double check that the API has been loaded within 10 seconds
        new Timer() {

            @Override
            public void run() {
                if (false == isVizApiLoaded) {
                    MessageBox.confirm(null, "Google visualization API not loaded, retry?",
                            new Listener<MessageBoxEvent>() {

                                @Override
                                public void handleEvent(MessageBoxEvent be) {
                                    final Button b = be.getButtonClicked();
                                    if ("yes".equalsIgnoreCase(b.getText())) {
                                        loadVizApi();
                                    }
                                }
                            });
                }
            }
        }.schedule(1000 * 10);
    }
}
