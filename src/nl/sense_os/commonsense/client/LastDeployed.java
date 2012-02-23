package nl.sense_os.commonsense.client;

public class LastDeployed {

    private static final String deployed = "Feb 23 12:32";

    public static String getPrettyString() {
        return deployed;
    }

    private LastDeployed() {
        // do not instantiate
    }
}
