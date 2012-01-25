package nl.sense_os.commonsense.client;

public class LastDeployed {

    private static final String deployed = "Jan 25 15:53";

    private LastDeployed() {
        // do not instantiate
    }

    public static String getPrettyString() {
        return deployed;
    }
}
