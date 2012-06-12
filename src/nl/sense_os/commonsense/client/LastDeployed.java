package nl.sense_os.commonsense.client;

public class LastDeployed {

    private static final String deployed = "Jun 16 16:52";

    public static String getPrettyString() {
	return deployed;
    }

    private LastDeployed() {
	// do not instantiate
    }
}
