package nl.sense_os.commonsense.main.client.visualization.data;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.communication.SessionManager;
import nl.sense_os.commonsense.common.client.communication.httpresponse.GetSensorDataResponse;
import nl.sense_os.commonsense.common.client.constant.Urls;
import nl.sense_os.commonsense.common.client.model.BackEndDataPoint;
import nl.sense_os.commonsense.common.client.model.Timeseries;
import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.event.DataRequestEvent;
import nl.sense_os.commonsense.main.client.event.LatestValuesRequestEvent;
import nl.sense_os.commonsense.main.client.event.NewSensorDataEvent;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.visualization.data.cache.Cache;
import nl.sense_os.commonsense.main.client.visualization.data.component.GxtProgressDialog;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.NumberFormat;

public class DataHandler implements DataRequestEvent.Handler, LatestValuesRequestEvent.Handler {

	private static final Logger LOG = Logger.getLogger(DataHandler.class.getName());
	private ProgressView progressView;
	private MainClientFactory clientFactory;
	private static final int PER_PAGE = 1000; // max: 1000

	public DataHandler(MainClientFactory clientFactory) {
		this.clientFactory = clientFactory;

		// TODO change into regular view
		progressView = new GxtProgressDialog();
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

			final Method method = RequestBuilder.GET;
			final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
			urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensor.getId() + "/data.json");
			urlBuilder.setParameter("last", "1");
			if (-1 != sensor.getAlias()) {
				urlBuilder.setParameter("alias", "" + sensor.getAlias());
			}
			final String url = urlBuilder.buildString();
			final String sessionId = SessionManager.getSessionId();

