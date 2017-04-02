package com.jboby93.markovbot;

import java.util.ArrayList;
import java.util.List;

public class DBSearchResult {
	private int index;
	private int score;
	private String key;
	private List<String> value;
	
	public DBSearchResult(){
		this.index = -1;
		this.score = 0;
		this.key = "null";
		this.value = new ArrayList<String>();
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> value) {
		this.value = value;
	}
}