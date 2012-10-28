package nl.sense_os.commonsense.main.client.visualization;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class VisualizePlace extends Place {

	/**
	 * PlaceTokenizer knows how to serialize the Place's state to a URL token.
	 */
	@Prefix("visualize")
	public static class Tokenizer implements PlaceTokenizer<VisualizePlace> {

		private static final Logger LOG = Logger.getLogger(Tokenizer.class.getName());

		@Override
		public VisualizePlace getPlace(String token) {
			List<GxtSensor> sensors = new ArrayList<GxtSensor>();
			int type = -1;
			long start = -1;
			long end = -1;
			boolean subsample = false;

			// parse sensors
			try {
				int sensorsBegin = token.indexOf("sensors=") + "sensors=".length();
				String rawSensors = token.substring(sensorsBegin, token.indexOf("/", sensorsBegin));
				String[] split = rawSensors.split(",");
				for (String id : split) {
                    sensors.add(new GxtSensor().setId(id));
				}

			} catch (Exception e) {
				LOG.severe("Failed to serialize token: " + e);
			}

			// parse type
			try {
				int typeBegin = token.indexOf("type=") + "type=".length();
				String rawType = token.substring(typeBegin, token.indexOf("/", typeBegin));
				type = Integer.parseInt(rawType);

			} catch (Exception e) {
				LOG.severe("Failed to serialize token: " + e);
			}

			// parse start
			try {
				int startBegin = token.indexOf("start=") + "start=".length();
				String rawStart = token.substring(startBegin, token.indexOf("/", startBegin));
				start = Long.parseLong(rawStart);

			} catch (Exception e) {
				LOG.severe("Failed to serialize token: " + e);
			}

			// parse start
			try {
				int endBegin = token.indexOf("end=") + "end=".length();
				String rawEnd = token.substring(endBegin, token.indexOf("/", endBegin));
				end = Long.parseLong(rawEnd);

			} catch (Exception e) {
				LOG.severe("Failed to serialize token: " + e);
			}

			// parse subsample
			try {
				int subsampleBegin = token.indexOf("subsample=") + "subsample=".length();
				String rawSubsample = token.substring(subsampleBegin);
				subsample = Boolean.parseBoolean(rawSubsample);

			} catch (Exception e) {
				LOG.severe("Failed to serialize token: " + e);
			}

			return new VisualizePlace(sensors, type, start, end, subsample);
		}

		@Override
		public String getToken(VisualizePlace place) {
			String sensors = "";
			for (GxtSensor sensor : place.getSensors()) {
				sensors += sensor.getId() + ",";
			}
			if (sensors.length() > 1) {
				sensors = sensors.substring(0, sensors.length() - 1);
			}
			return "sensors=" + sensors + "/type=" + place.getType() + "/start=" + place.getStart()
					+ "/end=" + place.getEnd() + "/subsample=" + place.isSubsample();
		}
	}

	private int type;
	private long start;
	private long end;
	private boolean subsample;
	private List<GxtSensor> sensors;

	public VisualizePlace(List<GxtSensor> sensors, int type, long start, long end, boolean subsample) {
		this.sensors = sensors;
		this.type = type;
		this.start = start;
		this.end = end;
		this.subsample = subsample;
	}

	public long getEnd() {
		return end;
	}

	public List<GxtSensor> getSensors() {
		return sensors;
	}

	public long getStart() {
		return start;
	}

	public int getType() {
		return type;
	}

	public boolean isSubsample() {
		return subsample;
	}
}
