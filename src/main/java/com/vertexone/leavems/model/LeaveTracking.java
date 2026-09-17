package com.vertexone.leavems.model;

public class LeaveTracking {
	
	private int trackid;
	private int leaveid;
	private String empid;
	private String status;
	private String actiondate;
	private String remarks;
	public int getTrackid() {
		return trackid;
	}
	public void setTrackid(int trackid) {
		this.trackid = trackid;
	}
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
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getActiondate() {
		return actiondate;
	}
	public void setActiondate(String actiondate) {
		this.actiondate = actiondate;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
}
