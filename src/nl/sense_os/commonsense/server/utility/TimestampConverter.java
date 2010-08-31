package nl.sense_os.commonsense.server.utility;

import java.util.Date;

public class TimestampConverter {

	public static String timestampToEpochSecs(Date t) {

		return (t.getTime() / 1000) + "";
	}
	
	public static Date epochSecsToTimestamp(String s) {
	    
	    double f = Double.parseDouble(s) * 1000;
		return new Date((long) f);
	}	
}
