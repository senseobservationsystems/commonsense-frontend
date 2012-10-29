package nl.sense_os.commonsense.main.client.visualization.component.map;

import java.util.logging.Logger;

import nl.sense_os.commonsense.shared.client.model.FloatDataPoint;
import nl.sense_os.commonsense.shared.client.model.Timeseries;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.maps.client.geom.LatLng;

public class LocationDataFilter {

    /**
     * Speed of an airplane
     */
    private static final int REASONABLE_SPEED = 250;
    private static final Logger LOG = Logger.getLogger(LocationDataFilter.class.getName());

    public static LocationData filter(LocationData input) {
        repeatFilterPoints(input);
        return input;
    }

    /**
     * @param latit
     * @param longit
     * @param newLatit
     * @param newLongit
     * @return the distance in meters
     */
    private static double calculateDistance(double latit, double longit, double newLatit,
            double newLongit) {
        LatLng ll1 = LatLng.newInstance(latit, longit);
        LatLng ll2 = LatLng.newInstance(newLatit, newLongit);
        return ll1.distanceFrom(ll2);
    }

    /**
     * repeat the filterPoints function until the point filtering succeeds
     * 
     * @param latPoints
     * @param lonPoints
     * @param timestamps
     * @param r
     */
    private static void repeatFilterPoints(LocationData locationData) {

        int startIndex = 0;
        int maxIndex = locationData.getLatitudes().getData().length()- 1;
        boolean done = false;
        while (!done) {

            if (startIndex <= maxIndex) {

                if (filterPoints(locationData, startIndex)) {
                    LOG.fine("Points filtering succeeded!");
                    done = true;
                    break;
                } else {
                    // try filtering from the next start index
                    startIndex++;
                }
            } else {
                LOG.fine("Reached the end of the array and still didnt find any sensible data");
                break;
            }
        }
    }

    /**
     * Calculates distance and speed between adjacent points of the given ID, and select out only
     * good points
     * 
     * @param locationData
     * @param startIndex
     * @return true if the filter worked
     */
    private static boolean filterPoints(LocationData locationData, int startIndex) {

        LOG.fine("Filter points! Start index: " + startIndex);

        Timeseries latitudes = locationData.getLatitudes();
        Timeseries longitudes = locationData.getLongitudes();
        JsArray<FloatDataPoint> latPoints = latitudes.getData().cast();
        JsArray<FloatDataPoint> lonPoints = longitudes.getData().cast();

        // initialize lat/long and time to the goodStart and store it as a "good" point
        FloatDataPoint goodLatPoint = latPoints.get(startIndex);
        FloatDataPoint goodLonPoint = lonPoints.get(startIndex);

        String emptyTimeseries = "{\"data\":[],\"id\":0,\"label\":\"\",\"start\":0,\"end\":0}";
        Timeseries goodLatitudes = JsonUtils.safeEval(emptyTimeseries);
        goodLatitudes.getData().push(goodLatPoint);
        Timeseries goodLongitudes = JsonUtils.safeEval(emptyTimeseries);
        goodLongitudes.getData().push(goodLonPoint);

        int badPointCount = startIndex;
        for (int p = startIndex + 1; p < latPoints.length(); p++) {

            FloatDataPoint newLatPoint = latPoints.get(p);
            FloatDataPoint newLonPoint = lonPoints.get(p);

            // calculate distance between two points
            double distance = calculateDistance(goodLatPoint.getValue(), goodLonPoint.getValue(),
                    newLatPoint.getValue(), newLonPoint.getValue());

            // calculate speed between the two points
            double timeDifference = (newLatPoint.getTimestamp() - goodLatPoint.getTimestamp()) / 1000;
            double speed = distance / timeDifference;

            if (timeDifference < 0.5 || speed > REASONABLE_SPEED) {
                LOG.finest("Bad point! Index: " + p + ", speed: " + speed + ", time difference: "
                        + timeDifference);
                badPointCount++;

            } else {
                // store lat-long coordinates and timestamp as a "good Point"
                goodLatitudes.getData().push(newLatPoint);
                goodLongitudes.getData().push(newLonPoint);

                // the following points will be checked against this point
                goodLatPoint = newLatPoint;
                goodLonPoint = newLonPoint;
            }
        }

        // succeeded or not? are there more good points than bad points?
        LOG.fine("Filtering complete. " + badPointCount + " bad points eliminated out of "
                + latPoints.length() + " total points");

        boolean filterSucceeded = false;
        if (badPointCount < goodLatitudes.getData().length()) {
            filterSucceeded = true;

            // TODO return result

        } else {
            filterSucceeded = false;
        }

        return filterSucceeded;
    }
}
