package nl.sense_os.commonsense.client;

public class LastDeployed {

    private static final String deployed = "Thu Oct 13 17:42";

    private LastDeployed() {
        // do not instantiate
    }

    public static String getPrettyString() {
        return deployed;
    }
}
