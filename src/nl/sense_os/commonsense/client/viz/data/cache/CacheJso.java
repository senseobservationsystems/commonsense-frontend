package nl.sense_os.commonsense.client.viz.data.cache;

import nl.sense_os.commonsense.client.viz.data.timeseries.BackEndDataPoint;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsonUtils;

/**
 * JSO with lists of cached sensor data. Exposes Java interface to {@link Cache} class. Other
 * classes should use that class to access the cache.
 */
final class CacheJso extends JavaScriptObject {

    protected CacheJso() {
        // empty protected constructor
    }

    /**
     * Creates a JSNI cache object.
     * 
     * @return The newly created cache.
     */
    protected static CacheJso create() {
        String source = "{ \"mapping\" : {}, \"content\" : [] }";
        return JsonUtils.<CacheJso> safeEval(source);
    };

    /**
     * Removes the data for a given sensor ID. If the sensor has multiple time series associated
     * with it (e.g. a JSON sensor), all of them are removed.
     * 
     * @param id
     *            ID of the sensor to remove the data for.
     */
    protected native void remove(int id) /*-{
        for ( var i = 0; i < this.content.length; i++) {
            var timeseries = this.content[i];
            if (timeseries.id == id) {
                timeseries.data = [];
            }
        }
    }-*/;

    /**
     * Gets sensor data from the cache, if available.
     * 
     * @param ids
     *            IDs of the sensors to get the data for.
     * @param start
     *            Start time of period to get data from, in milliseconds. Passed as a double because
     *            JavaScript does not have long type.
     * @param end
     *            End time of period to get data from, in milliseconds. Passed as a double because
     *            JavaScript does not have long type.
     */
    protected native JsArray<Timeseries> request(JsArrayInteger ids, double start, double end) /*-{
        var result = [];

        // for each sensor in the request
        for ( var i = 0; i < ids.length; i++) {
            var id = ids[i]

            // for each sensor in the cache
            for ( var j = 0; j < this.content.length; j++) {

                // check if this sensor has a requested ID
                var timeseries = this.content[j];
                if (timeseries.id == id) {

                    // prepare object to put selected data points in
                    var selection = {
                        'id' : id,
                        'label' : timeseries.label,
                        'start' : Infinity,
                        'end' : -Infinity,
                        'type' : timeseries.type,
                        'data' : []
                    };

                    // select the right data points from the time series
                    for ( var k = 0; k < timeseries.data.length; k++) {
                        var dataPoint = timeseries.data[k];
                        if (dataPoint.date >= start && (end == -1 || dataPoint.date <= end)) {
                            selection.data.push(dataPoint);

                            // update start / end time of the selection time series
                            if (dataPoint.date < selection.start) {
                                // console.log('new selection start time! '
                                // + dataPoint.date);
                                selection.start = dataPoint.date;
                            }
                            if (dataPoint.date > selection.end) {
                                // console.log('new selection end time! '
                                // + dataPoint.date);
                                selection.end = dataPoint.date;
                            }
                        } else {
                            // console.log('data point: ' + dataPoint.date
                            // + ', request start: ' + start
                            // + ', request end: ' + end);
                        }
                    }

                    if (selection.data.length > 0) {
                        result.push(selection);
                    }
                }
            }
        }
        return result;
    }-*/;

    /**
     * Stores sensor values in the cache.
     * 
     * @param id
     *            ID of the sensor to store values for.
     * @param label
     *            Label of the sensor.
     * @param start
     *            Start time of requested data period, in milliseconds. Passed as a double because
     *            JavaScript does not have long type.
     * @param end
     *            End time of requested data period, in milliseconds. Passed as a double because
     *            JavaScript does not have long type.
     * @param values
     *            JsArray with sensor value objects.
     */
    protected native void store(int id, String label, double start, double end,
            JsArray<BackEndDataPoint> values) /*-{

        // check all values in the array 
        for ( var i = 0, len = values.length; i < len; i++) {
            var backEndDataPoint = values[i];
            var rowid = backEndDataPoint.id;
            var date = Math.round(parseFloat(backEndDataPoint.date) * 1000);
            var value = backEndDataPoint.value;

            if (value.length > 0 && !isNaN(value)) {
                // The value contains a number
                var datapoint = {
                    'id' : rowid,
                    'date' : date,
                    'value' : parseFloat(value)
                };
                appendValue(this, id, label, start, end, datapoint);

            } else if (typeof (value) == 'string') {
                // The value contains a string
                var datapoint = {
                    'id' : rowid,
                    'date' : date,
                    'value' : value
                };
                appendValue(this, id, label, start, end, datapoint);

            } else {
                // the value can contain multiple properties
                for (prop in value) {
                    // prepare new value for the 'values' array
                    var propValue = value[prop];
                    if (propValue.length > 0 && !isNaN(propValue)) {
                        propValue = parseFloat(propValue);
                    }
                    var datapoint = {
                        'id' : rowid,
                        'date' : date,
                        'value' : propValue
                    };
                    appendValue(this, id, label + ' ' + prop, start, end, datapoint);
                }
            }
        }

        // function to add a value to the cache
        function appendValue(cache, id, label, start, end, datapoint) {

            var key = id + '. ' + label;

            // find earlier data (add if needed)
            var index = cache.mapping[key];
            if (index == undefined || cache.content[index].data.length == 0) {

                // create new entry
                var newTimeseries = {
                    'id' : id,
                    'label' : label,
                    'start' : start,
                    'end' : datapoint.date,
                    'type' : typeof (datapoint.value),
                    'data' : [ datapoint ]
                };

                // push timeseries in array and add new index to the mapping
                index = cache.content.push(newTimeseries) - 1;
                cache.mapping[key] = index;

            } else {
                // push onto earlier timeseries

                // check for duplicate timestamps
                oldPoint = cache.content[index].data.pop();
                if (undefined == oldPoint) {
                    // when does this happen??
                    console.log('last point is undefined? timeseries length: '
                            + cache.content[index].data.length);
                    // try to keep on
                    cache.content[index].data.push(datapoint);
                } else if (oldPoint.date == datapoint.date) {
                    // identical timestamps! check the IDs to keep the newest one
                    if (datapoint.id <= oldPoint.id) {
                        cache.content[index].data.push(oldPoint);
                    } else {
                        cache.content[index].data.push(datapoint);
                    }
                } else {
                    // just a regular new data point, also re-add the old point
                    cache.content[index].data.push(oldPoint, datapoint);
                }

                // update end time
                if (cache.content[index].end < datapoint.date) {
                    cache.content[index].end = datapoint.date;
                }
            }
        }
    }-*/;
}
