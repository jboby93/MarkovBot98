package com.jboby93.markovbot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;

import sun.management.VMManagement;

public class App {
	//==============================================
	// the usual
	//==============================================
	public static final String name = "MarkovBot 98";
	public static final String author = "jboby93";
	public static final String version = "0.7";
	public static final String build_date = "5/10/2016";
	public static final String NL = System.getProperty("line.separator");

	private static final int log_level = 0;
	//process ID
	private static String processID = "null";

	//the bot to use
	private static MarkovBot bot;

	//the last generated result
	private static String lastResult = "null";

	private static String startTimeString = "null";
	private static long startTime = -1;
	
	/**************************************************************************************************
	 * LOGGING STUFF \
	 **************************************************************************************************/
	private static String log_file = "[null]";
	private static boolean logFileOpen = false;
	private static PrintWriter log;
	
	public static String getStatus() {
		return bot.getStatus();
	}

	//==============================================
	// main()
	//==============================================
	public static void main(String args[]) {
		startTimeString = getDateString();
		startTime = getUNIXTimestamp();

		about();
		processID = getProcessID();

		try {
			openLogFile();
			log(name + " - v" + version + " (" + build_date + ")");
			log("main(): program started on " + startTimeString);
			log("main(): creating bot instance");
			bot = new MarkovBot();

			println("[This is the open-source and non-Facebook-connected version of TextpostBot 98.]");
			println("Like the page on FB if you want to see what happens when a crowdsourced Markov");
			println("chain generator is given shitposting powers!");
			println("");

			println("Welcome to " + name + " - type 'help' for commands");
			boolean quit = false;
			do {
				String in = readLine("[" + getStatus() + "] > ");
				String cmd[] = in.split(" ");
				//println("");

				switch (cmd[0]) {
				case "generate": //generate
				case "g":
					generatePost(cmd);
					break;
				case "last": //bring back the last generated output
				case "L":
					println("");
					println("This is the last generated result:");
					println(lastResult);
					println("=======================================");
					println("What do you want to do with this text?");
					println(" save - save to file; anything else - nothing");
					String ask = readLine("[save/[*]]: ");
					switch (ask.toLowerCase()) {
					case "save":
						saveLastResultToFile();
						break;
					}
					break;
				case "db":
					//get first argument
					if (cmd.length > 1) {
						switch (cmd[1]) {
						case "load":
							bot.loadDatabase();
							break;
						case "save":
							bot.saveDatabase();
							break;
						case "search":
						case "s":
							//remaining arguments: search terms
							if (cmd.length > 2) {
								//return n-grams the terms appear in, and also list the n-grams that
								//lead to the search terms
								ArrayList<String> terms = new ArrayList<String>();
								for (int t = 2; t < cmd.length; t++) {
									terms.add(cmd[t].trim().toLowerCase());
								}

								//results should be a listing, each n-gram has an ID which is its index in the database
								//this ID is used to perform editing operations.
								List<DBSearchResult> results = bot.getDB().search(terms);
								println(results.size() + " results found:");
								for (DBSearchResult result : results) {
									//[index] -- [key] -> [values]
									println(result.getIndex() + " [match score " + result.getScore() + "] -- '" + result.getKey()
											+ "' -> " + result.getValue().toString());
								}
							} else {
								//missing argument(s): search term(s)
								println("expected: search term(s)");
							}
							break;
						case "edit":
						case "e":
							//argument: the index of the n-gram to edit
							//looping menu: given the n-gram, list its possible outcomes
							//get the number of an outcome to edit or delete, or add a new outcome, or cancel
							if (cmd.length > 2) {

							} else {
								//missing argument - n-gram index
								println("expected: n-gram index. use 'search' to find an n-gram's index");
							}
							break;
						case "remove":
						case "rm":
							//argument: the index of the n-gram to remove
							if (cmd.length > 2) {
								bot.getDB().remove(Integer.parseInt(cmd[2]));
							} else {
								//missing argument - n-gram index
								println("expected: n-gram index. use 'search' to find an n-gram's index");
							}
							break;
						case "replace":
						case "r":
							//replace instances of one word with another throughout the database
							break;
						case "clear":
						case "C":
							if (confirm("Are you sure you want to empty the database?")) {
								if (confirm("Are you REALLY sure?")) {
									bot.getDB().clear();
								}
							}
							break;
						case "help":
							println("usage: db [sub-command]");
							println("file i/o:");
							println(" - load - Loads a database from a file. (can also use command macro 'dbl')");
							println(" - save - Saves a database to a file. (can also use command macro 'dbs')");
							println("database operations:");
							println(" - search [terms]");
							println("   Searches the database for n-grams containing or leading to the search terms.");
							println("   Each result is given an n-gram index that describes its location in the database.  You'll need to supply this ID to use the edit or remove commands on a particular n-gram.");
							println(" - edit [n-gram index]");
							println("   Allows editing of the outcomes of the n-gram at the specified index within the database, including creation and deletion.");
							println(" - remove [n-gram index]");
							println("   Removes an n-gram and all of its associated outcomes from the database.  This can impact generation results.");
							println(" - clear - Clears all data from the database.");
							break;
						}
					} else {
						//missing argument: db
						//print list of subcommands
						println("db: missing argument - expected load, save, search, edit, remove, clear, or help");
					}
					break;
				case "dbl": //load database
					if (cmd.length > 1) {
						bot.loadDatabaseFrom(cmd[1]);
					} else {
						bot.loadDatabase();
					}
					break;
				case "dbs": //save database
					if (cmd.length > 1) {
						bot.saveDatabaseTo(cmd[1]);
					} else {
						bot.saveDatabase();
					}
					break;
				case "read": //read and learn from file
				case "r":
					learnFromFile(cmd);
					break;
				case "teach": //learn from stdin, or filename if provided
				case "t":
					learnFromConsole(cmd);
					break;
				case "quit": //quit
				case "q":
					quit = true;
					break;
				case "debug":
					if (cmd.length == 1) {
						//missing arg
						println("expected debug command");
					} else {
						switch (cmd[1]) {
						case "time":
							log("getUNIXTimestamp() returned " + getUNIXTimestamp());
							break;
						default:
							println("Invalid debug command");
						}
					}
					break;
				case "help":
					println("Available commands:");
					println(" generate, g - generate a shitpost");
					println("               add \"from [file]\" to use a file as input text");
					println("          db - database commands");
					println("         dbl - load database");
					println("         dbs - save database");
					println("     read, r - read and learn from file");
					println("    teach, t - teach the bot from stdin");
					println("     quit, q - quit");
					break;
				default:
					println("unrecognized command: " + in.split(" ")[0]);
					break;
				}
			} while (!quit);
		} catch (Exception e) {
			panic(e);
		}

		log("main(): exiting");
		closeLogFile();
	} //end main()	

