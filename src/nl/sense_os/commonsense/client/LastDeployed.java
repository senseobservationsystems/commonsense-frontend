package nl.sense_os.commonsense.client;

public class LastDeployed {

    private static final String deployed = "Feb 23 12:10";

    private LastDeployed() {
        // do not instantiate
    }

    public static String getPrettyString() {
        return deployed;
    }
}
