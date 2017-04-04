package bot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import db.DBSearchResult;
import logging.About;
import logging.Logger;
import time.DateTime;

public class App {
	private static MarkovBot bot; 					// The bot to use
	private static String lastResult = null; 		// Last generated result
	private static String startTimeString = null;

	private static Scanner reader; 					// Scanner over BufferedReader, little I/O and less exception handling

	public static String getStatus() {
		return bot.getStatus();
	}

	public static void main(String args[]) {
		DateTime dateTime = new DateTime();
		startTimeString = dateTime.getDateString();
		reader = new Scanner(System.in);

		about();
		try {
			Logger.openLogFile();
			Logger.debug("main(): program started on " + startTimeString);
			Logger.debug("main(): creating bot instance");
			bot = new MarkovBot();

			System.out.println("[This is the open-source and non-Facebook-connected version of TextpostBot 98.]");
			System.out.println("Like the page on FB if you want to see what happens when a crowdsourced Markov");
			System.out.println("chain generator is given shitposting powers!");
			System.out.println("");

			System.out.println("Welcome to " + About.name + " - type 'help' for commands");
			boolean quit = false;
			do {
				System.out.print("[" + getStatus() + "] > ");
				System.out.flush();
				String[] cmd = reader.nextLine().split(" "); // Do it all in one line

				switch (cmd[0]) {
				case "generate": //generate
				case "g":
					generatePost(cmd);
					break;
				case "last": //bring back the last generated output
				case "L":
					System.out.println("");
					System.out.println("This is the last generated result:");
					System.out.println(lastResult);
					System.out.println("=======================================");
					System.out.println("What do you want to do with this text?");
					System.out.println(" save - save to file; anything else - nothing");
					System.out.print("[save/[*]]: ");
					System.out.flush();
					String ask = reader.nextLine().toLowerCase();

					switch (ask) {
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
								List<String> terms = new ArrayList<String>();
								for (int t = 2; t < cmd.length; t++) {
									terms.add(cmd[t].trim().toLowerCase());
								}

								//results should be a listing, each n-gram has an ID which is its index in the database
								//this ID is used to perform editing operations.
								List<DBSearchResult> results = bot.getDB().search(terms);
								System.out.println(results.size() + " results found:");
								for (DBSearchResult result : results) {
									//[index] -- [key] -> [values]
									System.out.println(result.getIndex() + " [match score " + result.getScore()
											+ "] -- '" + result.getKey() + "' -> " + result.getValue().toString());
								}
							} else {
								//missing argument(s): search term(s)
								System.out.println("expected: search term(s)");
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
								System.out.println("expected: n-gram index. use 'search' to find an n-gram's index");
							}
							break;
						case "remove":
						case "rm":
							//argument: the index of the n-gram to remove
							if (cmd.length > 2) {
								bot.getDB().remove(Integer.parseInt(cmd[2]));
							} else {
								//missing argument - n-gram index
								System.out.println("expected: n-gram index. use 'search' to find an n-gram's index");
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
							System.out.println("usage: db [sub-command]");
							System.out.println("file i/o:");
							System.out.println(
									" - load - Loads a database from a file. (can also use command macro 'dbl')");
							System.out.println(
									" - save - Saves a database to a file. (can also use command macro 'dbs')");
							System.out.println("database operations:");
							System.out.println(" - search [terms]");
							System.out.println(
									"   Searches the database for n-grams containing or leading to the search terms.");
							System.out.println(
									"   Each result is given an n-gram index that describes its location in the database.  You'll need to supply this ID to use the edit or remove commands on a particular n-gram.");
							System.out.println(" - edit [n-gram index]");
							System.out.println(
									"   Allows editing of the outcomes of the n-gram at the specified index within the database, including creation and deletion.");
							System.out.println(" - remove [n-gram index]");
							System.out.println(
									"   Removes an n-gram and all of its associated outcomes from the database.  This can impact generation results.");
							System.out.println(" - clear - Clears all data from the database.");
							break;
						}
					} else {
						//missing argument: db
						//print list of subcommands
						System.out.println(
								"db: missing argument - expected load, save, search, edit, remove, clear, or help");
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
						System.out.println("expected debug command");
					} else {
						switch (cmd[1]) {
						case "time":
							Logger.debug("getUNIXTimestamp() returned " + dateTime.getUNIXTimestamp());
							break;
						default:
							System.out.println("Invalid debug command");
						}
					}
					break;
				case "help":
					System.out.println("Available commands:");
					System.out.println(" generate, g - generate a shitpost");
					System.out.println("               add \"from [file]\" to use a file as input text");
					System.out.println("          db - database commands");
					System.out.println("         dbl - load database");
					System.out.println("         dbs - save database");
					System.out.println("     read, r - read and learn from file");
					System.out.println("    teach, t - teach the bot from stdin");
					System.out.println("     quit, q - quit");
					break;
				default:
					System.out.println("unrecognized command: " + cmd[0]);
					break;
				}
			} while (!quit);
		} catch (Exception e) {
			panic(e);
		}

