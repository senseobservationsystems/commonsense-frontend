package nl.sense_os.commonsense.client;

public class LastDeployed {

    private static final String deployed = "Feb 24 11:57";

    public static String getPrettyString() {
        return deployed;
    }

    private LastDeployed() {
        // do not instantiate
    }
}
