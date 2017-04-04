package db;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bot.App;
import logging.Logger;

public class MarkovDB {
	public static final String NO_ENTRY_FOR_CURRENT_KEY = "NO_ENTRY_FOR_CURRENT_KEY";

	private static final String keyvalue_sep = "#KV_SEP#";
	private static final String choice_sep = "#SEP#";

	private Map<String, List<String>> _data;
	private BufferedReader reader; // Lots of line reading, this is more efficient than Scanner
	private String currentKey;
	private boolean modified;
	private String filename;
	private int n;

	public int getDBSize() {
		return _data.size();
	}

	public int getN() {
		return n;
	}

	// A variable parameter constructor, can basically treat int... as int[],
	// But it lets you call the constructor like:
	// MarkovDB() or MarkovDB(2) or MarkovDB(1, 2, 3, 4)
	public MarkovDB(int... n) {
		_data = new HashMap<String, List<String>>();
		this.n = (n.length == 0) ? 2 : n[0];
		this.currentKey = null;
		this.modified = false;
		this.filename = null;
		this.reader = new BufferedReader(new InputStreamReader(System.in));
	}

	public String getCurrentKey() {
		return currentKey;
	}

	public void randomize() {
		//pick random n-gram from the database to start word generation from
		if (_data.entrySet().size() == 0) {
			//database is empty?
			Logger.error("randomize(): error; data model size is 0");
			currentKey = null;
		} else {
			int start = (int) Math.random() * _data.entrySet().size() - 1;
			currentKey = (String) _data.keySet().toArray()[start];
		}
	} //end randomize()

