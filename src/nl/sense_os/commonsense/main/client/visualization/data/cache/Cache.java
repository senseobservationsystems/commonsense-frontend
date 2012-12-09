package nl.sense_os.commonsense.main.client.visualization.data.cache;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.lib.client.model.apiclass.DataPoint;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.shared.client.model.Timeseries;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;

public class Cache {

    private static final Logger LOG = Logger.getLogger(Cache.class.getName());
    private static CacheJso cache;

    protected Cache() {
        // empty private constructor to prevent instantiation
    }

    public static void remove(GxtSensor sensor) {
        if (cache != null) {
            cache.remove(sensor.getId());
        } else {
            LOG.fine("Cannot remove cached data: cache=null");
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
     * @param start
     *            Start time of period to get data from.
     * @param end
     *            End time of period to get data from.
     * @return Cached data, as array of Timeseries that can be used directly by Jos' timeline graph.
     */
    public static JsArray<Timeseries> request(List<GxtSensor> sensors, long start, long end) {

        if (null != cache) {

            // convert list of sensors into JSONArray of IDs
            String ids = "[";
            for (GxtSensor sensor : sensors) {
                ids += "'" + sensor.getId() + "', ";
            }
            if (sensors.size() > 0) {
                ids = ids.substring(0, ids.length() - 2);
            }
            ids += "]";

            // get data from cache
            JsArray<Timeseries> result = cache.request(JsonUtils.<JsArrayString> unsafeEval(ids),
                    start, end);
            LOG.fine("Retrieved " + result.length() + " timeseries from the cache.");
            for (int i = 0; i < result.length(); i++) {
                LOG.fine(result.get(i).getLabel() + ": " + result.get(i).getData().length()
                        + " points");
            }
            return result;

        } else {
            LOG.fine("No cache object, returning empty array...");
            return JsArray.createArray().cast();

        }
    }

    /**
     * Appends data from CommonSense to the local cache.
     * 
     * @param sensor
     *            SensorModel that this data belongs to.
     * @param start
     *            Requested start time of the data.
     * @param end
     *            Requested end time of the data.
     * @param data
     *            Raw data response from CommonSense.
     * @return The total number of values in the request.
     */
    public static void store(GxtSensor sensor, long start, long end,
 JsArray<DataPoint> data) {
        // LOG.setLevel(Level.ALL);
        LOG.fine("Caching " + data.length() + " data points for " + sensor.getDisplayName());

        if (null == cache) {
            // create cache object
            LOG.fine("Create cache...");
            cache = CacheJso.create();
        }
        cache.store(sensor.getId(), sensor.getDisplayName(), start, end, data);
    }
}