	//==============================================
	// commands (args[0] = the command itself)
	//==============================================
	public static void generatePost(String args[]) {
		//arg: (optional) word count
		int wordCount = 100;
		boolean fromFile = false;

		if (args.length > 1) {
			try {
				wordCount = Integer.parseInt(args[1]);
			} catch (Exception e) {
				fromFile = args[1].equals("from") && (args.length > 2);
				wordCount = -1;
			}
		}

		//if the argument wasn't numeric, see what it is
		boolean usedTempDB = false;
		boolean proceed = true;

		if (wordCount == -1) {
			if (fromFile) {
				//save current database to temp file
				bot.saveDatabaseTo("tmp.botdb");

				//clear the current database
				bot.clearDatabase();

				//open the file and feed it to the bot
				if (bot.learnFromFile(args[2]) == -1) {
					//exception occured
					proceed = false;
				} else {
					//get word count
					int tmp_size = bot.getDBSize();
					println("generate: got " + bot.getDBSize() + " database entries from " + args[2]);
					String fromFile_wordCount = readLine("How many words do you want? [#/[100]]: ");
					try {
						wordCount = Integer.parseInt(fromFile_wordCount);
					} catch (Exception e) {
						println("using default value of 100");
						wordCount = 100;
					}
				}
			} else {
				println("generate: invalid argument; expected word count or \"from [filename]\"");
				String in_wordCount = readLine("How many words do you want? [#]: ");
				try {
					wordCount = Integer.parseInt(in_wordCount);
				} catch (Exception e) {
					println("using default value of 100");
					wordCount = 100;
				}
			}
		} //end if (arguments check)

		if (proceed && bot.getDBSize() > 0) {
			println("generating " + wordCount + " words of shit, hang on...");
			println("");

			String result = bot.generate(wordCount); //default: 100
			lastResult = result;

			println(result);
			println("=======================================");
			println("What do you want to do with this text?");
			println(" save - save to file; anything else - nothing");
			String ask = readLine("[save/[*]]: ");
			switch (ask.toLowerCase()) {
			case "save":
				saveLastResultToFile();
				break;
			}
		} else {
			//canceled due to error or other reason
			if (bot.getDBSize() == 0) {
				log("generatePost(): can't generate shit; database is empty!");
			}
		}

		if (usedTempDB) {
			//restore the temp database
			bot.clearDatabase();
			bot.replaceDatabase("tmp.botdb");
		}

		println("Done.");
	} //end generatePost()

