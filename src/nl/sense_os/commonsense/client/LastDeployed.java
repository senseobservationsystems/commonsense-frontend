package nl.sense_os.commonsense.client;

public class LastDeployed {

    private static final String deployed = "Thu Oct 12 14:02";

    private LastDeployed() {
        // do not instantiate
    }

    public static String getPrettyString() {
        return deployed;
    }
}
