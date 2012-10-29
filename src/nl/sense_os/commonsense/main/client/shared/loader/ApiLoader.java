package nl.sense_os.commonsense.main.client.shared.loader;

import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.util.Constants;

import com.google.gwt.maps.client.Maps;
import com.google.gwt.visualization.client.VisualizationUtils;

public class ApiLoader implements Loader {

    private static final Logger LOG = Logger.getLogger(ApiLoader.class.getName());
    private boolean isMapsApiLoaded;
    private boolean isVizApiLoaded;
    private Callback callback;

    @Override
    public void load(Callback callback) {
        this.callback = callback;
        loadMapsApi();
        loadVizApi();
    }

    /**
     * Loads the Google Maps API
     */
    private void loadMapsApi() {

        // Asynchronously load the Maps API.
        if (Maps.isLoaded()) {
            LOG.fine("Google Maps API already loaded");
            return;
        }

        Maps.loadMapsApi(Constants.MapsKeys.MAPS_KEY, "2", false, new Runnable() {

            @Override
            public void run() {
                LOG.config("Google Maps API (version " + Maps.getVersion() + ") loaded");
                isMapsApiLoaded = true;
                onLoadComplete();
            }
        });
    }

    /**
     * Loads the Google visualization API
     */
    private void loadVizApi() {

        // Load the visualization API
        this.isVizApiLoaded = false;
        final Runnable vizCallback = new Runnable() {

            @Override
            public void run() {
                LOG.config("Google Visualization API loaded");
                isVizApiLoaded = true;
                onLoadComplete();
            }
        };
        VisualizationUtils.loadVisualizationApi(vizCallback, new String[] {});
    }

    private synchronized void onLoadComplete() {
        if (isVizApiLoaded && isMapsApiLoaded && null != callback) {
            callback.onSuccess(true);
        }
    }
}