	public String getNextWord() {
		//generate random word from current n-gram
		//does the current n-gram exist?
		if (!_data.containsKey(currentKey)) {
			//nope, so randomize? or return null?
			return NO_ENTRY_FOR_CURRENT_KEY;
		}

		//get the available options
		List<String> options = _data.get(currentKey);

		//random bug?
		if (options.size() == 0) {
			return NO_ENTRY_FOR_CURRENT_KEY;
		}

		//pick one randomly
		String nextWord = options.get((int)Math.random() * options.size() - 1);

		try {
			String newKey[] = currentKey.split(" ");
			for (int i = 0; i < n - 1; i++) {
				newKey[i] = newKey[i + 1];
			}
			newKey[newKey.length - 1] = nextWord;
			currentKey = "";
			for (int i = 0; i < newKey.length; i++) {
				currentKey += newKey[i];
				if (i != newKey.length - 1) {
					currentKey += " ";
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			//pick a random new one
			currentKey = (String) _data.keySet().toArray()[(int)Math.random()*_data.entrySet().size() - 1];
		}

		return nextWord;
	}

	public void teach(String source) {
		// Replace all whitespace (newline, tab, space, etc) with single spaces then split
		String[] words = source.replaceAll("\\s+", " ").split(" ");

		for (int i = 0; i < words.length - n - 1; i++) {
			//form a string consisting of words in range i -> (i+n-1)
			String key = sanitize(words[i]);// + " " + sanitize(words[i+1]);
			for (int j = 1; j < n; j++)
				key = key + " " + sanitize(words[i + j]);

			//if there is already a value for this key in the map, get the current value, otherwise create an empty list
			List<String> t = (_data.containsKey(key) ? _data.get(key) : new ArrayList<String>());

			//add word (i+n+1) to this list. this is the word that comes after the phrase we generated above
			t.add(sanitize(words[i + n]));

			//update the value in the map
			_data.put(key, t);
		} //end for

		modified = true;
	} //end teach()

	private String sanitize(String word) {
		//convert to lowercase and remove any whitespace
		return word.toLowerCase().trim();
	}

	public boolean isModified() {
		return modified;
	}

	public String getFilename() {
		return filename;
	}

	public void load() {
		load(null);
	}

	public void load(String from) {
		boolean append = false;
		boolean cancel = false;

		if (filename != null) {
			//prompt before changing databases
			System.out.println("There is already a database currently open: " + filename);
			System.out.println("What do you want to do?");
			System.out.println("1) Import to current database");
			System.out.println("2) Close the current database and open a different one");
			System.out.print("[1/2/[cancel]]: ");
			System.out.flush();

			String r = null;
			try {
				r = reader.readLine();
			} catch (IOException e) {
				Logger.logStackTrace(e);
			}
			switch (r) {
			case "1":
				append = true;
				break;
			case "2":
				append = false;
				break;
			default:
				cancel = true;
			}
		}

		if (!cancel) {
			String file = null;
			if (from == null) {
				System.out.print("Load from file [or '#cancel']: ");
				System.out.flush();
				try {
					file = reader.readLine();
				} catch (IOException e) {
					Logger.logStackTrace(e);
				}
			} else {
				file = from;
			}
			if (!file.toLowerCase().equals("#cancel")) {
				try {
					readFromFile(file, append);
					filename = file;
				} catch (IOException e) {
					Logger.logStackTrace(e);
				}
			}
		}
	}

	public void load_replace(String from) {
		try {
			readFromFile(from, false);
			filename = from;
			Logger.info("db.load_replace(): replaced previous database with " + from);
		} catch (IOException e) {
			Logger.logStackTrace(e);
		} //end try
	}

	public void load_import(String from) {
		try {
			readFromFile(from, true);
			filename = from;
			Logger.info("db.load_import(): imported " + from + " to current database");
		} catch (IOException e) {
			Logger.logStackTrace(e);
		} //end try
	}

	private void readFromFile(String file, boolean append) throws IOException {
		//read the database from the file
		// (use MarkovBot VB.NET project as reference so we can load the same format)
		if (!append)
			_data.clear();

		String in = App.readFile(file);
		String lines[] = in.split(App.NL);

		for (String s : lines) {
			if (s.trim().isEmpty())
				continue;

			String[] pair = s.split(keyvalue_sep);
			String key = pair[0].trim();

			if (s.startsWith("N=")) {
				s = s.replace("N=", "");
				if (s.matches("\\d+")) {
					this.n = Integer.parseInt(s);
				} else {
					// Invalid format for the N=# line
				}
			} else {
				String[] value = null;
				if (pair.length == 2) {
					value = pair[1].split(choice_sep);
				}

				List<String> valueList = new ArrayList<String>();
				if (value != null) {
					for (String v : value) {
						valueList.add(v);
					}
				}

				if (append) {
					if (_data.containsKey(key)) {
						for (String l : _data.get(key)) {
							valueList.add(l);
						}
					}
				}

				_data.put(key, valueList);
			}
		}

		Logger.debug("db.readFromFile(): read " + (lines.length - 1) + " entries from file " + file);
	}

	public void save(String fileName) {
		try {
			writeToFile(fileName);
			modified = false;
			filename = fileName;
		} catch (IOException e) {
			Logger.logStackTrace(e);
		}
	}

	public void save() {
		if (filename == null) {
			try {
				saveAs();
			} catch (IOException e) {
				Logger.logStackTrace(e);
			}
		} else {
			try {
				writeToFile(filename);
				modified = false;
			} catch (IOException e) {
				Logger.logStackTrace(e);
			}
		}
	}

	public void saveAs() throws IOException {
		System.out.print("Save to file [or '#cancel']: ");
		System.out.flush();
		String file = reader.readLine();
		if (!file.toLowerCase().equals("#cancel")) {
			writeToFile(file);
			filename = file;
			modified = false;
		}
	}

	private void writeToFile(String file) throws IOException {
		// Just instantiate a new writer here, simpler
		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
		fileWriter.write("N=" + this.n);

		for (String key : _data.keySet()) {
			List<String> values = _data.get(key);
			String line = "";

			for (int i = 0; i < values.size(); i++) {
				line += values.get(i);
				if (i < values.size() - 1) {
					line += choice_sep;
				}
			}
			fileWriter.write(key + keyvalue_sep + line);
		}
		fileWriter.close();
		Logger.debug("db.writeToFile(): wrote " + (_data.keySet().size()) + " entries to file " + file);
	}

	public void clear() {
		_data.clear();
		modified = true;
	} //end clear()

	//editing methods
	public List<DBSearchResult> search(List<String> terms) {
		List<DBSearchResult> results = new ArrayList<DBSearchResult>();

		//find and add search result containers for each match, with scores (how many of the keywords does this result contain?)
		int index = 0;
		for (String key : _data.keySet()) {
			DBSearchResult thisResult = new DBSearchResult();
			thisResult.setKey(key);
			thisResult.setValue(_data.get(key));
			thisResult.setIndex(index++); // Index isn't used past here, increment now

			int score = thisResult.getScore();
			for (String term : terms) {

				//key matches +2
				if (key.toLowerCase().contains(term.toLowerCase())) {
					score += 2;
				}

				//value (outcome) matches
				for (String outcome : _data.get(key)) {
					if (outcome.toLowerCase().equals(term)) {
						score++;
					} else if (outcome.toLowerCase().contains(term)) {
						//exclude single-letter search terms
						if (term.length() > 1)
							score++;
					}
				}
			} //end for(each search term)
			thisResult.setScore(score);

			//any hits?
			if (score > 0) {
				results.add(thisResult);
			}
		}

		/*
		 * Shouldn't rely on implementation-specific ordering of any data
		 * structure using hashing Safer to manually order things at the end
		 * with a Comparator
		 * 
		 * DBSearchResult implements the Comparable interface, so instances can
		 * be determined to be greater or less than each other for sorting
		 * purposes
		 */

		Collections.sort(results);

		return results;
	}

	private String getKeyForIndex(int index) {
		return (String) _data.keySet().toArray()[index];
	}

	public void addOutcome(int index, String outcome) {
		_data.get(getKeyForIndex(index)).add(outcome);
		modified = true;
	}

	public void editOutcome(int index, int outcomeIndex, String newValue) {
		_data.get(getKeyForIndex(index)).set(outcomeIndex, newValue);
		modified = true;
	}

	public void removeOutcome(int index, int outcomeIndex) {
		_data.get(getKeyForIndex(index)).remove(outcomeIndex);
		modified = true;
	}

	public void remove(int at) {
		_data.remove(getKeyForIndex(at));
		this.modified = true;
	}
} //end class MarkovDB
