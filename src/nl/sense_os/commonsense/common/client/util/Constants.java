package nl.sense_os.commonsense.common.client.util;

import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;

public class Constants {

	public static class MapsKeys {

		/**
		 * Google Maps API key, generated for http://rc.sense-os.nl
		 */
		private static final String MAPS_KEY_RC = "ABQIAAAAcc8ibe_QaK2XBw4Vp-cVyBQsjUjcByiCivvn1ppG43l0EBYmuxQJjoQuotfoMFzKYEK6QgwlJRD2Pg";

		/**
		 * Google Maps API key, generated for http://common.sense-os.nl
		 */
		private static final String MAPS_KEY_STABLE = "ABQIAAAAcc8ibe_QaK2XBw4Vp-cVyBQRZw5PnBZuDX77DtiPDAIQJTIImRRXR2NlGlzF15dD3pzgYJu67vgxTw";

		/**
		 * Google Maps API key, generated for http://common.dev.sense-os.nl
		 */
		private static final String MAPS_KEY_DEV = "ABQIAAAAcc8ibe_QaK2XBw4Vp-cVyBSkBEmSOMRgjngroDitmgRTGdBMeRTbwc1k-RzAZgpJJ7UzaCSpp5AFyQ";

		/**
		 * Google Maps API key, generated for http://apigee.common.sense-os.nl
		 */
		private static final String MAPS_KEY_APIGEE = "ABQIAAAAcc8ibe_QaK2XBw4Vp-cVyBTvcdSqkWlRFnarZeagA5hkQdjGexTqYU3q3N3rdDah3xpifwQSeolw8w";

		/**
		 * Maps key for "regular" deployments: either stable version or test version.
		 */
		private static final String MAPS_KEY_REGULAR = Constants.RC_MODE ? MAPS_KEY_RC
				: MAPS_KEY_STABLE;
		private static final String MAPS_KEY_INTERMED = Constants.APIGEE_MODE ? MAPS_KEY_APIGEE
				: MAPS_KEY_REGULAR;

		/**
		 * Google Maps API key.
		 */
		public static final String MAPS_KEY = Constants.DEV_MODE ? MAPS_KEY_DEV : MAPS_KEY_INTERMED;

		private MapsKeys() {
			// empty private constructor to prevent instantiation
		}
	}

	public static class TestData {

		public static final String testDataJson = "{\"data\":["
				+ "{\"id\":\"20243680\",\"sensor_id\":\"89\",\"value\":\"{\\\"x-axis\\\":-0.14982382953166962,\\\"y-axis\\\":0.46309182047843933,\\\"z-axis\\\":9.847511291503906}\",\"date\":\"1302858147.5\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
				+ "{\"id\":\"20245598\",\"sensor_id\":\"89\",\"value\":\"{\\\"x-axis\\\":-0.108962781727314,\\\"y-axis\\\":0.5039528608322144,\\\"z-axis\\\":9.806650161743164}\",\"date\":\"1302859047.68\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
				+ "{\"id\":\"20246442\",\"sensor_id\":\"89\",\"value\":\"{\\\"x-axis\\\":-0.19068486988544464,\\\"y-axis\\\":0.46309182047843933,\\\"z-axis\\\":9.765789031982422}\",\"date\":\"1302859498.59\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
				+ "{\"id\":\"20247605\",\"sensor_id\":\"89\",\"value\":\"{\\\"x-axis\\\":0,\\\"y-axis\\\":0.46309182047843933,\\\"z-axis\\\":9.806650161743164}\",\"date\":\"1302860007.35\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
				+ "{\"id\":\"20249252\",\"sensor_id\":\"89\",\"value\":\"{\\\"x-axis\\\":0,\\\"y-axis\\\":0.5039528608322144,\\\"z-axis\\\":9.806650161743164}\",\"date\":\"1302860907.38\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"}"
				+ "],\"total\":5}";

