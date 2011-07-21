package nl.sense_os.commonsense.client.common.constants;

public class Keys {

    /**
     * Google Maps API key, generated for http://rc.sense-os.nl
     */
    private static final String MAPS_KEY_RC = "ABQIAAAAcc8ibe_QaK2XBw4Vp-cVyBQsjUjcByiCivvn1ppG43l0EBYmuxQJjoQuotfoMFzKYEK6QgwlJRD2Pg";

    /**
     * Google Maps API key, generated for http://common.sense-os.nl
     */
    private static final String MAPS_KEY_STABLE = "ABQIAAAAcc8ibe_QaK2XBw4Vp-cVyBQRZw5PnBZuDX77DtiPDAIQJTIImRRXR2NlGlzF15dD3pzgYJu67vgxTw";

    /**
     * Google Maps API key, generated for http://common.dev.sense-os.nl
     */
    private static final String MAPS_KEY_DEV = "ABQIAAAAcc8ibe_QaK2XBw4Vp-cVyBSkBEmSOMRgjngroDitmgRTGdBMeRTbwc1k-RzAZgpJJ7UzaCSpp5AFyQ";

    /**
     * Google Maps API key, generated for http://apigee.common.sense-os.nl
     */
    private static final String MAPS_KEY_APIGEE = "ABQIAAAAcc8ibe_QaK2XBw4Vp-cVyBTvcdSqkWlRFnarZeagA5hkQdjGexTqYU3q3N3rdDah3xpifwQSeolw8w";

    /**
     * Maps key for "regular" deployments: either stable version or test version.
     */
    private static final String MAPS_KEY_REGULAR = Constants.RC_MODE
            ? MAPS_KEY_RC
            : MAPS_KEY_STABLE;
    private static final String MAPS_KEY_INTERMED = Constants.APIGEE_MODE
            ? MAPS_KEY_APIGEE
            : MAPS_KEY_REGULAR;

    /**
     * Google Maps API key.
     */
    public static final String MAPS_KEY = Constants.DEV_MODE ? MAPS_KEY_DEV : MAPS_KEY_INTERMED;

    private Keys() {
        // empty private constructor to prevent instantiation
    }
}
