package com.jboby93.markovbot;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Logger {

	private static final String LOG_FILE_NAME = "Log.txt"; // Log file name
	private static PrintWriter log; // The log itself
	private static final LogLevel logLevel = LogLevel.VERBOSE; // Show all logs

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
			debug("closeLogFile(): log file " + LOG_FILE_NAME + " closed");
			log.close();
			log = null;
		}
	}

	public static void info(String msg) {
		log(msg, LogLevel.INFO);
	}

	public static void debug(String msg) {
		log(msg, LogLevel.DEBUG);
	}

	public static void warning(String msg) {
		log(msg, LogLevel.WARNING);
	}

	public static void error(String msg) {
		log(msg, LogLevel.ERROR);
	}

	private static void log(String msg, LogLevel level) {
		String entry = Tools.getTimeStamp() + level.toString() + msg;
		if (logLevel.ordinal() >= level.ordinal()) {
			System.out.println(entry);
		}
		writeLogFile(entry);
	}

	public static void logStackTrace(Exception e) {
		StringWriter stack = new StringWriter();
		e.printStackTrace(new PrintWriter(stack));
		error("[stack]: " + stack.toString());
	}
}