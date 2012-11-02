package nl.sense_os.commonsense.main.client.visualization;

import java.util.HashMap;

import nl.sense_os.commonsense.main.client.MainClientFactory;

public class VisualizeActivityFactory {

    private static final HashMap<String, VisualizeActivity> MAP = new HashMap<String, VisualizeActivity>();
    private static VisualizeActivityFactory INSTANCE;

    public static VisualizeActivityFactory getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new VisualizeActivityFactory();
        }
        return INSTANCE;
    }

    private VisualizeActivityFactory() {
        // private constructor to enforce singleton pattern
    }

    public VisualizeActivity getActivity(VisualizePlace place, MainClientFactory clientFactory) {

        // see if the visualization is already in our map
        String token = new VisualizePlace.Tokenizer().getToken(place);
        VisualizeActivity activity = MAP.get(token);

        if (null == activity) {
            // create new activity
            activity = new VisualizeActivity(place, clientFactory);

            // store the view for reuse
            MAP.put(token, activity);
        }

        return activity;
    }
}
