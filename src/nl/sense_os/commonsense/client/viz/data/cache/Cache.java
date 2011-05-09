package nl.sense_os.commonsense.client.viz.data.cache;

import java.util.List;

import nl.sense_os.commonsense.client.common.json.overlays.Timeseries;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.SensorModel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;

public class Cache {

    private static CacheJso cache;
    private static final String TAG = "Cache";

    public static void remove(SensorModel sensor) {
        if (cache != null) {
            cache.remove(sensor.getId());
        } else {
            Log.w(TAG, "Cannot remove cached data: cache=null");
        }
    }

    public static void clear() {
        cache = null;
    }

    /**
     * Gets sensor data from the cache, if available.
     * 
     * @param sensors
     *            List of sensors to get the data for.
     * @return Cached data, as JavaScriptObject that can be used directly by Jos' timeline graph.
     */
    public static JsArray<Timeseries> request(List<SensorModel> sensors) {
        if (null != cache) {
            String ids = "[";
            for (SensorModel sensor : sensors) {
                ids += sensor.getId() + ", ";
            }
            if (sensors.size() > 0) {
                ids = ids.substring(0, ids.length() - 2);
            }
            ids += "]";
            return cache.request(JsonUtils.<JsArray<?>> safeEval(ids));
        } else {
            // Log.d(TAG, "No cache object, returning empty array...");
            return JsArray.createArray().cast();
        }
    }

    /**
     * Appends data from CommonSense to the local cache.
     * 
     * @param sensor
     *            SensorModel that this data belongs to.
     * @param data
     *            Raw data response from CommonSense.
     * @return The total number of values in the request.
     */
    public static void store(SensorModel sensor, long start, long end, JsArray<?> data) {
        if (null == cache) {
            // create cache object
            // Log.d(TAG, "Create cache...");
            cache = CacheJso.create();
        }
        cache.store(sensor.getId(), sensor.<String> get("text"), start, end, data);
    }
}
