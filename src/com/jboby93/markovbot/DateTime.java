package com.jboby93.markovbot;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateTime {

	public static String getDateString() {
		Calendar c = Calendar.getInstance();
	
		String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September",
				"October", "November", "December" };
	
		return months[c.get(Calendar.MONTH)] + " " + c.get(Calendar.DAY_OF_MONTH) + ", " + c.get(Calendar.YEAR) + " "
				+ c.get(Calendar.HOUR) + ":"
				+ (c.get(Calendar.MINUTE) < 10 ? "0" + c.get(Calendar.MINUTE) : c.get(Calendar.MINUTE)).toString()
				+ (c.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm") + " " + TimeZone.getDefault()
						.getDisplayName(TimeZone.getDefault().inDaylightTime(new Date()), TimeZone.SHORT);
	}

	public static String getTimeStampForFileName() {
		Calendar c = Calendar.getInstance();
	
		return (c.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + c.get(Calendar.HOUR_OF_DAY) : c.get(Calendar.HOUR_OF_DAY))
				.toString()
				+ (c.get(Calendar.MINUTE) < 10 ? "0" + c.get(Calendar.MINUTE) : c.get(Calendar.MINUTE)).toString()
				+ (c.get(Calendar.SECOND) < 10 ? "0" + c.get(Calendar.SECOND) : c.get(Calendar.SECOND)).toString();
	}

	public static String getTimeStamp() {
		Calendar c = Calendar.getInstance();
		return "["
				+ (c.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + c.get(Calendar.HOUR_OF_DAY) : c.get(Calendar.HOUR_OF_DAY))
						.toString()
				+ ":" + (c.get(Calendar.MINUTE) < 10 ? "0" + c.get(Calendar.MINUTE) : c.get(Calendar.MINUTE)).toString()
				+ ":" + (c.get(Calendar.SECOND) < 10 ? "0" + c.get(Calendar.SECOND) : c.get(Calendar.SECOND)).toString()
				+ "]";
	}

	public static long getUNIXTimestamp() {
		return Calendar.getInstance().getTimeInMillis() / 1000;
	}
	
}
