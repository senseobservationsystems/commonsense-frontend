package nl.sense_os.commonsense.client.views;

import java.util.List;

import nl.sense_os.commonsense.client.events.VizEvents;
import nl.sense_os.commonsense.client.utility.Log;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class VizMap extends View {

    private static final String TAG = "VizMap";
    private ContentPanel panel;
    private List<TreeModel> sensors;

    public VizMap(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(VizEvents.ShowMap)) {
            // Log.d(TAG, "Show");
            onShow(event);
        } else {
            Log.e(TAG, "Unexpected event type received!");
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.panel = new ContentPanel(new FitLayout());
        this.panel.setHeaderVisible(false);
        this.panel.setBodyBorder(false);
        this.panel.setScrollMode(Scroll.NONE);
    }

    private void onShow(AppEvent event) {

        this.sensors = event.<List<TreeModel>> getData();

        AppEvent response = new AppEvent(VizEvents.MapReady);
        response.setData(this.panel);
        Dispatcher.forwardEvent(response);
    }
}
