package nl.sense_os.commonsense.client.data;

import java.util.List;

import nl.sense_os.commonsense.client.json.overlays.Timeseries;
import nl.sense_os.commonsense.shared.SensorModel;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;

public class Cache {

    /**
     * JSO with lists of cached sensor data.
     */
    private static JavaScriptObject cache;

    public static void remove(SensorModel sensor) {
        cache = remove(sensor.getId(), cache);
    }

    private final static native JavaScriptObject remove(String id, JavaScriptObject cache) /*-{
		if (cache == undefined) {
			return null;
		}

		for ( var i = 0; i < cache.content.length; i++) {
			var entry = cache.content[i];
			if (entry.id == id) {
				entry.data = [];
			}
		}
		return cache;
    }-*/;

    /**
     * Gets sensor data from the cache, if available.
     * 
     * @param ids
     *            IDs of the sensors to get the data for.
     * @param cache
     *            Cache with sensor data.
     * @return Cached data, as JavaScriptObject that can be used directly by Jos' timeline graph.
     */
    private static native JsArray<Timeseries> request(JsArray<?> ids, JavaScriptObject cache) /*-{
		if (cache == undefined) {
			return [];
		}

		var data = [];
		for ( var i = 0; i < ids.length; i++) {
			var id = ids[i]
			for ( var j = 0; j < cache.content.length; j++) {
				var entry = cache.content[j];
				if (entry.id == id) {
					data.push(entry);
				}
			}
		}
		return data;
    }-*/;

    /**
     * Gets sensor data from the cache, if available.
     * 
     * @param sensors
     *            List of sensors to get the data for.
     * @return Cached data, as JavaScriptObject that can be used directly by Jos' timeline graph.
     */
    public static JsArray<Timeseries> request(List<SensorModel> sensors) {
        String ids = "[";
        for (SensorModel sensor : sensors) {
            ids += sensor.getId() + ", ";
        }
        if (sensors.size() > 0) {
            ids = ids.substring(0, ids.length() - 2);
        }
        ids += "]";
        return request(JsonUtils.<JsArray<?>> safeEval(ids), cache);
    }

    /**
     * Appends data from CommonSense to the local cache.
     * 
     * @param sensor
     *            SensorModel that this data belongs to.
     * @param csData
     *            Raw data response from CommonSense.
     * @return The total number of values in the request.
     */
    public static void store(SensorModel sensor, JsArray<?> data) {
        cache = store(sensor.getId(), sensor.<String> get("text"), data, cache);
    }

    /**
     * Stores
     * 
     * @param id
     * @param sensor
     * @param values
     * @param cache
     * @return
     */
    private static native JavaScriptObject store(String id, String sensor, JsArray<?> values,
            JavaScriptObject cache) /*-{

		function appendValue(id, label, newValue) {
			var key = id + '. ' + label;

			// find earlier data (add if needed)
			var index = cache.mapping[key];
			if (index == undefined) {
				// create new entry
				var newEntry = {
					'id' : id,
					'label' : label,
					'type' : typeof (newValue.value),
					'data' : [ newValue ]
				};

				// add new index
				index = cache.content.push(newEntry) - 1;
				cache.mapping[key] = index;

			} else {
				// push onto earlier entry
				cache.content[index].data.push(newValue);
			}
		}

		if (cache == undefined) {
			cache = {
				'mapping' : {},
				'content' : []
			};
		}

		for ( var i = 0, len = values.length; i < len; i++) {
			var obj = values[i];
			var date = obj.date;
			var value = obj.value;

			if (!isNaN(value)) {
				// The value contains a number

				// prepare new value for the 'values' array
				var newValue = {
					'date' : Math.round(parseFloat(date) * 1000),
					'value' : parseFloat(value)
				};

				appendValue(id, sensor, newValue);

			} else if (typeof (value) == 'string') {
				// The value contains a string

				// prepare new value for the 'values' array
				var newValue = {
					'date' : Math.round(parseFloat(date) * 1000),
					'value' : value
				};

				appendValue(id, sensor, newValue);

			} else {
				// the value can contain multiple properties

				for (prop in value) {
					// prepare new value for the 'values' array
					var newValue = {
						'date' : Math.round(parseFloat(date) * 1000),
						'value' : value[prop]
					};

					appendValue(id, sensor + ' ' + prop, newValue);
				}
			}
		}

		return cache;
    }-*/;
}
