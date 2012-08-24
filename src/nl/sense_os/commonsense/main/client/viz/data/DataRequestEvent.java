package nl.sense_os.commonsense.main.client.viz.data;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;

import com.extjs.gxt.ui.client.mvc.AppEvent;

public class DataRequestEvent extends AppEvent {

    private long start, end;
    private List<GxtSensor> sensors;
    private boolean showProgress, subsample;

    public DataRequestEvent() {
        super(DataEvents.DataRequest);
        sensors = new ArrayList<GxtSensor>();
    }

    public DataRequestEvent(long start, long end, List<GxtSensor> sensors, boolean subsample,
            boolean showProgress) {
        this();
        setStart(start);
        setEnd(end);
        setSensors(sensors);
        setShowProgress(showProgress);
        setSubsample(subsample);
    }

    public long getEnd() {
        return end;
    }

    public List<GxtSensor> getSensors() {
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

    public void setSensors(List<GxtSensor> sensors) {
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