	public static void learnFromFile(String args[]) {
		String file = "";
		if (args.length == 1) {
			file = readLine("Learn from file [or #cancel to cancel]: ");
		} else {
			file = args[1];
		}

		if (!file.equals("#cancel")) {
			bot.learnFromFile(file);
			println("Done.");
		}
	} //end learnFromFile()

	public static void learnFromConsole(String args[]) {
		String input = "";
		String cancel = "#cancel";
		boolean done = false;

		int before = bot.getDBSize();

		println("The bot will be fed line by line.  Type #cancel to finish.");
		println("==========================================================");
		while (!done) {
			input = readLine();

			if (input.equals(cancel)) {
				done = true;
			} else {
				bot.learnFrom(input);
			}
		}

		int after = bot.getDBSize();

		println("Added " + (after - before) + " new entries to the bot's database");
	} //end learnFromConsole()

	public static void saveLastResultToFile() {
		println("Enter a filename or '#cancel' to cancel.");
		String file = readLine("save as: ");
		if (file.equals("#cancel")) {
			println("Operation cancelled.");
		} else {
			try {
				writeFile(file, lastResult);
				println("File saved successfully!");
			} catch (IOException e) {
				logStackTrace(e);
			}
		}
	} //end saveLastResultToFile()

	//on process crash, halt all threads
	private static void panic(Exception e) {
		log("panic(): panic handler invoked on exception -- the program will stop");
		logStackTrace(e);
		//hopefully from here the process can then end
	}

	//==============================================
	// usual I/O functions
	//==============================================
	public static void print(String s) {
		System.out.print(s);
	}

	public static void println(String s) {
		System.out.println(s);
	}

	public static void printStackTrace(Exception e) {
		StringWriter stack = new StringWriter();
		e.printStackTrace(new PrintWriter(stack));
		println("[stack]: " + stack.toString());
	}

