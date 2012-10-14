package nl.sense_os.commonsense.main.client.visualization.component.map;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.gwt.maps.client.geom.LatLng;

public class LocationDataFilter {

	private static final double R = 6371;
	private static final int REASONABLE_SPEED = 25000;
	private static final Logger LOG = Logger.getLogger(LocationDataFilter.class.getName());

	public static LocationData filter(LocationData input) {
		return input;
	}

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
	private static void repeatFilterPoints(ArrayList<Float> latPoints, ArrayList<Float> lonPoints,
			ArrayList<Long> timestamps, int r) {

		int goodStart = 0;
		boolean done = false;

		while (done == false) {

			int filter_succeeded = filterPoints(latPoints, lonPoints, timestamps, r, goodStart);

			if (filter_succeeded == 1) {
				LOG.fine("Points filtering succeeded!");
				done = true;
				break;
			}

			else {
				if (goodStart == latPoints.size() - 1) {

					LOG.fine("Reached the end of the array and still didnt find any sensible data");
					break;
				}
				// try filtering from the next point
				else {
					goodStart++;
				}
			}
		}

	}

	/**
	 * calculate distance and speed between adjacent points of the given ID, and select out only
	 * good points
	 * 
	 * @param latLongPoints
	 * @param currentArray
	 * @param r
	 * @param goodStart
	 * @return
	 */
	private static int filterPoints(ArrayList<Float> latPoints, ArrayList<Float> lonPoints,
			ArrayList<Long> timestamps, int r, int goodStart) {

		// create arrayLists to store latitudes, longitudes and timestamps of only "good points" for
		// this ID
		// create an arrayList to store indices of "bad points" that have to be discarded for this
		// ID
		// add the points before the goodStart to the badPoints arrayList

		ArrayList<Float> IdGoodLat = new ArrayList<Float>();
		ArrayList<Float> IdGoodLon = new ArrayList<Float>();
		ArrayList<Long> IdGoodTimestamps = new ArrayList<Long>();
		ArrayList<Integer> IdBadPoints = new ArrayList<Integer>();
		ArrayList<Long> IdTimeDifferences = new ArrayList<Long>();
		ArrayList<Double> IdDistances = new ArrayList<Double>();
		long IdTimeRange = 0;
		double IdDistanceRange = 0;

		for (int s = 0; s < goodStart; s++) {
			IdBadPoints.add(s);
		}

		// initialize lat/long and time to the goodStart and store it as a "good" point
		double latit = latPoints.get(goodStart);
		double longit = lonPoints.get(goodStart);
		long pointTimestamp = timestamps.get(goodStart);

		IdGoodLat.add(new Float(latit));
		IdGoodLon.add(new Float(longit));
		IdGoodTimestamps.add(pointTimestamp);

		// LOG.fine("goodStart is "+ goodStart + " latit is " + latit + " longit is " + longit);

		for (int p = goodStart + 1; p < latPoints.size(); p++) {

			double newLatit = latPoints.get(p);
			double newLongit = lonPoints.get(p);
			long newPointTimestamp = timestamps.get(p);

			// convert values to radians
			double radLongit = Math.toRadians(longit);
			double radLatit = Math.toRadians(latit);
			double radNewLongit = Math.toRadians(newLongit);
			double radNewLatit = Math.toRadians(newLatit);

			// calculate distance in km between two points
			double distance = Math.acos(Math.sin(radLatit) * Math.sin(radNewLatit)
					+ Math.cos(radLatit) * Math.cos(radNewLatit)
					* Math.cos(radNewLongit - radLongit))
					* R;

			// calculate time difference and speed between the two points
			long timeDifference = newPointTimestamp - pointTimestamp;
			double hourDifference = timeDifference * 2.77777778 * 0.0000001;
			double speed = -1;

			if (hourDifference != 0) {
				speed = distance / hourDifference;
			} else {
				LOG.fine("distance equals zero");
			}

			// String pointDate = calculDate(timestamps.get(p));
			// String prevPointDate = calculDate (timestamps.get(p -1));

			if (speed > REASONABLE_SPEED && distance > 25) /* && hourDifference < 2 */{

				Integer newBadPoint = new Integer(p);
				IdBadPoints.add(newBadPoint);

				// LOG.fine ("\nThere is nothing on earth that can move so fast!" +
				// "\ndistance is " + distance + "hourDifference is " + hourDifference + "speed is "
				// + speed + " date is " + pointDate +
				// "\ncomparing timestamp " + pointDate + " to " + prevPointDate +
				// "\npoint " + p + " of " + Id_names.get(r) + " will be discarded" +
				// " its newLatit is " + newLatit + " newLongit is " + newLongit);

			} else {

				// store lat-long coordinates and timestamp as a "good Point"
				IdGoodLat.add(new Float(newLatit));
				IdGoodLon.add(new Float(newLongit));
				IdGoodTimestamps.add(newPointTimestamp);
				IdTimeDifferences.add(timeDifference);
				IdDistances.add(distance);
				// LOG.fine ("The distance is " + distance);
				if (distance > 0) {
					IdDistanceRange = IdDistanceRange + distance;
				}

				// the following points will be checked against this point
				latit = newLatit;
				longit = newLongit;
				pointTimestamp = newPointTimestamp;

			}
		}

		// succeeded or not? are there more good points than bad points?

		int filterSucceeded = -1;
		if (IdGoodLat.size() > IdBadPoints.size()) {

			filterSucceeded = 1;

			// TODO return result

		} else {
			filterSucceeded = 0;
		}

		return filterSucceeded;
	}

