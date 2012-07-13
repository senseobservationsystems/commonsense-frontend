package nl.sense_os.commonsense.client;

public class LastDeployed {

	private static final String deployed = "Jul 13 15:04";

	public static String getPrettyString() {
		return deployed;
	}

	private LastDeployed() {
		// do not instantiate
	}
}
