package nl.sense_os.commonsense.main.client.visualization;

import java.util.logging.Logger;

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
			int type = -1;
			long start = -1;
			long end = -1;
			boolean subsample = false;

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
				int subsampleBegin = token.indexOf("start=") + "start=".length();
				String rawSubsample = token.substring(subsampleBegin);
				subsample = Boolean.parseBoolean(rawSubsample);

			} catch (Exception e) {
				LOG.severe("Failed to serialize token: " + e);
			}

			return new VisualizePlace(type, start, end, subsample);
		}

		@Override
		public String getToken(VisualizePlace place) {
			return "type=" + place.getType() + "/start=" + place.getStart() + "/end="
					+ place.getEnd() + "/subsample=" + place.isSubsample();
		}
	}

	private int type;
	private long start;
	private long end;
	private boolean subsample;

	public VisualizePlace(int type, long start, long end, boolean subsample) {
		this.type = type;
		this.start = start;
		this.end = end;
		this.subsample = subsample;
	}

	public long getEnd() {
		return end;
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