			// prepare request callback
			RequestCallback reqCallback = new RequestCallback() {

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
			try {
				RequestBuilder builder = new RequestBuilder(method, url);
				builder.setHeader("X-SESSION_ID", sessionId);
				builder.sendRequest(null, reqCallback);
			} catch (Exception e) {
				LOG.warning("GET slast data request threw exception: " + e.getMessage());
				reqCallback.onError(null, e);
			}

		} else {
			// hoooray we're done!
			onLatestValuesComplete(sensors, source);
		}
	}

	/**
	 * Hides the progress bar View.
	 */
	private void hideProgress() {
		progressView.hideWindow();
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

		reqDataSubsampled(start, end, sensors, sensorIndex, pageIndex, subsample, showProgress,
				event.getSource());
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
	 * Handles successful requests for paged data. Parses the response, stores it and moves on to
	 * the next page, or the next sensor in the list.
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
	 *            Index of the current page of data for the current sensor.
	 * @param total
	 *            Total amount of data that should be retrieved for the current sensor. This count
	 *            is returned along with the first page of data.
	 * @param source
	 *            View that requested the data.
	 * @param showProgress
	 *            Boolean to indicate whether to update the user of the progress.
	 */
	private void onReqRawSuccess(String response, long start, long end, List<GxtSensor> sensors,
			int sensorIndex, int pageIndex, int total, Object source, boolean showProgress) {

		// parse the incoming data
		GetSensorDataResponse jsoResponse = GetSensorDataResponse.create(response);

		// store data in cache
		GxtSensor sensor = sensors.get(sensorIndex);
		JsArray<BackEndDataPoint> data = jsoResponse.getData();
		Cache.store(sensor, start, end, data);

		// the first page also contains a total count, otherwise reuse the total from earlier pages
		if (pageIndex == 0) {
			total = jsoResponse.getTotal();
		}

		// check if we need to fetch additional pages
		if (pageIndex * PER_PAGE + data.length() < total && data.length() > 0) {
			reqDataRaw(start, end, sensors, sensorIndex, pageIndex + 1, total, source, showProgress);
		} else {
			updateProgress(Math.min(sensorIndex + 1, sensors.size()), sensors.size());
			reqDataRaw(start, end, sensors, sensorIndex + 1, 0, 0, source, showProgress);
		}
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
	private void onReqSubsampledSuccess(String response, long start, long end,
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
			reqDataSubsampled(start, end, sensors, sensorIndex, pageIndex, subsampled,
					showProgress, source);
		} else if (sensorIndex < sensors.size()) {
			// next sensor
			sensorIndex++;
			if (showProgress) {
				updateProgress(Math.min(sensorIndex, sensors.size()), sensors.size());
			}
			reqDataSubsampled(start, end, sensors, sensorIndex, 0, subsampled, showProgress, source);
		} else {
			// completed all pages for all sensors
			onDataComplete(start, end, sensors, source);
		}
	}

	/**
	 * Requests data from a list of sensors, between a given start and end date. CommonSense will
	 * page the data to ensure that we get all data.
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
	 *            Index of the current page.
	 * @param sensorTotal
	 *            Total amount of data points to retrieve for the current sensor. This number is
	 *            supplied by CommonSense with the first page of data.
	 * @param vizPanel
	 *            Panel that requested the data.
	 * @param showProgress
	 *            Set to true to display a progress dialog.
	 */
	private void reqDataRaw(final long start, final long end, final List<GxtSensor> sensors,
			final int sensorIndex, final int pageIndex, final int sensorTotal, final Object source,
			final boolean showProgress) {
		LOG.fine("Request paged data...");

		if (sensorIndex < sensors.size()) {

			final GxtSensor sensor = sensors.get(sensorIndex);

			// remove preexisting data from the cache, because reusing it is too complicated
			if (pageIndex == 0) {
				Cache.remove(sensor);
			}

			final Method method = RequestBuilder.GET;
			final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
			urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensor.getId() + "/data.json");

			// paging parameters
			urlBuilder.setParameter("per_page", "" + PER_PAGE);
			urlBuilder.setParameter("page", "" + pageIndex);

			// only need a total count for the first page request
			if (0 == pageIndex) {
				urlBuilder.setParameter("total", "1");
			}

			// start date parameter
			final String startDate = NumberFormat.getFormat("#.000").format(start / 1000d);
			urlBuilder.setParameter("start_date", startDate);

			// end date is optional
			if (end != -1) {
				final String endDate = NumberFormat.getFormat("#.000").format(end / 1000d);
				urlBuilder.setParameter("end_date", endDate);
			}

			// prepare request callback
			RequestCallback reqCallback = new RequestCallback() {

				@Override
				public void onError(Request request, Throwable exception) {
					LOG.warning("GET data (paged) onError callback: " + exception.getMessage());
					onDataFailed(0, showProgress);
				}

				@Override
				public void onResponseReceived(Request request, Response response) {
					LOG.finest("GET data (paged) response received: " + response.getStatusText());
					int statusCode = response.getStatusCode();
					if (Response.SC_OK == statusCode) {
						onReqRawSuccess(response.getText(), start, end, sensors, sensorIndex,
								pageIndex, sensorTotal, source, showProgress);
					} else {
						LOG.warning("GET data (paged) returned incorrect status: " + statusCode);
						onDataFailed(statusCode, showProgress);
					}
				}
			};

			// send request
			try {
				final String sessionId = SessionManager.getSessionId();
				RequestBuilder builder = new RequestBuilder(method, urlBuilder.buildString());
				builder.setHeader("X-SESSION_ID", sessionId);
				builder.sendRequest(null, reqCallback);
			} catch (Exception e) {
				LOG.warning("GET data (paged) request threw exception: " + e.getMessage());
				reqCallback.onError(null, e);
			}

		} else {
			// should not happen, but just in case...
			onDataComplete(start, end, sensors, source);
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
	private void reqDataSubsampled(final long start, final long end, final List<GxtSensor> sensors,
			final int sensorIndex, final int pageIndex, final boolean subsample,
			final boolean showProgress, final Object source) {
		LOG.fine("Request data...");

		if (sensorIndex < sensors.size()) {

			final GxtSensor sensor = sensors.get(sensorIndex);

			// remove preexisting data from the cache, because reusing it is too complicated
			if (pageIndex == 0) {
				Cache.remove(sensor);
			}

			final Method method = RequestBuilder.GET;
			final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
			urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensor.getId() + "/data.json");
			urlBuilder.setParameter("per_page", "" + PER_PAGE);
			urlBuilder.setParameter("page", "" + pageIndex);

			// start date parameter
			final String startDate = NumberFormat.getFormat("#.000").format(start / 1000d);
			urlBuilder.setParameter("start_date", startDate);

			// end date is optional
			if (end != -1) {
				final String endDate = NumberFormat.getFormat("#.000").format(end / 1000d);
				urlBuilder.setParameter("end_date", endDate);
			}

			// set subsample interval
			if (subsample) {
				urlBuilder.setParameter("interval", "" + calcInterval(start, end));
			}

			// use alias if necessary
			if (-1 != sensor.getAlias()) {
				urlBuilder.setParameter("alias", "" + sensor.getAlias());
			}

			final String sessionId = SessionManager.getSessionId();

			// prepare request callback
			RequestCallback reqCallback = new RequestCallback() {

				@Override
				public void onError(Request request, Throwable exception) {
					LOG.warning("GET subsampled data onError callback: " + exception.getMessage());
					onDataFailed(0, showProgress);
				}

				@Override
				public void onResponseReceived(Request request, Response response) {
					LOG.finest("GET data (subsampled) response received: "
							+ response.getStatusText());
					int statusCode = response.getStatusCode();
					if (Response.SC_OK == statusCode) {
						onReqSubsampledSuccess(response.getText(), start, end, sensors,
								sensorIndex, pageIndex, subsample, showProgress, source);
					} else {
						LOG.warning("GET data (subsampled) returned incorrect status: "
								+ statusCode);
						onDataFailed(statusCode, showProgress);
					}
				}
			};

			// send request
			try {
				RequestBuilder builder = new RequestBuilder(method, urlBuilder.buildString());
				builder.setHeader("X-SESSION_ID", sessionId);
				builder.sendRequest(null, reqCallback);
			} catch (Exception e) {
				LOG.warning("GET data (subsampled) request threw exception: " + e.getMessage());
				reqCallback.onError(null, e);
			}

		} else {
			// should not happen, but just in case...
			onDataComplete(start, end, sensors, source);
		}
	}

	/**
	 * Shows a dialog to track the progress of a set of data requests.
	 * 
	 * @param tasks
	 *            The total number of data requests that will have to be done.
	 */
	private void showProgress(int tasks) {
		progressView.showWindow(tasks);
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
		progressView.updateMainProgress(progress, total);
	}
}
