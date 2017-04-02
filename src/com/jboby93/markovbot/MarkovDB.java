package com.jboby93.markovbot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class MarkovDB {
	private Map<String, ArrayList<String>> _data;

	public int getDBSize() {
		return _data.size();
	}

	private int n = 2;

	public int getN() {
		return n;
	}

	public MarkovDB() {
		this(2);
	}

	public MarkovDB(int n) {
		_data = new HashMap<String, ArrayList<String>>();
		this.n = n;
	}

	private String currentKey = "null";

	public String getCurrentKey() {
		return currentKey;
	}

	public void randomize() {
		//pick random n-gram from the database to start word generation from
		if (_data.entrySet().size() == 0) {
			//database is empty?
			App.log("randomize(): error; data model size is 0");
			currentKey = "null";
		} else {
			int start = App.rand(_data.entrySet().size() - 1);
			currentKey = (String) _data.keySet().toArray()[start];
		}
	} //end randomize()

	public static final String NO_ENTRY_FOR_CURRENT_KEY = "NO_ENTRY_FOR_CURRENT_KEY";

	public String getNextWord() {
		//generate random word from current n-gram

		//does the current n-gram exist?
		if (!_data.containsKey(currentKey)) {
			//nope, so randomize? or return null?
			return NO_ENTRY_FOR_CURRENT_KEY;
		}

		//get the available options
		ArrayList<String> options = _data.get(currentKey);

		//random bug?
		if (options.size() == 0) {
			return NO_ENTRY_FOR_CURRENT_KEY;
		}

		//pick one randomly
		String nextWord = options.get(App.rand(options.size() - 1));

		try {
			String newKey[] = currentKey.split(" ");
			for (int i = 0; i < n - 1; i++) {
				newKey[i] = newKey[i + 1];
			}
			newKey[newKey.length - 1] = nextWord;
			currentKey = App.join(newKey, " ");
		} catch (ArrayIndexOutOfBoundsException e) {
			//pick a random new one
			currentKey = (String) _data.keySet().toArray()[App.rand(_data.entrySet().size() - 1)];
		}

		return nextWord;
	} //end getNextWord()

	public void teach(String source) {
		//remove double-spaces and newlines, replacing with single-spaces
		String words[] = source.replace("  ", " ").replace("\n", " ").replace("\t", " ").split(" "); // (3-31: also remove tab characters)

		for (int i = 0; i < words.length - n - 1; i++) {
			//form a string consisting of words in range i -> (i+n-1)
			String key = sanitize(words[i]);// + " " + sanitize(words[i+1]);
			for (int j = 1; j < n; j++)
				key = key + " " + sanitize(words[i + j]);

			//if there is already a value for this key in the map, get the current value, otherwise create an empty list
			ArrayList<String> t = (_data.containsKey(key) ? _data.get(key) : new ArrayList<String>());

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

	private boolean modified = false;

	public boolean isModified() {
		return modified;
	}

	private String filename = "null";

	public String getFilename() {
		return filename;
	}

	public void load() {
		load("null");
	}

	public void load(String from) {
		boolean append = false;
		boolean cancel = false;

		if (!filename.equals("null")) {
			//prompt before changing databases
			App.println("There is already a database currently open: " + filename);
			App.println("What do you want to do?");
			App.println("1) Import to current database");
			App.println("2) Close the current database and open a different one");
			String r = App.readLine("[1/2/[cancel]]: ");
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
			String file = (from.equals("null") ? App.readLine("Load from file [or '#cancel']: ") : from);
			if (!file.toLowerCase().equals("#cancel")) {
				try {
					readFromFile(file, append);
					filename = file;
				} catch (IOException e) {
					App.logStackTrace(e);
				} //end try
			} //end if
		} //end if
	} //end load()

	public void load_replace(String from) {
		try {
			readFromFile(from, false);
			filename = from;
			App.log("db.load_replace(): replaced previous database with " + from);
		} catch (IOException e) {
			App.logStackTrace(e);
		} //end try
	}

	public void load_import(String from) {
		try {
			readFromFile(from, true);
			filename = from;
			App.log("db.load_import(): imported " + from + " to current database");
		} catch (IOException e) {
			App.logStackTrace(e);
		} //end try
	}

	private static final String keyvalue_sep = "#KV_SEP#";
	private static final String choice_sep = "#SEP#";

	private void readFromFile(String file, boolean append) throws IOException {
		//read the database from the file
		// (use MarkovBot VB.NET project as reference so we can load the same format)
		if (!append)
			_data.clear();

		String in = App.readFile(file);
		String lines[] = in.split(App.NL);

		for (String s : lines) {
			if (s.trim().equals(""))
				continue;

			String pair[] = s.split(keyvalue_sep);
			String key = pair[0].trim();

			if (App.Left(s, 2).equals("N=")) {
				try {
					int n = Integer.parseInt(s.replace("N=", ""));
					this.n = n;
				} catch (Exception e) {
					//invalid format for N=# line
					//
				}
			} else {
				String value[] = {};
				if (pair.length == 2) {
					value = pair[1].split(choice_sep);
				}

				ArrayList<String> value_list = new ArrayList<String>();
				for (String v : value) {
					value_list.add(v);
				}

				if (append) {
					if (_data.containsKey(key)) {
						for (String l : _data.get(key)) {
							value_list.add(l);
						}
					}
				} //end if

				_data.put(key, value_list);
			}
		} //end for

		App.log("db.readFromFile(): read " + (lines.length - 1) + " entries from file " + file);
	} //end readFromFile()

	public void save(String to) {
		try {
			writeToFile(to);
			modified = false;
			filename = to;
		} catch (IOException e) {
			App.logStackTrace(e);
		}
	} //end save()

	public void save() {
		if (filename.equals("null")) {
			saveAs();
		} else {
			try {
				writeToFile(filename);
				modified = false;
			} catch (IOException e) {
				App.logStackTrace(e);
			}
		}
	} //end save()

	public void saveAs() {
		String file = App.readLine("Save to file [or '#cancel']: ");
		if (!file.toLowerCase().equals("#cancel")) {
			try {
				writeToFile(file);
				filename = file;
				modified = false;
			} catch (IOException e) {
				App.logStackTrace(e);
			} //end try
		} //end if
	} //end saveAs()

	private void writeToFile(String file) throws IOException {
		//generate the output to write to the file
		ArrayList<String> out = new ArrayList<String>();
		out.add("N=" + this.n);
		for (Map.Entry<String, ArrayList<String>> pair : _data.entrySet()) {
			String key = pair.getKey();
			Object value_arr[] = pair.getValue().toArray();

			StringBuilder value_sb = new StringBuilder();
			int i = 0;
			for (Object s : value_arr) {
				value_sb.append((String) s);
				//is there more?
				if (i + 1 < pair.getValue().size())
					value_sb.append(choice_sep);
				i++; //bug fix (3/28/2016) - was missing this increment; could be why the database size seems to fluctuate
			}
			String value = value_sb.toString(); //App.join((String[])pair.getValue().toArray(), choice_sep);
			out.add(key + keyvalue_sep + value);
		}

		App.writeLines(file, out);
		App.log("db.writeToFile(): wrote " + (out.size() - 1) + " entries to file " + file);
	} //end writeToFile()

	public void clear() {
		_data.clear();
		modified = true;
	} //end clear()

	//editing methods
	public ArrayList<DBSearchResult> search(ArrayList<String> terms) {
		ArrayList<DBSearchResult> results = new ArrayList<DBSearchResult>();

		//find and add search result containers for each match, with scores (how many of the keywords does this result contain?)
		int index = 0;
		for (Map.Entry<String, ArrayList<String>> entry : _data.entrySet()) {
			DBSearchResult thisResult = new DBSearchResult();
			thisResult.key = entry.getKey();
			thisResult.value = entry.getValue();
			thisResult.index = index;

			for (String t : terms) {
				//key matches +2
				if (entry.getKey().toLowerCase().contains(t.toLowerCase())) {
					thisResult.score += 2;
				}

				//value (outcome) matches
				for (String o : entry.getValue()) {
					if (o.toLowerCase().equals(t)) {
						thisResult.score++;
					} else if (o.toLowerCase().contains(t)) {
						//exclude single-letter search terms
						if (t.length() > 1)
							thisResult.score++;
					}
				}
			} //end for(each search term)

			//any hits?
			if (thisResult.score > 0) {
				results.add(thisResult);
			}

			index++;
		} //end for(each database entry)

		//will already be sorted by ID since we're going in the order of the entry set of the data map
		//so next, sort by score, starting from the end of the results. if it has a higher score than the one above it, move it
		//	to the top of the results list
		//

		return results;
	} //end search()

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
