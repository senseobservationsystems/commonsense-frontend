package nl.sense_os.commonsense.server.utility;

import java.sql.Timestamp;

public class TimestampConverter {

	public static String timestampToEpochSecs(Timestamp t) {

		return (t.getTime() / 1000) + "";
	}
	
	public static Timestamp epochSecsToTimestamp(String s) {
	    
	    double f = Double.parseDouble(s) * 1000;
		return new Timestamp((long) f);
	}	
}