		public static final String testDataString = "{\"data\":["
				+ "{\"id\":\"20243680\",\"sensor_id\":\"89\",\"value\":\"hoi1\",\"date\":\"1302858147.5\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
				+ "{\"id\":\"20245598\",\"sensor_id\":\"89\",\"value\":\"hoi2\",\"date\":\"1302859047.68\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
				+ "{\"id\":\"20246442\",\"sensor_id\":\"89\",\"value\":\"hoi3\",\"date\":\"1302859498.59\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
				+ "{\"id\":\"20247605\",\"sensor_id\":\"89\",\"value\":\"hoi4\",\"date\":\"1302860007.35\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
				+ "{\"id\":\"20249252\",\"sensor_id\":\"89\",\"value\":\"hoi5\",\"date\":\"1302860907.38\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"}"
				+ "],\"total\":5}";

		public static final String testDataFloat = "{\"data\":["
				+ "{\"id\":\"20243680\",\"sensor_id\":\"89\",\"value\":\"1\",\"date\":\"1302858147.5\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
				+ "{\"id\":\"20245598\",\"sensor_id\":\"89\",\"value\":\"2\",\"date\":\"1302859047.68\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
				+ "{\"id\":\"20246442\",\"sensor_id\":\"89\",\"value\":\"3\",\"date\":\"1302859498.59\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
				+ "{\"id\":\"20247605\",\"sensor_id\":\"89\",\"value\":\"4\",\"date\":\"1302860007.35\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"},"
				+ "{\"id\":\"20249252\",\"sensor_id\":\"89\",\"value\":\"5\",\"date\":\"1302860907.38\",\"week\":\"15\",\"month\":\"4\",\"year\":\"2011\"}"
				+ "],\"total\":5}";

		public static final native JsArray<Timeseries> getTimeseriesPosition(int maxPoints) /*-{
			var start = 1304208000000; // 01/05/2011
			var lat = {
				'id' : 1,
				'label' : 'latitude',
				'end' : 0,
				'start' : start,
				'type' : 'number',
				'data' : []
			};
			var lng = {
				'id' : 1,
				'label' : 'longitude',
				'end' : 0,
				'start' : start,
				'type' : 'number',
				'data' : []
			};
			for ( var i = 0; i < maxPoints; i++) {
				var date = start + (i * 3600000);
				var latValue = 45 * Math.sin(2 * Math.PI * i / maxPoints);
				var lngValue = 180 * i / maxPoints;

				lat.data.push({
					'date' : date,
					'value' : latValue

				});
				lat.end = date;

				lng.data.push({
					'date' : date,
					'value' : lngValue

				});
				lng.end = date;
			}
			return [ lat, lng ];
		}-*/;

		public static final native JsArray<Timeseries> getTimeseriesPosition1(int maxPoints) /*-{
			var start = 1304208000000; // 01/05/2011
			var lat = {
				'id' : 2,
				'label' : 'latitude',
				'end' : 0,
				'start' : start,
				'type' : 'number',
				'data' : []
			};
			var lng = {
				'id' : 2,
				'label' : 'longitude',
				'end' : 0,
				'start' : start,
				'type' : 'number',
				'data' : []
			};
			for ( var i = 0; i < maxPoints; i++) {
				var date = start + (i * 3600000);
				var latValue = 45 * Math.sin(2 * Math.PI * i / maxPoints) - 20;
				var lngValue = 180 * i / maxPoints;

				lat.data.push({
					'date' : date,
					'value' : latValue
				});
				lat.end = date;

				lng.data.push({
					'date' : date,
					'value' : lngValue
				});
				lng.end = date;
			}
			return [ lat, lng ];
		}-*/;

		public static final native JsArray<Timeseries> getTimeseriesPosition2(int maxPoints) /*-{
			var start = 1304208000000; // 01/05/2011
			var lat = {
				'id' : 3,
				'label' : 'latitude',
				'end' : 0,
				'start' : start,
				'type' : 'number',
				'data' : []
			};
			var lng = {
				'id' : 3,
				'label' : 'longitude',
				'end' : 0,
				'start' : start,
				'type' : 'number',
				'data' : []
			};
			for ( var i = 0; i < maxPoints; i++) {
				var date = start + (i * 3600000);
				var latValue = 45 * Math.sin(2 * Math.PI * i / maxPoints) - 40;
				var lngValue = 180 * i / maxPoints;

				lat.data.push({
					'date' : date,
					'value' : latValue
				});
				lat.end = date;

				lng.data.push({
					'date' : date,
					'value' : lngValue
				});
				lng.end = date;
			}
			return [ lat, lng ];
		}-*/;

		private TestData() {
			// private constructor to make sure this class is not instantiated
		}
	}

	/**
	 * Flag for Stable mode. <code>true</code> if the app is deployed to common.sense-os.nl.
	 */
	public static final boolean STABLE_MODE = GWT.getModuleBaseURL().contains("common.sense-os.nl");

	/**
	 * Flag for 'ted' mode. <code>true</code> if the app is deployed to
	 * commonsense-test.appspot.com.
	 */
	public static final boolean TED_MODE = GWT.getModuleBaseURL().contains(
			"commonsense-test.appspot.com");

	/**
	 * Flag for dev mode. <code>true</code> if the app is deployed to common.dev.sense-os.nl.
	 */
	public static final boolean DEV_MODE = !GWT.isProdMode()
			|| GWT.getModuleBaseURL().contains("common.dev.sense-os.nl");

	/**
	 * Flag for Apigee test mode. <code>true</code> if the app is deployed to
	 * apigee.common.sense-os.nl.
	 */
	public static final boolean APIGEE_MODE = GWT.getModuleBaseURL().contains(
			"apigee.common.sense-os.nl");

	/**
	 * Flag for Release Candidate mode. <code>true</code> if the app is deployed to rc.sense-os.nl.
	 */
	public static final boolean RC_MODE = GWT.getModuleBaseURL().contains("rc.sense-os.nl");

	/**
	 * Flag for local mode. <code>true</code> if the app is deployed to an unknown location.
	 */
	public static final boolean GENERIC_MODE = !STABLE_MODE && !TED_MODE && !DEV_MODE
			&& !APIGEE_MODE && !RC_MODE;

	/**
	 * true if shortcut 'hacks' for easy developing are allowed
	 */
	public static final boolean ALLOW_HACKS = !GWT.isProdMode();

	private Constants() {
		// Private constructor to make sure this class is not instantiated.
	}
}