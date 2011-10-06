package nl.sense_os.commonsense.client.common.utility;

import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;

import com.google.gwt.core.client.JsArray;

public class TestData {

    private TestData() {
        // private constructor to make sure this class is not instantiated
    }

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
}
