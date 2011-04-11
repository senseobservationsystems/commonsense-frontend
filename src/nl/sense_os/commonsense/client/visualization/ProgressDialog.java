package nl.sense_os.commonsense.client.visualization;

import nl.sense_os.commonsense.client.utility.Log;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.MessageBox;

public class ProgressDialog extends View {

    private static final String TAG = "ProgressDialog";
    private MessageBox messageBox;

    public ProgressDialog(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(VizEvents.ShowProgress)) {
            showWindow();

        } else if (type.equals(VizEvents.UpdateProgress)) {
            final double progress = event.getData("progress");
            final double total = event.getData("total");
            update(progress, total);

        } else if (type.equals(VizEvents.HideProgress)) {
            hideWindow();

        } else {
            Log.w(TAG, "Unexpected event type!");
        }

    }

    private void hideWindow() {
        messageBox.close();

    }

    private void update(double progress, double total) {
        messageBox.updateProgress(progress / total, "Getting sensor data... [" + (int) progress
                + "/" + (int) total + "]");
    }

    private void showWindow() {
        messageBox = MessageBox.progress(null, "Please wait...", "Getting sensor data...");
    }

}
