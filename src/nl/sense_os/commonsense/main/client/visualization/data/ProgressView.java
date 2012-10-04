package nl.sense_os.commonsense.main.client.visualization.data;

import com.google.gwt.user.client.ui.IsWidget;

public interface ProgressView extends IsWidget {

	/**
	 * Updates the sensor number progress message box, displaying the number of the sensor we are
	 * working with.
	 * 
	 * @param progress
	 *            Number of item completed.
	 * @param total
	 *            Total number of items.
	 */
	void updateMainProgress(int progress, int total);

	/**
	 * Updates the sensor data progress message box, displaying the completion percentage.
	 * 
	 * @param progress
	 *            Number of item completed.
	 * @param total
	 *            Total number of items
	 * @param text
	 *            Text to show in the progress bar
	 */
	void updateSubProgress(double progress, double total, String text);

	/**
	 * Shows the progress message box, with empty progress bar.
	 */
	void showWindow(int tasks);

	/**
	 * Closes the progress message box.
	 */
	void hideWindow();
}
