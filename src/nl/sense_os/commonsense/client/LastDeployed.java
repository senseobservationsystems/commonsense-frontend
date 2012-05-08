package nl.sense_os.commonsense.client;

public class LastDeployed {

    private static final String deployed = "May 8 17:21";

    public static String getPrettyString() {
	return deployed;
    }

    private LastDeployed() {
	// do not instantiate
    }
}
