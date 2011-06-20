package nl.sense_os.commonsense.client.common.constants;

public class Keys {

    /**
     * Google Maps API key, generated for http://rc.sense-os.nl
     */
    private static final String MAPS_KEY_RC = "ABQIAAAAcc8ibe_QaK2XBw4Vp-cVyBQsjUjcByiCivvn1ppG43l0EBYmuxQJjoQuotfoMFzKYEK6QgwlJRD2Pg";
    // old key:
    // "ABQIAAAAcc8ibe_QaK2XBw4Vp-cVyBQYr_M-iqqVQWbBU0ti1KBe5MFjFxQAq9nNCLMy6cXkTX8xOCj9FjzFJA";

    /**
     * Google Maps API key, generated for http://common.sense-os.nl
     */
    private static final String MAPS_KEY_STABLE = "ABQIAAAAcc8ibe_QaK2XBw4Vp-cVyBQRZw5PnBZuDX77DtiPDAIQJTIImRRXR2NlGlzF15dD3pzgYJu67vgxTw";
    // old key:
    // "ABQIAAAA3D_pshePfBK3EagBrZGggxRw2HbSAHXTbPxYxhYhGrEpeVUi1BS8AbO5bPL1UMzVm1LL2thJx-M_jw";

    /**
     * Google Maps API key, generated for http://common.dev.sense-os.nl
     */
    private static final String MAPS_KEY_DEV = "ABQIAAAAcc8ibe_QaK2XBw4Vp-cVyBSkBEmSOMRgjngroDitmgRTGdBMeRTbwc1k-RzAZgpJJ7UzaCSpp5AFyQ";

    /**
     * Maps key for "regular" deployments to appspot: either stable version or test version.
     */
    private static final String MAPS_KEY_REGULAR = Constants.RC_MODE
            ? MAPS_KEY_RC
            : MAPS_KEY_STABLE;

    /**
     * Google Maps API key.
     */
    public static final String MAPS_KEY = Constants.DEV_MODE ? MAPS_KEY_DEV : MAPS_KEY_REGULAR;

    private Keys() {
        // empty private constructor to prevent instantiation
    }
}
