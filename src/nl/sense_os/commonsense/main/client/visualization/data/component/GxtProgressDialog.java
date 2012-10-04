package nl.sense_os.commonsense.main.client.visualization.data.component;

import nl.sense_os.commonsense.main.client.gxt.component.CenteredWindow;
import nl.sense_os.commonsense.main.client.visualization.data.ProgressView;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.i18n.client.NumberFormat;

public class GxtProgressDialog extends CenteredWindow implements ProgressView {

	private final ProgressBar mainProgress;
	private final ProgressBar subProgress;

	public GxtProgressDialog() {
		setLayout(new FitLayout());
		setHeading("Please wait...");
		setSize(300, 165);

		subProgress = new ProgressBar();
		mainProgress = new ProgressBar();

		FormPanel form = new FormPanel();
		form.setBodyBorder(false);
		form.setHeaderVisible(false);
		form.setScrollMode(Scroll.AUTOY);
		form.setLabelAlign(LabelAlign.TOP);
		form.setLabelSeparator(" ");

		subProgress.auto();
		AdapterField subProgressField = new AdapterField(subProgress);
		subProgressField.setFieldLabel("Requesting sensor data...");
		form.add(subProgressField, new FormData("-5"));

		// spacer
		form.add(new Text(""));

		mainProgress.updateText("[ 0 / 1 ]");
		AdapterField mainProgressField = new AdapterField(mainProgress);
		mainProgressField.setFieldLabel("Sensors completed:");
		form.add(mainProgressField, new FormData("-5"));

		add(form);
	}

	@Override
	public void hideWindow() {
		hide();
	}

	@Override
	public void showWindow(int tasks) {

		mainProgress.updateText("[ 0 / " + tasks + " ]");

		show();
	}

	public void updateMainProgress(int progress, int total) {
		// logger.fine( "Update main: [ " + progress + " / " + total + " ]");

		double value = ((double) progress) / ((double) total);
		mainProgress.updateProgress(value, "[ " + progress + " / " + total + " ]");

		// also reset the subtask progress bar
		if (progress != total) {
			subProgress.reset();
		}
	}

	public void updateSubProgress(double progress, double total, String text) {
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
}
