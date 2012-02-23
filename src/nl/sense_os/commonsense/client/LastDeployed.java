package nl.sense_os.commonsense.client;

public class LastDeployed {

    private static final String deployed = "Jan 24 13:58";

    public static String getPrettyString() {
        return deployed;
    }

    private LastDeployed() {
        // do not instantiate
    }
}
