package nl.sense_os.commonsense.main.client.sensors;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class SensorsPlace extends Place {

	/**
	 * PlaceTokenizer knows how to serialize the Place's state to a URL token.
	 */
	@Prefix("sensors")
	public static class Tokenizer implements PlaceTokenizer<SensorsPlace> {

		@Override
		public SensorsPlace getPlace(String token) {
			return new SensorsPlace(token);
		}

		@Override
		public String getToken(SensorsPlace place) {
			return place.getToken();
		}
	}

	private String token;

	public SensorsPlace() {
		this("");
	}

	public SensorsPlace(String token) {
		this.token = token;
	}

	public String getToken() {
		return this.token;
	}
}
