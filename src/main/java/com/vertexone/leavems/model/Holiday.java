package com.vertexone.leavems.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Holiday {
	private int hdid;
	private String hdname;
	private String hddate;
	private String session;
	public int getHdid() {
		return hdid;
	}
	public void setHdid(int hdid) {
		this.hdid = hdid;
	}
	public String getHdname() {
		return hdname;
	}
	public void setHdname(String hdname) {
		this.hdname = hdname;
	}
	public String getHddate() {
		return hddate;
	}
	public void setHddate(String hddate) {
		this.hddate = hddate;
	}
	public String getSession() {
		return session;
	}
	public void setSession(String session) {
		this.session = session;
	}
	
	 public String getFormattedDate() {
	        LocalDate date = LocalDate.parse(hddate);
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");

	        return date.format(formatter);
	    }
	 
	 public String getDay() {
		    LocalDate date = LocalDate.parse(hddate);
		    return String.format("%02d", date.getDayOfMonth());
		}
}
