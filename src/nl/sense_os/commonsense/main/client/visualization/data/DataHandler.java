package nl.sense_os.commonsense.main.client.visualization.data;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.lib.client.communication.CommonSenseClient;
import nl.sense_os.commonsense.lib.client.model.httpresponse.GetSensorDataResponse;
import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.shared.event.DataRequestEvent;
import nl.sense_os.commonsense.main.client.shared.event.LatestValuesRequestEvent;
import nl.sense_os.commonsense.main.client.shared.event.NewSensorDataEvent;
import nl.sense_os.commonsense.main.client.visualization.data.cache.Cache;
import nl.sense_os.commonsense.shared.client.model.Timeseries;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public class DataHandler implements DataRequestEvent.Handler, LatestValuesRequestEvent.Handler {

	private static final Logger LOG = Logger.getLogger(DataHandler.class.getName());
	private MainClientFactory clientFactory;
	private static final int PER_PAGE = 1000; // max: 1000

	public DataHandler(MainClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	private int calcInterval(long start, long end) {
		long realEnd = end == -1 ? System.currentTimeMillis() : end;
		double interval = Math.ceil(((realEnd - start) / 1000000d));
		if (interval < 60) {
			interval = 0;
		} else if (interval < 300) {
			interval = 60;
		} else if (interval < 600) {
			interval = 300;
		} else if (interval < 1800) {
			interval = 600;
		} else if (interval < 3600) {
			interval = 1800;
		} else if (interval < 60 * 60 * 24 * 31 * 6 / 1000d) {
			// for requests for up to 6 months in range, set interval to one hour
			interval = 3600;
		} else if (interval < 60 * 60 * 24 * 365 * 2 / 1000d) {
			// for requests for up to 2 years in range, set interval to one day
			interval = 86400;
		} else {
			// crazy large interval for crazy large requests
			interval = 604800;
		}
		return Double.valueOf(interval).intValue();
	}

	private void getLatestValues(final List<GxtSensor> sensors, final int index, final Object source) {
		if (index < sensors.size()) {

			GxtSensor sensor = sensors.get(index);

			// prepare request callback
			RequestCallback callback = new RequestCallback() {

				@Override
				public void onError(Request request, Throwable exception) {
					LOG.warning("GET last data onError callback: " + exception.getMessage());
					onLatestValueFailure(0, source);
				}

				@Override
				public void onResponseReceived(Request request, Response response) {
					LOG.finest("GET last data response received: " + response.getStatusText());
					int statusCode = response.getStatusCode();
					if (Response.SC_OK == statusCode) {
						onLatestValueSuccess(response.getText(), sensors, index, source);
					} else {
						LOG.warning("GET last data returned incorrect status: " + statusCode);
						onLatestValueFailure(statusCode, source);
					}
				}
			};

			// send request
            CommonSenseClient.getClient().getSensorData(callback, sensor.getId(), null, null, null,
                    null, null,
                    null, null, true, null);

		} else {
			// hoooray we're done!
			onLatestValuesComplete(sensors, source);
		}
	}

	/**
	 * Requests one page of data points from a list of sensors, between a given start and end date.
	 * CommonSense will subsample the data to ensure that all data fits on that one page (1000
	 * points).
	 * 
	 * @param start
	 *            Start time of the time range, in milliseconds.
	 * @param end
	 *            End time of the time range, in milliseconds. Set to -1 to leave the end data
	 *            unspecified.
	 * @param sensors
	 *            List of sensors to request data for. The data is fetched for one sensor at a time.
	 * @param sensorIndex
	 *            Index of the current sensor in the list.
	 * @param pageIndex
	 *            Index of the current page in the sensor data.
	 * @param subsample
	 *            Boolean to request subsampled data.
	 * @param showProgress
	 *            Set to true to display a progress dialog.
	 */
	private void getSensorData(final long start, final long end, final List<GxtSensor> sensors,
			final int sensorIndex, final int pageIndex, final boolean subsample,
			final boolean showProgress, final Object source) {
		LOG.fine("Request data...");

		if (sensorIndex < sensors.size()) {

			final GxtSensor sensor = sensors.get(sensorIndex);

			// remove preexisting data from the cache, because reusing it is too complicated
			if (pageIndex == 0) {
				Cache.remove(sensor);
			}

			// request parameters
            Long startDate = start;
            Long endDate = end != -1 ? end : null;
            Integer interval = subsample ? calcInterval(start, end) : null;

			// prepare request callback
			RequestCallback callback = new RequestCallback() {

				@Override
				public void onError(Request request, Throwable exception) {
					LOG.warning("GET subsampled data onError callback: " + exception.getMessage());
					onDataFailed(-1, showProgress);
				}

				@Override
				public void onResponseReceived(Request request, Response response) {
					LOG.finest("GET data (subsampled) response received: "
							+ response.getStatusText());
					int statusCode = response.getStatusCode();
					if (Response.SC_OK == statusCode) {
						onGetSensorDataSuccess(response.getText(), start, end, sensors,
								sensorIndex, pageIndex, subsample, showProgress, source);
					} else {
						LOG.warning("GET data (subsampled) returned incorrect status: "
								+ statusCode);
						onDataFailed(statusCode, showProgress);
					}
				}
			};

			// send request
            CommonSenseClient.getClient().getSensorData(callback, sensor.getId(), startDate,
                    endDate, null, PER_PAGE, pageIndex, interval, null, null, null);

		} else {
			// should not happen, but just in case...
			onDataComplete(start, end, sensors, source);
		}
	}

	/**
	 * Hides the progress bar View.
	 */
	private void hideProgress() {
		clientFactory.getProgressView().hideWindow();
	}

	/**
	 * Handles the callbacks when data requests are completed. Hides the progress dialog and passes
	 * the data on to the panel that requested the data.
	 * 
	 * @param start
	 *            Start of time range of the finished requests.
	 * @param end
	 *            End of time range of the finished requests.
	 * @param sensors
	 *            List of sensors that the data was retrieved for
	 */
	private void onDataComplete(long start, long end, List<GxtSensor> sensors, Object source) {
		LOG.fine("onDataComplete...");

		hideProgress();

		// fire event
		JsArray<Timeseries> data = Cache.request(sensors, start, end);
		NewSensorDataEvent event = new NewSensorDataEvent(sensors, data);
		clientFactory.getEventBus().fireEvent(event);
	}

	/**
	 * Handles the event that a data request fails. If progress was being shown, it notifies the
	 * user that we failed.
	 * 
	 * @param code
	 *            Response code of the failed request.
	 * @param showProgress
	 *            boolean to indicate whether to notify the user.
	 */
	private void onDataFailed(int code, boolean showProgress) {
		if (showProgress) {
			hideProgress();
			MessageBox.alert(null, "Data request failed! Please try again.", null);
		}
	}

	/**
	 * Handles request for data from the user. Shows the progress dialog and starts requesting the
	 * data.
	 */
	@Override
	public void onDataRequest(DataRequestEvent event) {

		long start = event.getStart();
		long end = event.getEnd();
		List<GxtSensor> sensors = event.getSensors();
		int sensorIndex = 0;
		int pageIndex = 0;
		boolean subsample = event.isSubsample();
		boolean showProgress = event.isShowProgress();

		if (showProgress) {
			showProgress(sensors.size());
		}

		getSensorData(start, end, sensors, sensorIndex, pageIndex, subsample, showProgress,
				event.getSource());
	}

	/**
	 * Handles successful requests for subsampled data. Parses the response, stores it and moves on
	 * to the next sensor in the list.
	 * 
	 * @param response
	 *            Response from CommonSense back end, should contain sensor data points.
	 * @param start
	 *            Start of the requested time range.
	 * @param end
	 *            End of the requested time range, or -1 for no end time.
	 * @param sensors
	 *            List of sensors that we are requesting data for.
	 * @param sensorIndex
	 *            Index of the sensor that the data belongs to.
	 * @param pageIndex
	 *            Index of the page of sensor data.
	 * @param subsampled
	 *            Boolean to indicate whether the data was subsampled.
	 * @param showProgress
	 *            Boolean to indicate whether to update the user of the progress.
	 * @param source
	 *            View that requested the data.
	 */
	private void onGetSensorDataSuccess(String response, long start, long end,
			List<GxtSensor> sensors, int sensorIndex, int pageIndex, boolean subsampled,
			boolean showProgress, Object source) {
		LOG.fine("Data page success...");

		// parse the incoming data
		GetSensorDataResponse jsoResponse = GetSensorDataResponse.create(response);

		// store data in cache
		GxtSensor sensor = sensors.get(sensorIndex);
		Cache.store(sensor, start, end, jsoResponse.getData());

		if (jsoResponse.getData().length() == PER_PAGE) {
			// get next page
			pageIndex++;
			getSensorData(start, end, sensors, sensorIndex, pageIndex, subsampled, showProgress,
					source);
		} else if (sensorIndex < sensors.size()) {
			// next sensor
			sensorIndex++;
			if (showProgress) {
				updateProgress(Math.min(sensorIndex, sensors.size()), sensors.size());
			}
			getSensorData(start, end, sensors, sensorIndex, 0, subsampled, showProgress, source);
		} else {
			// completed all pages for all sensors
			onDataComplete(start, end, sensors, source);
		}
	}

	/**
	 * Handles a failed request for the latest data.
	 * 
	 * @param code
	 *            Response code of the failed request.
	 * @param source
	 *            View that requested the data.
	 */
	private void onLatestValueFailure(int code, Object source) {
		// does nothing
	}

	private void onLatestValuesComplete(List<GxtSensor> sensors, Object source) {
		LOG.finest("Latest values complete...");

		JsArray<Timeseries> data = Cache.request(sensors, 0, System.currentTimeMillis());
		NewSensorDataEvent event = new NewSensorDataEvent(sensors, data);
		clientFactory.getEventBus().fireEvent(event);
	}

	@Override
	public void onLatestValuesRequest(LatestValuesRequestEvent event) {

		List<GxtSensor> sensors = event.getSensors();
		for (GxtSensor sensor : sensors) {
			Cache.remove(sensor);
		}
		int index = 0;
		getLatestValues(sensors, index, event.getSource());
	}

	private void onLatestValueSuccess(String response, List<GxtSensor> sensors, int index,
			Object source) {

		GetSensorDataResponse jso = GetSensorDataResponse.create(response);
		Cache.store(sensors.get(index), 0, 0, jso.getData());

		index++;
		getLatestValues(sensors, index, source);
	}

	/**
	 * Shows a dialog to track the progress of a set of data requests.
	 * 
	 * @param tasks
	 *            The total number of data requests that will have to be done.
	 */
	private void showProgress(int tasks) {
		clientFactory.getProgressView().showWindow(tasks);
	}

	/**
	 * Updates the progress dialog.
	 * 
	 * @param progress
	 *            Number of completed tasks.
	 * @param total
	 *            Total number of tasks.
	 * @see #showProgress(int)
	 */
	private void updateProgress(int progress, int total) {
		clientFactory.getProgressView().updateMainProgress(progress, total);
	}
}
