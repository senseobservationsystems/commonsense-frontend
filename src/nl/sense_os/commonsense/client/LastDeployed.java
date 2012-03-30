package nl.sense_os.commonsense.client;

public class LastDeployed {

    private static final String deployed = "Mar 30 12:04";

    public static String getPrettyString() {
	return deployed;
    }

    private LastDeployed() {
	// do not instantiate
    }
}
