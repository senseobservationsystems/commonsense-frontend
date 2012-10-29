package nl.sense_os.commonsense.main.client.visualization;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.event.DataRequestEvent;
import nl.sense_os.commonsense.main.client.event.NewSensorDataEvent;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.shared.client.model.Timeseries;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class VisualizeActivity extends AbstractActivity implements VisualizationView.Presenter,
        NewSensorDataEvent.Handler {

    private static final Logger LOG = Logger.getLogger(VisualizeActivity.class.getName());

    /**
     * Appends new data to the old data
     * 
     * @param oldData
     *            Original set of timeseries
     * @param newData
     *            New timeseries that need to be appended
     */
    private static JsArray<Timeseries> appendNewData(JsArray<Timeseries> oldData,
            JsArray<Timeseries> newData) {
        if (null == oldData) {
            LOG.fine("No old data to append to");
            return newData;

        } else {
            for (int i = 0; i < newData.length(); i++) {
                Timeseries toAppend = newData.get(i);
                boolean appended = false;
                for (int j = 0; j < oldData.length(); j++) {
                    Timeseries original = oldData.get(j);
                    if (toAppend.getLabel().equals(original.getLabel())
                            && toAppend.getId() == original.getId()) {
                        LOG.fine("Append data to " + original.getLabel());
                        original.append(toAppend);
                        appended = true;
                        break;
                    }
                }
                if (!appended) {
                    LOG.fine("Add new timeseries to the visualization data " + toAppend.getLabel());
                    oldData.push(toAppend);
                }
            }
            return oldData;
        }
    }

    /**
     * Used to obtain views, eventBus, placeController. Alternatively, could be injected via GIN.
     */
    private MainClientFactory clientFactory;
    private VisualizePlace place;
    private long start;
    private long end;
    private boolean subsample;

    private List<GxtSensor> sensors;

    private VisualizationView view;

    private JsArray<Timeseries> data;

    public VisualizeActivity(VisualizePlace place, MainClientFactory clientFactory) {
        this.place = place;
        start = place.getStart();
        end = place.getEnd();
        subsample = place.isSubsample();
        sensors = place.getSensors();
        this.clientFactory = clientFactory;
    }

    @Override
    public void getData() {
        DataRequestEvent dataRequest = new DataRequestEvent(start, end, sensors, subsample, true);
        clientFactory.getEventBus().fireEvent(dataRequest);
    }

    @Override
    public void onNewSensorData(NewSensorDataEvent event) {
        if (event.getSensors().equals(sensors)) {
            LOG.fine("received new sensor data!");
            JsArray<Timeseries> newData = event.getSensorData();
            data = appendNewData(data, newData);

            view.visualize(data);
        }
    }

    @Override
    public void refreshData() {
        LOG.fine("Refresh data...");

        // TODO don't refresh when the user has left the visualization section of the app

        if (null != data) {
            for (GxtSensor sensor : sensors) {

                // find the latest data point for which we have data and refresh from this point
                long refreshStart = start;
                for (int i = 0; i < data.length(); i++) {
                    Timeseries ts = data.get(i);
                    if (ts.getId() == sensor.getId()) {
                        refreshStart = ts.getEnd() > refreshStart ? ts.getEnd() : refreshStart;
                    }
                }

                DateTimeFormat dtf = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL);
                LOG.fine("Refresh start time: " + dtf.format(new Date(refreshStart)));

                // submit request event
                DataRequestEvent dataRequest = new DataRequestEvent(refreshStart, end, sensors,
                        subsample, false);
                clientFactory.getEventBus().fireEvent(dataRequest);
            }

        } else {
            getData();
        }
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        LOG.info("Start 'visualize' activity");

        // get the view
        view = VisualizationViewFactory.getInstance().getView(place);
        view.setPresenter(this);

        // add to layout
        LayoutContainer parent = clientFactory.getMainView().getGxtActivityPanel();
        parent.removeAll();
        parent.add(view.asWidget());

        // listen for new sensor data
        clientFactory.getEventBus().addHandler(NewSensorDataEvent.TYPE, this);

        // notify the view
        view.onShow(parent);
    }
}
