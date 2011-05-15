package nl.sense_os.commonsense.shared.constants;

public class Keys {

    /**
     * Google Maps API key, generated for http://common-sense-test.appspot.com
     */
    private static final String MAPS_KEY_TEST = "ABQIAAAAcc8ibe_QaK2XBw4Vp-cVyBQYr_M-iqqVQWbBU0ti1KBe5MFjFxQAq9nNCLMy6cXkTX8xOCj9FjzFJA";

    /**
     * Google Maps API key, generated for http://common-sense.appspot.com
     */
    private static final String MAPS_KEY_STABLE = "ABQIAAAA3D_pshePfBK3EagBrZGggxRw2HbSAHXTbPxYxhYhGrEpeVUi1BS8AbO5bPL1UMzVm1LL2thJx-M_jw";

    /**
     * Google Maps API key.
     */
    public static final String MAPS_KEY = Constants.TEST_MODE ? MAPS_KEY_TEST : MAPS_KEY_STABLE;

    private Keys() {
        // empty private constructor to prevent instantiation
    }
}
