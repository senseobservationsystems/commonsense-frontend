package nl.sense_os.commonsense.client.states;

import java.util.List;

import nl.sense_os.commonsense.client.visualization.timeline.TimeLinePanel2;
import nl.sense_os.commonsense.shared.SensorModel;

public class FeedbackPanel extends TimeLinePanel2 {

    public FeedbackPanel(List<SensorModel> sensors, long start, long end, String title) {
        super(sensors, start, end, title);
        this.tlineOpts.setSelectable(true);
        this.tlineOpts.setEditable(true);
        this.tlineOpts.setStackEvents(false);
    }
}
