package com.jboby93.markovbot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Logger {
	
	private static final int log_level = 0;
	private static String log_file = null; // Log file name
	private static boolean logFileOpen = false;
	private static PrintWriter log; // The log itself

	public static void openLogFile() {
		if (!logFileOpen) {
			String fileName = "stdout.txt";
	
			//if the file exists, delete it so we can start clean
			File check = new File(fileName);
			if (check.exists()) {
				check.delete();
			}
	
			//String f = "../../logs/" + sessionID + "-" + getTimeStampForFileName() + ".log";
			try {
				log = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
				log_file = fileName;
				logFileOpen = true;
			} catch (IOException e) {
				log("IOException opening log file " + fileName + ":" + e.getMessage());
			}
		} else {
			log("A log file is already opened!");
		}
	}

	public static void writeLogFile(String msg) {
		if (logFileOpen) {
			log.println(msg);
			log.flush();
		} else {
			//no log file is open!
		}
	}

	public static void closeLogFile() {
		if (logFileOpen) {
			log.close();
			logFileOpen = false;
			log("closeLogFile(): log file " + log_file + " closed");
			log_file = "[null]";
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
		if (log_level >= level) {
			System.out.println(Tools.getTimeStamp() + l + msg);
	
		}
		if (logFileOpen)
			writeLogFile(Tools.getTimeStamp() + l + msg);
	}

	public static void logStackTrace(Exception e) {
		StringWriter stack = new StringWriter();
		e.printStackTrace(new PrintWriter(stack));
		log("[stack]: " + stack.toString());
	}
}