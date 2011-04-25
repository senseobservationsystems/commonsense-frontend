package nl.sense_os.commonsense.client.data.cache;

import nl.sense_os.commonsense.client.json.overlays.Timeseries;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;

/**
 * JSO with lists of cached sensor data. Exposes Java interface to {@link Cache} class. Other
 * classes should use that class to access the cache.
 */
final class CacheJso extends JavaScriptObject {

    protected CacheJso() {
        // empty protected constructor
    }

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
    protected native void remove(String id) /*-{
		for ( var i = 0; i < this.content.length; i++) {
			var entry = this.content[i];
			if (entry.id == id) {
				entry.data = [];
			}
		}
    }-*/;

    /**
     * Gets sensor data from the cache, if available.
     * 
     * @param ids
     *            IDs of the sensors to get the data for.
     */
    protected native JsArray<Timeseries> request(JsArray<?> ids) /*-{
		var data = [];
		for ( var i = 0; i < ids.length; i++) {
			var id = ids[i]
			for ( var j = 0; j < this.content.length; j++) {
				var entry = this.content[j];
				if (entry.id == id) {
					data.push(entry);
				}
			}
		}
		return data;
    }-*/;

    /**
     * Stores sensor values in the cache.
     * 
     * @param id
     *            ID of the sensor to store values for.
     * @param label
     *            Label of the sensor.
     * @param start
     * @param end
     * @param values
     *            JsArray with sensor value objects.
     */
    protected native void store(String id, String label, double start, double end, JsArray<?> values) /*-{

		// function to add a value to the cache
		function appendValue(cache, id, label, start, end, datapoint) {

			var key = id + '. ' + label;

			// find earlier data (add if needed)
			var index = cache.mapping[key];
			if (index == undefined) {
				// create new entry
				var newEntry = {
					'id' : id,
					'label' : label,
					'start' : start,
					'end' : datapoint.date,
					'type' : typeof (datapoint.value),
					'data' : [ datapoint ]
				};

				// add new index
				index = cache.content.push(newEntry) - 1;
				cache.mapping[key] = index;

			} else {
				// push onto earlier entry
				cache.content[index].data.push(datapoint);
				if (cache.content[index].end < datapoint.date) {
					cache.content[index].end = datapoint.date;
				}
			}
		}

		// check all values in the array 
		for ( var i = 0, len = values.length; i < len; i++) {
			var obj = values[i];
			var date = obj.date;
			var value = obj.value;

			if (!isNaN(value)) {
				// The value contains a number
				var datapoint = {
					'date' : Math.round(parseFloat(date) * 1000),
					'value' : parseFloat(value)
				};
				appendValue(this, id, label, start, end, datapoint);

			} else if (typeof (value) == 'string') {
				// The value contains a string
				var datapoint = {
					'date' : Math.round(parseFloat(date) * 1000),
					'value' : value
				};
				appendValue(this, id, label, start, end, datapoint);

			} else {
				// the value can contain multiple properties
				for (prop in value) {
					// prepare new value for the 'values' array
					var propValue = value[prop];
					if (!isNaN(propValue)) {
						propValue = parseFloat(propValue);
					}
					var datapoint = {
						'date' : Math.round(parseFloat(date) * 1000),
						'value' : propValue
					};
					appendValue(this, id, label + ' ' + prop, start, end,
							datapoint);
				}
			}
		}
    }-*/;
}