	/**
	 * Selects only the points between which the distance is significant compared to the total
	 * length of the trace
	 * 
	 * @param bigListIndex
	 */
	private void filterAnimatedPoints(int bigListIndex) {
		ArrayList<Float> latValues = null;// allGoodLat.get(bigListIndex);
		ArrayList<Float> lonValues = null;// allGoodLon.get(bigListIndex);
		ArrayList<Long> timestamps = null;// allGoodTimestamps.get(bigListIndex);

		ArrayList<LatLng> animatePoints = new ArrayList<LatLng>();
		ArrayList<Double> animatedDistances = new ArrayList<Double>();
		ArrayList<Long> animatedTimestamps = new ArrayList<Long>();
		ArrayList<Long> animatedTimeranges = new ArrayList<Long>();

		double animatedDistanceRange = 0;

		double firstLatit = latValues.get(0);
		double firstLongit = lonValues.get(0);
		long firstTimestamp = timestamps.get(0);

		LatLng point = LatLng.newInstance(firstLatit, firstLongit);
		animatePoints.add(point);
		animatedTimestamps.add(firstTimestamp);

		for (int i = 0; i < latValues.size() - 1; i++) {
			double latit = latValues.get(i);
			double longit = lonValues.get(i);
			long timestamp = timestamps.get(i);

			double newLatit = latValues.get(i + 1);
			double newLongit = lonValues.get(i + 1);
			long newTimestamp = timestamps.get(i + 1);
			// LOG.fine ("Analyzing timestamp " + calculDate(newTimestamp));

			long timeRange = newTimestamp - timestamp;

			double distance = calculateDistance(latit, longit, newLatit, newLongit);

			if (distance > 0) {
				LatLng newPoint = LatLng.newInstance(newLatit, newLongit);
				animatePoints.add(newPoint);
				animatedDistances.add(distance);
				animatedDistanceRange = animatedDistanceRange + distance;
				animatedTimestamps.add(newTimestamp);
				animatedTimeranges.add(timeRange);
			}

		}

		// TODO fix these results:
		// animateLatLngList.add(animatePoints);
		// allAnimatedDistances.add(animatedDistances);
		// allAnimatedTimestamps.add(animatedTimestamps);
		//
		// longerAnimateLatLngList.add(animatePoints);
		// longerListAnimatedDistances.add(animatedDistances);
		// longerListAnimatedTimestamps.add(animatedTimestamps);
		//
		// long lastTimestamp = animatedTimestamps.get(animatedTimestamps.size() - 1);
		// allAnimatedTimeranges.add(animatedTimeranges);
		// animatedDistanceRanges.add(animatedDistanceRange);
		// LOG.fine("Total distance for " + bigListIndex + " is " + animatedDistanceRange
		// + " the animatePoints size for " + bigListIndex + " is " + animatePoints.size());
		//
		// animationPointsAdded = true;
	}
}
