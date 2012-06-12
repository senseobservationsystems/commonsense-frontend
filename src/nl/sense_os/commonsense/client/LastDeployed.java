package nl.sense_os.commonsense.client;

public class LastDeployed {

    private static final String deployed = "Jun 12 17:00";

    public static String getPrettyString() {
	return deployed;
    }

    private LastDeployed() {
	// do not instantiate
    }
}