		Logger.debug("main(): exiting");
		Logger.closeLogFile();
	}

	public static void generatePost(String args[]) throws IOException {
		//arg: (optional) word count
		int wordCount = 100;
		boolean fromFile = false;

		if (args.length > 1) {
			if (args[1].matches("\\d+")) { // Only contains digits
				wordCount = Integer.parseInt(args[1]);
			} else { // Has other shit in it, won't parse in base 10
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
					System.out.println("generate: got " + bot.getDBSize() + " database entries from " + args[2]);
					System.out.print("How many words do you want? [#/[100]]: ");
					System.out.flush();

					String fromFile_wordCount = reader.nextLine();
					if (fromFile_wordCount.matches("\\d+")) { // Only digits
						wordCount = Integer.parseInt(fromFile_wordCount);
					} else { // Other shit than digits again
						System.out.println("using default value of 100");
						wordCount = 100;
					}
				}
			} else {
				System.out.println("generate: invalid argument; expected word count or \"from [filename]\"");
				System.out.print("How many words do you want? [#]: ");
				System.out.flush();

				String in_wordCount = reader.nextLine();
				if (in_wordCount.matches("\\d+")) { // Only digits in our string again
					wordCount = Integer.parseInt(in_wordCount);
				} else { // Other shit in there
					System.out.println("using default value of 100");
					wordCount = 100;
				}
			}
		}

		if (proceed && bot.getDBSize() > 0) {
			System.out.println("generating " + wordCount + " words of shit, hang on...");
			System.out.println("");

			String result = bot.generate(wordCount); //default: 100
			lastResult = result;

			System.out.println(result);
			System.out.println("=======================================");
			System.out.println("What do you want to do with this text?");
			System.out.println(" save - save to file; anything else - nothing");
			System.out.print("save/[*]]: ");
			System.out.flush();

			String ask = reader.nextLine().toLowerCase();
			switch (ask) {
			case "save":
				saveLastResultToFile();
				break;
			}
		} else {
			//canceled due to error or other reason
			if (bot.getDBSize() == 0) {
				Logger.warning("generatePost(): can't generate shit; database is empty!");
			}
		}

		if (usedTempDB) {
			//restore the temp database
			bot.clearDatabase();
			bot.replaceDatabase("tmp.botdb");
		}

		System.out.println("Done.");
	} //end generatePost()

	public static void learnFromFile(String args[]) {
		String file = null;
		if (args.length == 1) {
			System.out.print("Learn from file [or #cancel to cancel]: ");
			System.out.flush();
			file = reader.nextLine();
		} else {
			file = args[1];
		}

		if (!file.equals("#cancel")) {
			bot.learnFromFile(file);
			System.out.println("Done.");
		}
	}

	public static void learnFromConsole(String args[]) {

		String cancel = "#cancel";
		boolean done = false;

		int before = bot.getDBSize();

		System.out.println("The bot will be fed line by line.  Type #cancel to finish.");
		System.out.println("==========================================================");
		System.out.flush();

		String input = null;
		while (!done) {
			input = reader.nextLine();
			if (input.equals(cancel)) {
				done = true;
			} else {
				bot.learnFrom(input);
			}
		}

		int after = bot.getDBSize();
		System.out.println("Added " + (after - before) + " new entries to the bot's database");
	}

	public static void saveLastResultToFile() {
		System.out.println("Enter a filename or '#cancel' to cancel.");
		System.out.print("Save as: ");
		System.out.flush();

		String file = reader.nextLine();

		if (file.equals("#cancel")) {
			System.out.println("Operation cancelled.");
		} else {
			writeFile(file, lastResult, false);
			System.out.println("File saved successfully!");
		}
	}

	//on process crash, halt all threads
	private static void panic(Exception e) {
		Logger.error("panic(): panic handler invoked on exception -- the program will stop");
		Logger.logStackTrace(e);
	}

	public static String readFile(String file) throws IOException {
		BufferedReader fileReader = new BufferedReader(new FileReader(file));

		String out = "";
		String line = null;
		while ((line = fileReader.readLine()) != null) {
			out += line + About.NL;
		}
		fileReader.close();
		return out;
	}

	public static boolean confirm(String prompt) {
		System.out.print(prompt + " [y/n]");
		System.out.flush();
		String response = reader.nextLine();
		return (response.toLowerCase().charAt(0) == 'y');
	}

	public static void about() {
		String about = About.name + " - v" + About.version + " (" + About.build_date + ")\n";
		Logger.info(about);
		System.out.println(about);
	}

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