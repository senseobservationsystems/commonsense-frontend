package nl.sense_os.commonsense.server.utility;

import java.sql.Timestamp;

public class TimestampConverter {

	public static String timestampToMicroEpoch(Timestamp t) {
//		Long l = t.getTime() / 1000;
//		Double d = ((double) t.getNanos()) / 1000000000; 
//		String s = String.format("%.8f", d) + "%20" + l.toString();
		return (t.getTime() / 1000) + "";
	}
	
	public static Timestamp microEpochToTimestamp(String s) {
	    float f = Float.parseFloat(s) * 1000;
	    Timestamp t = new Timestamp((long) f);
//		Timestamp t = new Timestamp(Long.parseLong(s.split(".")[0])*1000);
//		t.setNanos(new Double(Double.parseDouble(s.split(" ")[0])*1000000000).intValue());
		return t;
	}	
}
