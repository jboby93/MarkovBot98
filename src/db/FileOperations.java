package db;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import logging.Logger;

public class FileOperations {

	public static void writeFile(String file, String text, boolean append) {
		BufferedWriter fileWriter = null;
		try {
			fileWriter = new BufferedWriter(new FileWriter(file, append));
		} catch (IOException e) {
			Logger.error("Could not find file: " + file);
			Logger.logStackTrace(e);
		}
	
		try {
			fileWriter.write(text);
			fileWriter.close(); // Close calls flush
		} catch (IOException e1) {
			Logger.logStackTrace(e1);
		}
	}

}
