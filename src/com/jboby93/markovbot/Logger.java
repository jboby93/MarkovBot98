package com.jboby93.markovbot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Logger {

	public static void openLogFile() {
		if (!Logger.logFileOpen) {
			String fileName = "stdout.txt";
	
			//if the file exists, delete it so we can start clean
			File check = new File(fileName);
			if (check.exists()) {
				check.delete();
			}
	
			//String f = "../../logs/" + sessionID + "-" + getTimeStampForFileName() + ".log";
			try {
				Logger.log = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
				Logger.log_file = fileName;
				Logger.logFileOpen = true;
			} catch (IOException e) {
				Logger.log("IOException opening log file " + fileName + ":" + e.getMessage());
			}
		} else {
			Logger.log("A log file is already opened!");
		}
	}

	public static void writeLogFile(String msg) {
		if (Logger.logFileOpen) {
			Logger.log.println(msg);
			Logger.log.flush();
		} else {
			//no log file is open!
		}
	}

	public static void closeLogFile() {
		if (Logger.logFileOpen) {
			Logger.log.close();
			Logger.logFileOpen = false;
			Logger.log("closeLogFile(): log file " + Logger.log_file + " closed");
			Logger.log_file = "[null]";
		}
	}

	public static void log(String msg) {
		Logger.log(msg, 0);
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
		if (Logger.log_level >= level) {
			System.out.println(Tools.getTimeStamp() + l + msg);
	
		}
		if (Logger.logFileOpen)
			writeLogFile(Tools.getTimeStamp() + l + msg);
	}

	public static void logStackTrace(Exception e) {
		StringWriter stack = new StringWriter();
		e.printStackTrace(new PrintWriter(stack));
		log("[stack]: " + stack.toString());
	}

	/*
	 * The @SuppressWarnings tags stop the compiler from complaining that some
	 * variables are declared but not being used. I've made the assumption that
	 * they are used in some component I haven't seen yet If they actually
	 * aren't being used, you should consider removal
	 */
	static final int log_level = 0;
	// Logging
	static String log_file = null; // Log file name
	static boolean logFileOpen = false;
	static PrintWriter log; // The log itself

}
