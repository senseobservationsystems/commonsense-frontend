package nl.sense_os.commonsense.client;

public class LastDeployed {

    private static final String deployed = "Jan 20 17:32";

    private LastDeployed() {
        // do not instantiate
    }

    public static String getPrettyString() {
        return deployed;
    }
}
