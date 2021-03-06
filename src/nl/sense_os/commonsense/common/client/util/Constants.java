package nl.sense_os.commonsense.common.client.util;

import nl.sense_os.commonsense.common.client.model.Timeseries;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;

public class Constants {

	public static class MapsKeys {

		private static final String MAPS_KEY_BACKEND_RC = "AIzaSyB4khOS82GtGOLzitfpJmskILGIr3ZOW1E";

		/**
		 * Google Maps API key, generated for http://rc.sense-os.nl
		 */
		private static final String MAPS_KEY_RC = "AIzaSyB4khOS82GtGOLzitfpJmskILGIr3ZOW1E";

		/**
		 * Google Maps API key, generated for http://common.sense-os.nl
		 */
		private static final String MAPS_KEY_STABLE = "AIzaSyB4khOS82GtGOLzitfpJmskILGIr3ZOW1E";

		/**
		 * Google Maps API key, generated for http://common.dev.sense-os.nl
		 */
		private static final String MAPS_KEY_DEV = "AIzaSyB4khOS82GtGOLzitfpJmskILGIr3ZOW1E";

		/**
		 * Maps key for "regular" deployments: either stable version or test version.
		 */
		private static final String MAPS_KEY_REGULAR = Constants.RC_MODE ? MAPS_KEY_RC
				: MAPS_KEY_STABLE;

		/**
		 * Google Maps API key.
		 */
		public static final String MAPS_KEY = Constants.DEV_MODE ? MAPS_KEY_DEV
				: Constants.BACKEND_RC_MODE ? MAPS_KEY_BACKEND_RC : MAPS_KEY_REGULAR;

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
	 * Flag for backend release candidate mode. <code>true</code> if the app is deployed to
	 * rc.dev.sense-os.nl.
	 */
	public static final boolean BACKEND_RC_MODE = GWT.getModuleBaseURL().contains(
			"rc.dev.sense-os.nl");

	/**
	 * Flag for dev mode. <code>true</code> if the app is deployed to common.dev.sense-os.nl.
	 */
	public static final boolean DEV_MODE = GWT.getModuleBaseURL()
			.contains("common.dev.sense-os.nl");

	/**
	 * Flag for Release Candidate mode. <code>true</code> if the app is deployed to rc.sense-os.nl.
	 */
	public static final boolean RC_MODE = GWT.getModuleBaseURL().contains("rc.sense-os.nl");

	/**
	 * Flag for local mode. <code>true</code> if the app is deployed to an unknown location.
	 */
	public static final boolean GENERIC_MODE = !STABLE_MODE && !DEV_MODE && !RC_MODE;

	/**
	 * true if shortcut 'hacks' for easy developing are allowed
	 */
	public static final boolean ALLOW_HACKS = !GWT.isProdMode();

	/**
	 * Registry key for the list of all devices for the current user
	 */
	public static final String REG_DEVICE_LIST = "DevicesList";

	/**
	 * Registry key for the list of all environments for the current user
	 */
	public static final String REG_ENVIRONMENT_LIST = "EnvironmentList";

	/**
	 * Registry key for the list of groups for the current user
	 */
	public static final String REG_GROUPS = "Groups";

	/**
	 * Registry key for the list of all sensors for the current user
	 */
	public static final String REG_SENSOR_LIST = "SensorsList";

	/**
	 * Registry key for the list of services for the current user
	 */
	public static final String REG_SERVICES = "Services";

	/**
	 * Registry key for the session ID, stored as String
	 * 
	 * @deprecated Use {@link SessionManager} instead
	 */
	@Deprecated
	public static final String REG_SESSION_ID = "SessionId";

	/**
	 * Registry key for the current User
	 */
	public static final String REG_USER = "User";

	/**
	 * Registry key for the visualization panel
	 */
	public static final String REG_VIZPANEL = "VizPanel";

	private Constants() {
		// Private constructor to make sure this class is not instantiated.
	}
}