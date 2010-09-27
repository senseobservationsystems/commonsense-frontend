package nl.sense_os.commonsense.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import nl.sense_os.commonsense.server.data.BooleanValue;
import nl.sense_os.commonsense.server.data.FloatValue;
import nl.sense_os.commonsense.server.data.JsonValue;
import nl.sense_os.commonsense.server.data.SensorType;
import nl.sense_os.commonsense.server.data.StringValue;

public class GlobalCleanup {

	private static String URL = "http://common-sense-test.appspot.com/ivo/cleanup";
	//private static String URL = "http://localhost:8888/ivo/cleanup";
	private static String entityNameVar = "?entityName=";
	private static String amountVar = "&amount=";
	private static String basicAmount = "250";

	private static void removeAll(String type) {
		try {
			URL url;
			url = new URL(URL + entityNameVar + type + amountVar + basicAmount);

   			int amount = -1;
   			int total = 0;
   			while (amount != 0) {
   				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
   				amount = Integer.parseInt(in.readLine());
   				total = total + amount;
   				System.out.print(".");
   				in.close();
			}
   			System.out.println();
   			System.out.println(total + " entities of type " + type + " removed.");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	    
	public static void main(String[] args) {
		removeAll(SensorType.class.getName());
		removeAll(FloatValue.class.getName());
		removeAll(BooleanValue.class.getName());
		removeAll(JsonValue.class.getName());
		removeAll(StringValue.class.getName());
	}

	

}
