package com.vertexone.leavems.model;

public class LeaveApplication {
	private int leaveid;
	private String empid;
	private String leavetype;
	private String fromdate;
	private String todate;
	private int noofdays;
	private String reason;
	private String appliedon;
	private String status;
	private String fullname;
	private String empgrade;
	private String designation;
	private String pic_file_name;
	
	public int getLeaveid() {
		return leaveid;
	}
	public void setLeaveid(int leaveid) {
		this.leaveid = leaveid;
	}
	public String getEmpid() {
		return empid;
	}
	public void setEmpid(String empid) {
		this.empid = empid;
	}
	public String getLeavetype() {
		return leavetype;
	}
	public void setLeavetype(String leavetype) {
		this.leavetype = leavetype;
	}
	public String getFromdate() {
		return fromdate;
	}
	public void setFromdate(String fromdate) {
		this.fromdate = fromdate;
	}
	public String getTodate() {
		return todate;
	}
	public void setTodate(String todate) {
		this.todate = todate;
	}
	public int getNoofdays() {
		return noofdays;
	}
	public void setNoofdays(int noofdays) {
		this.noofdays = noofdays;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getAppliedon() {
		return appliedon;
	}
	public void setAppliedon(String appliedon) {
		this.appliedon = appliedon;
	}
	public String getFullname() {
		return fullname;
	}
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
	public String getEmpgrade() {
		return empgrade;
	}
	public void setEmpgrade(String empgrade) {
		this.empgrade = empgrade;
	}
	public String getDesignation() {
		return designation;
	}
	public void setDesignation(String designation) {
		this.designation = designation;
	}
	public String getPic_file_name() {
		return pic_file_name;
	}
	public void setPic_file_name(String pic_file_name) {
		this.pic_file_name = pic_file_name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	
}
