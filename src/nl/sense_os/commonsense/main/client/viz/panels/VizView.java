package nl.sense_os.commonsense.main.client.viz.panels;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.model.Timeseries;
import nl.sense_os.commonsense.common.client.util.Constants;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
import nl.sense_os.commonsense.main.client.viz.data.DataRequestEvent;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Timer;

public abstract class VizView extends View {

	/**
	 * Timer to periodically refresh the data for the visualisation.
	 */
	private class RefreshTimer extends Timer {

		@Override
		public void run() {
			onRefresh();
		}
	}

	private static final Logger LOG = Logger.getLogger(VizView.class.getName());

	private static final int REFRESH_PERIOD = 1000 * 10;
	private RefreshTimer refreshTimer;
	private boolean isAutoRefresh;

	/**
	 * Panel that all visualisations should be added to.
	 */
	protected TabPanel tabPanel;

	public VizView(Controller c) {
		super(c);
	}

	protected abstract void onRefresh();

	protected void addRefreshListeners(VizPanel panel, final TabItem item) {

		final ToolButton refresh = panel.getRefresh();
		if (null != refresh) {
			refresh.addSelectionListener(new SelectionListener<IconButtonEvent>() {

				@Override
				public void componentSelected(IconButtonEvent ce) {
					onRefresh();
				}
			});
		}

		final ToolButton autoRefresh = panel.getAutoRefresh();
		if (null != autoRefresh) {
			autoRefresh.addSelectionListener(new SelectionListener<IconButtonEvent>() {

				@Override
				public void componentSelected(IconButtonEvent ce) {

					if (null == refreshTimer) {
						registerHideListeners(item);
						refreshTimer = new RefreshTimer();
						isAutoRefresh = false;
					}

					if (!isAutoRefresh) {
						onRefresh();
						refreshTimer.scheduleRepeating(REFRESH_PERIOD);
						isAutoRefresh = true;
						autoRefresh.setToolTip("stop autorefresh");
						autoRefresh.setStylePrimaryName("x-tool-pin");
					} else {
						refreshTimer.cancel();
						isAutoRefresh = false;
						autoRefresh.setToolTip("start autorefresh");
						autoRefresh.setStylePrimaryName("x-tool-right");
					}
				}
			});
		}

		panel.addListener(Events.BeforeHide, new Listener<ComponentEvent>() {

			@Override
			public void handleEvent(ComponentEvent be) {
				if (null != refreshTimer) {
					refreshTimer.cancel();
					isAutoRefresh = false;
					refreshTimer = null;
				}

				// remove tab item from tab panel
				item.removeFromParent();
			}
		});
	}

	protected String createChartTitle(List<ExtSensor> sensors) {
		String title = null;
		for (ExtSensor sensor : sensors) {
			title = sensor.getDisplayName() + ", ";
		}

		// remove trailing ", "
		title = title.substring(0, title.length() - 2);

		// // trim to max length
		// if (title.length() > 18) {
		// title = title.substring(0, 15) + "...";
		// }
		return title;
	}

	@Override
	protected void initialize() {
		super.initialize();

		tabPanel = Registry.get(Constants.REG_VIZPANEL);
		if (tabPanel == null) {
			LOG.severe("Cannot find main visualization panel!");
			return;
		}
	}

	/**
	 * Appends new data to the old data
	 * 
	 * @param oldData
	 *            Original set of timeseries
	 * @param newData
	 *            New timeseries that need to be appended
	 */
	protected JsArray<Timeseries> appendNewData(JsArray<Timeseries> oldData,
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
	 * Dispatches request for refreshing the sensor data.
	 */
	protected void refreshData(JsArray<Timeseries> currentData, List<ExtSensor> sensors,
			long start, long end, boolean subsample) {
		LOG.fine("Refresh data...");

		// TODO don't refresh when the user has left the visualization section of the app

		if (null != sensors) {

			for (ExtSensor sensor : sensors) {

				// find the latest data point for which we have data and refresh from this point
				long refreshStart = start;
				for (int i = 0; i < currentData.length(); i++) {
					Timeseries ts = currentData.get(i);
					if (ts.getId() == sensor.getId()) {
						LOG.finest("Found time series for sensor " + sensor.getDisplayName());
						LOG.fine("time series end: "
								+ DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL).format(
										new Date(ts.getEnd())));
						refreshStart = ts.getEnd() > refreshStart ? ts.getEnd() : refreshStart;

					}
				}

				DateTimeFormat dtf = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL);
				LOG.fine("Refresh start time: " + dtf.format(new Date(refreshStart)));

				// submit request event
				DataRequestEvent refreshRequest = new DataRequestEvent(refreshStart, end, sensors,
						true, false);
				refreshRequest.setSource(this);
				Dispatcher.forwardEvent(refreshRequest);
			}

		} else {
			LOG.warning("Cannot refresh data: list of sensors is null");
		}
	}

	/**
	 * Registers listeners to keep track if the visibility of this panel. Used to stop the auto
	 * refresh requests when the panel is hidden.
	 */
	private void registerHideListeners(TabItem item) {

		item.addListener(Events.Hide, new Listener<ComponentEvent>() {

			@Override
			public void handleEvent(ComponentEvent be) {
				if (isAutoRefresh) {
					refreshTimer.cancel();
				}
			}
		});
		item.addListener(Events.Close, new Listener<ComponentEvent>() {

			@Override
			public void handleEvent(ComponentEvent be) {
				if (isAutoRefresh) {
					refreshTimer.cancel();
				}
			}
		});
		item.addListener(Events.Remove, new Listener<ComponentEvent>() {

			@Override
			public void handleEvent(ComponentEvent be) {
				if (isAutoRefresh) {
					refreshTimer.cancel();
				}
			}
		});
		item.addListener(Events.Show, new Listener<ComponentEvent>() {

			@Override
			public void handleEvent(ComponentEvent be) {
				if (isAutoRefresh) {
					onRefresh();
					refreshTimer.scheduleRepeating(REFRESH_PERIOD);
				}
			}
		});
	}

	/**
	 * Stores sensors and time range, and dispatches event to request sensor data.
	 * 
	 * @param sensors
	 *            List with SensorModels to visualize.
	 * @param start
	 *            Start time of the period to display.
	 * @param end
	 *            End time of the period to display.
	 * @param subsample
	 * 
	 */
	protected void requestData(List<ExtSensor> sensors, long start, long end, boolean subsample) {
		LOG.fine("Request data...");
		DataRequestEvent dataRequest = new DataRequestEvent(start, end, sensors, subsample, true);
		dataRequest.setSource(this);
		Dispatcher.forwardEvent(dataRequest);
	}
}
