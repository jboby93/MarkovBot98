package com.jboby93.markovbot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTime {
	
	private Date date;
	private DateFormat longDate;
	private DateFormat timeStamp;
	
	public DateTime(){
		this.date = new Date();
		this.longDate = new SimpleDateFormat("MMMM DD, YYYY hh:mm a 'Z'");
		this.timeStamp = new SimpleDateFormat("HH:mm:ss");
	}
	
	
	// Long date
	public String getDateString() {
		return longDate.format(date);
	}

	// Short date
	public String getTimeStampForFileName() {
		return timeStamp.format(date);
	}

	// Short date with []
	public String getTimeStamp() {
		return "["+timeStamp.format(date)+"]";
	}

	public long getUNIXTimestamp() {
		return date.getTime() / 1000;
	}
}