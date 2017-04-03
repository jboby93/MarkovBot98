package com.jboby93.markovbot;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import sun.management.VMManagement;

public class Tools {
	// Random number generator -- range: [0, max)
	public static int rand(int max) {
		return (int) (Math.random() * max);
	}

	//string join function as in PHP
	public static String join(String r[], String d) {
		String out = "";
		for (int i = 0; i < r.length; i++) {
			out += r[i];
			if (i != r.length - 1) {
				out += d;
			}
		}
		return out;
	}

	public static String getProcessID() {
		Integer pid = -1;

		/*
		 * When Java 9 is alpha, this whole process will be doable in one line:
		 * long pid = ProcessHandle.current().getPid(); As opposed to using
		 * hacky reflection
		 */
		try {
			RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
			Field jvm = runtime.getClass().getDeclaredField("jvm");
			jvm.setAccessible(true);
			VMManagement mgmt = (VMManagement) jvm.get(runtime);
			Method pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
			pid_method.setAccessible(true);

			pid = (Integer) pid_method.invoke(mgmt);
		} catch (Exception e) {
			App.log("getProcessID(): exception occurred: " + e.getMessage());
			pid = -1;
		}

		return pid.toString();
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

	public static String getTimeStampForFileName() {
		Calendar c = Calendar.getInstance();

		return (c.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + c.get(Calendar.HOUR_OF_DAY) : c.get(Calendar.HOUR_OF_DAY))
				.toString()
				+ (c.get(Calendar.MINUTE) < 10 ? "0" + c.get(Calendar.MINUTE) : c.get(Calendar.MINUTE)).toString()
				+ (c.get(Calendar.SECOND) < 10 ? "0" + c.get(Calendar.SECOND) : c.get(Calendar.SECOND)).toString();
	}

	public static long getUNIXTimestamp() {
		return Calendar.getInstance().getTimeInMillis() / 1000;
	}

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
}
