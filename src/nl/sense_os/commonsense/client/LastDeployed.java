package nl.sense_os.commonsense.client;

public class LastDeployed {

    private static final String deployed = "We Oct 12 14:54";

    private LastDeployed() {
        // do not instantiate
    }

    public static String getPrettyString() {
        return deployed;
    }
}
