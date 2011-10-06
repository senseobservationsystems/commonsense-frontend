package nl.sense_os.commonsense.client.viz.data;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.viz.panels.VizPanel;

import com.extjs.gxt.ui.client.mvc.AppEvent;

public class DataRequestEvent extends AppEvent {

    private long start, end;
    private List<SensorModel> sensors;
    private VizPanel panel;
    private boolean showProgress, subsample;

    public DataRequestEvent() {
        super(DataEvents.DataRequest);
        sensors = new ArrayList<SensorModel>();
    }

    public DataRequestEvent(long start, long end, List<SensorModel> sensors, boolean subsample,
            boolean showProgress, VizPanel panel) {
        this();
        setStart(start);
        setEnd(end);
        setSensors(sensors);
        setPanel(panel);
        setShowProgress(showProgress);
        setSubsample(subsample);
    }

    public long getEnd() {
        return end;
    }

    public VizPanel getPanel() {
        return panel;
    }

    public List<SensorModel> getSensors() {
        return sensors;
    }

    public long getStart() {
        return start;
    }

    public boolean isShowProgress() {
        return showProgress;
    }

    public boolean isSubsample() {
        return subsample;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public void setPanel(VizPanel panel) {
        this.panel = panel;
    }

    public void setSensors(List<SensorModel> sensors) {
        this.sensors = sensors;
    }

    public void setShowProgress(boolean showProgress) {
        this.showProgress = showProgress;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public void setSubsample(boolean subsample) {
        this.subsample = subsample;
    }
}
