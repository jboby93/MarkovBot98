package com.jboby93.markovbot;

import java.io.IOException;

public class MarkovBot {
	public static final String GENERATE_ERROR_NODATA = "#GENERATE_ERROR_NODATA#";

	private Exception lastException;
	private MarkovDB _db;

	public MarkovBot() {
		_db = new MarkovDB();
		lastException = null;
	}

	public String generate() {
		return this.generate(-1, 100);
	}

	public String generate(int wordLimit) {
		return this.generate(-1, wordLimit);
	}

	//4-27: added minWordCount support (not fully implemented yet)
	public String generate(int minWordCount, int wordLimit) {
		//pick a random key to start on
		_db.randomize();

		//ready to go?
		if (_db.getCurrentKey() == null) {
			//data model is empty
			Logger.warning("generate(): generate failed; no data to work with");
			return GENERATE_ERROR_NODATA;
		} else {
			//let's do it
			String r = _db.getCurrentKey() + " ";

			int words = _db.getN();
			boolean end = false;

			while (words < wordLimit && !end) {
				String next = _db.getNextWord();
				if (next.equals(MarkovDB.NO_ENTRY_FOR_CURRENT_KEY)) {
					//current n-gram not in database, 50/50 chance of starting a new paragraph
					end = true;
				} else {
					r += next + " ";
					words++;
				}
			}

			//did we generate enough words?
			if (minWordCount != -1) {
				if (words < minWordCount) {
					//todo: how do we enforce this? ideas:
					// - start a new paragraph, and generate the remaining amount of words
					// - start new sentence (add ".  " and continue)
					//
				}
			}

			return r;
		} //end if (is there anything in the data model?)
	} //end generate()

	public void learnFrom(String input) {
		_db.teach(input);
	}

	public int learnFromFile(String file) {
		try {
			String in[] = App.readFile(file).split(App.NL);
			for (String s : in) {
				_db.teach(s);
			}

			return 0;
		} catch (IOException e) {
			Logger.logStackTrace(e);
			return -1;
		}
	} //end learnFromFile()

	public void clearDatabase() {
		_db.clear();
	}

	public void loadDatabaseFrom(String file) {
		_db.load(file);
	}

	public void loadDatabase() {
		_db.load();
	}

	public void saveDatabaseTo(String file) {
		_db.save(file);
	}

	public void saveDatabase() {
		_db.save();
	}

	//used by event queue handler
	public void replaceDatabase(String file) {
		_db.load_replace(file);
	}

	public void importDatabase(String file) {
		_db.load_import(file);
	}

	public String getStatus() {
		if (_db.getDBSize() == 0)
			return "NO-DB";
		else
			return "DB-" + _db.getDBSize() + (_db.isModified() ? "*" : "");
	}

	public int getDBSize() {
		return _db.getDBSize();
	}

	public MarkovDB getDB() {
		return _db;
	}

	public Exception getLastException() {
		return this.lastException;
	}
}
