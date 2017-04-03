package com.jboby93.markovbot;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Logger {
	
	private static final String LOG_FILE_NAME = "Log.txt"; // Log file name
	private static PrintWriter log; // The log itself
	private static final int logLevel = 0;

	public static void openLogFile() {
		if (log == null) {
			try {
				log = new PrintWriter(new FileWriter(LOG_FILE_NAME, true)); // Always appending to log
			} catch (IOException e) {
				// You won't be able to log the failure if you can't instantiate the log in the first place
				e.printStackTrace();
			}
		}
	}

	public static void writeLogFile(String msg) {
		if (log != null) {
			log.println(msg); // println automatically flushes
		}
	}

	public static void closeLogFile() {
		if (log != null) {
			log.close();
			log("closeLogFile(): log file " + LOG_FILE_NAME + " closed");
			log = null;
		}
	}

	public static void log(String msg) {
		log(msg, 0);
	}

	public static void log(String msg, int level) {
		String l = "";
		switch (level) {
		case 0:
			l = " ";
			break;
		case 1:
			l = " [V1] ";
			break;
		case 2:
			l = " [V2] ";
			break;
		}
		String entry = Tools.getTimeStamp() +l+msg;
		if (logLevel >= level) {
			System.out.println(entry);
		}
		
		writeLogFile(entry);
	}

	public static void logStackTrace(Exception e) {
		StringWriter stack = new StringWriter();
		e.printStackTrace(new PrintWriter(stack));
		log("[stack]: " + stack.toString());
	}
}