package nl.sense_os.commonsense.main.client.viz.data;

import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.ext.component.CenteredWindow;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.i18n.client.NumberFormat;

public class ProgressDialog extends View {

    private static final Logger logger = Logger.getLogger("ProgressDialog");
    private CenteredWindow window;
    private ProgressBar mainProgress;
    private ProgressBar subProgress;

    public ProgressDialog(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(DataEvents.ShowProgress)) {
            // logger.fine( "Show");
            final int tasks = event.getData("tasks");
            showWindow(tasks);

        } else if (type.equals(DataEvents.UpdateDataProgress)) {
            final double progress = event.getData("progress");
            final double total = event.getData("total");
            final String text = event.getData("text");
            updateSubProgress(progress, total, text);

        } else if (type.equals(DataEvents.UpdateMainProgress)) {
            final int progress = event.getData("progress");
            final int total = event.getData("total");
            updateMainProgress(progress, total);

        } else if (type.equals(DataEvents.HideProgress)) {
            // logger.fine( "Hide");
            hideWindow();

        } else {
            logger.warning("Unexpected event type!");
        }
    }

    /**
     * Closes the progress message box.
     */
    private void hideWindow() {
        if (window != null) {
            window.hide();
        }
        window = null;
    }

    /**
     * Shows the progress message box, with empty progress bar.
     */
    private void showWindow(int tasks) {

        if (window != null) {
            window.hide();
        }

        window = new CenteredWindow();
        window.setLayout(new FitLayout());
        window.setHeadingText("Please wait...");
        window.setSize(300, 165);

        FormPanel form = new FormPanel();
        form.setBodyBorder(false);
        form.setHeaderVisible(false);
        form.setScrollMode(Scroll.AUTOY);
        form.setLabelAlign(LabelAlign.TOP);
        form.setLabelSeparator(" ");

        subProgress = new ProgressBar();
        subProgress.auto();
        AdapterField subProgressField = new AdapterField(subProgress);
        subProgressField.setFieldLabel("Requesting sensor data...");
        form.add(subProgressField, new FormData("-5"));

        // spacer
        form.add(new Text(""));

        mainProgress = new ProgressBar();
        mainProgress.updateText("[ 0 / " + tasks + " ]");
        AdapterField mainProgressField = new AdapterField(mainProgress);
        mainProgressField.setFieldLabel("Sensors completed:");
        form.add(mainProgressField, new FormData("-5"));

        window.add(form);

        window.show();
    }

    /**
     * Updates the sensor data progress message box, displaying the completion percentage.
     * 
     * @param progress
     *            Number of item completed.
     * @param total
     *            Total number of items.
     */
    private void updateSubProgress(double progress, double total, String text) {
        if (progress > 0 && total > 0) {
            double value = progress / total;
            String valueString = NumberFormat.getPercentFormat().format(value);
            // logger.fine( "Update sub: " + value + " ( " + progress + " / " + total + ")");
            subProgress.updateProgress(value, valueString);
        }

        if (text != null) {
            // TODO update field label
        }
    }

    /**
     * Updates the sensor number progress message box, displaying the number of the sensor we are
     * working with.
     * 
     * @param progress
     *            Number of item completed.
     * @param total
     *            Total number of items.
     */
    private void updateMainProgress(int progress, int total) {
        // logger.fine( "Update main: [ " + progress + " / " + total + " ]");

        double value = ((double) progress) / ((double) total);
        mainProgress.updateProgress(value, "[ " + progress + " / " + total + " ]");

        // also reset the subtask progress bar
        if (progress != total) {
            subProgress.reset();
        }
    }
}
