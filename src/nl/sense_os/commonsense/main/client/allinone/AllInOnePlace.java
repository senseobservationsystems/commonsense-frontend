package nl.sense_os.commonsense.main.client.allinone;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class AllInOnePlace extends Place {

	/**
	 * PlaceTokenizer knows how to serialize the Place's state to a URL token.
	 */
	@Prefix("allinone")
	public static class Tokenizer implements PlaceTokenizer<AllInOnePlace> {

		@Override
		public AllInOnePlace getPlace(String token) {
			return new AllInOnePlace(token);
		}

		@Override
		public String getToken(AllInOnePlace place) {
			return place.getToken();
		}
	}

	private String token;

	public AllInOnePlace() {
		this("");
	}

	public AllInOnePlace(String token) {
		this.token = token;
	}

	public String getToken() {
		return this.token;
	}
}
