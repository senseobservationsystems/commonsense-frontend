package nl.sense_os.commonsense.client;

public class LastDeployed {

    private static final String deployed = "Jan 24 14:43";

    private LastDeployed() {
        // do not instantiate
    }

    public static String getPrettyString() {
        return deployed;
    }
}