	public static String readFile(String file) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line = null;
			StringBuilder sb = new StringBuilder();
			String ls = System.getProperty("line.separator");

			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append(ls);
			}

			reader.close();
			return sb.toString();
		} catch (IOException e) {
			throw e;
		}
	} //end readFile()

	public static String readLine(String prompt) {
		return readLine(prompt, false);
	}

	public static String readLine(String prompt, boolean newlineAfterPrompt) {
		System.out.print(prompt);
		if (newlineAfterPrompt)
			System.out.println("");

		return readLine();
	}

	public static String readLine() {
		try {
			Scanner s = new Scanner(System.in);
			String l = s.nextLine();
			//s.close();
			return l;
		} catch (Exception e) {
			return "readLine(): an exception occurred here? " + e.getMessage();
		}
	}

	public static boolean confirm(String prompt) {
		String response = readLine(prompt + " [y/N]: ");
		return response.toLowerCase().contains("y");
	}

	public static void about() {
		println(name + " - v" + version + " (" + build_date + ")");
		//println("developer: " + author);
		println("");
	}

	public static String Left(String str, int length) {
		return str.substring(0, Math.min(length, str.length()));
	}

	public static void writeFile(String file, String text) throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			//write the text
			bw.write(text);

			bw.flush();
			bw.close();
		} catch (IOException e) {
			throw e;
		}
	} //end writeFile()

	public static void writeLines(String file, ArrayList<String> lines) throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			//write the text
			for (String s : lines) {
				bw.write(s);
				bw.write("\n");
			}

			bw.flush();
			bw.close();
		} catch (IOException e) {
			throw e;
		}
	} //end writeLines()

	public static void appendToFile(String file, String text) throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
			//write the text
			bw.write(text);

			bw.flush();
			bw.close();
		} catch (IOException e) {
			throw e;
		}
	}

	public static void appendLinesToFile(String file, ArrayList<String> lines) throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
			//write the text
			for (String s : lines) {
				bw.write(s);
				bw.newLine();
			}

			bw.flush();
			bw.close();
		} catch (IOException e) {
			throw e;
		}
	}

	public static void openLogFile() {
		if (!logFileOpen) {
			//make dir if it doesn't exist
			//File logsDir = new File("../../logs"); //put in /cai/logs

			//if(logsDir.exists() && logsDir.isDirectory()) {
			//	//good
			//} else {
			//	logsDir.mkdir();
			//}

			String f = "stdout.txt";

			//if the file exists, delete it so we can start clean
			File check = new File(f);
			if (check.exists()) {
				check.delete();
			}

			//String f = "../../logs/" + sessionID + "-" + getTimeStampForFileName() + ".log";
			try {
				log = new PrintWriter(new BufferedWriter(new FileWriter(f, true)));
				log_file = f;
				logFileOpen = true;
			} catch (IOException e) {
				log("openLogFile(): IOException opening log file " + f + ":" + e.getMessage());
			}
		} else {
			log("openLogFile(): a log file is already opened!");
		}
	} //end openLogFile()

	public static void writeLogFile(String msg) {
		if (logFileOpen) {
			log.println(msg);
			log.flush();
		} else {
			//no log file is open!
		}
	} //end writeLogFile()

	public static void closeLogFile() {
		if (logFileOpen) {
			log.close();
			logFileOpen = false;
			log("closeLogFile(): log file " + log_file + " closed");
			log_file = "[null]";
		}
	} //end closeLogFile()

	public static void log(String msg) {
		log(msg, 0);
		//System.out.println(getTimeStamp() + " " + msg);
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
			System.out.println(getTimeStamp() + l + msg);

		}
		if (logFileOpen)
			writeLogFile(getTimeStamp() + l + msg);
		//if(iDebug) iPause();
	} //end log()

	public static void logStackTrace(Exception e) {
		StringWriter stack = new StringWriter();
		e.printStackTrace(new PrintWriter(stack));
		log("[stack]: " + stack.toString());
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

	//==============================================
	// Utility functions
	//==============================================
	//random number generator -- range: [0, max)
	public static int rand(int max) {
		return (int) (Math.random() * max);
	}

	//string join function as in PHP
	// (http://stackoverflow.com/questions/1515437/java-function-for-arrays-like-phps-join)
	public static String join(String r[], String d) {
		if (r.length == 0)
			return "";
		StringBuilder sb = new StringBuilder();
		int i;
		for (i = 0; i < r.length - 1; i++)
			sb.append(r[i] + d);
		return sb.toString() + r[i];
	}

	private static String getProcessID() {
		Integer pid = -1;

		try {
			RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
			Field jvm = runtime.getClass().getDeclaredField("jvm");
			jvm.setAccessible(true);
			VMManagement mgmt = (VMManagement) jvm.get(runtime);
			Method pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
			pid_method.setAccessible(true);

			pid = (Integer) pid_method.invoke(mgmt);
		} catch (Exception e) {
			log("getProcessID(): exception occurred: " + e.getMessage());
			pid = -1;
		}

		return pid.toString();
	} //end getProcessID()
} //end class App
