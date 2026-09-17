package com.vertexone.leavems.model;

public class Feedback {
	private int feedid;
	private String empid;
	private String subject;
	private String message;
	private String addedon;
	
	private String fullname;
    private String pic_file_name;
    private String empgrade;
    private String gender;
    private int age;
    private String designation;
    private String mobno;

    
    public int getFeedid() {
		return feedid;
	}
	public void setFeedid(int feedid) {
		this.feedid = feedid;
	}
	public String getEmpid() {
		return empid;
	}
	public void setEmpid(String empid) {
		this.empid = empid;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getAddedon() {
		return addedon;
	}
	public void setAddedon(String addedon) {
		this.addedon = addedon;
	}
	public String getFullname() {
		return fullname;
	}
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
	public String getPic_file_name() {
		return pic_file_name;
	}
	public void setPic_file_name(String pic_file_name) {
		this.pic_file_name = pic_file_name;
	}
	public String getEmpgrade() {
		return empgrade;
	}
	public void setEmpgrade(String empgrade) {
		this.empgrade = empgrade;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getDesignation() {
		return designation;
	}
	public void setDesignation(String designation) {
		this.designation = designation;
	}
	public String getMobno() {
		return mobno;
	}
	public void setMobno(String mobno) {
		this.mobno = mobno;
	}
	
}
