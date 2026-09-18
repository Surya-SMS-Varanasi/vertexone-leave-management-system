package com.vertexone.leavems.model;

import org.springframework.web.multipart.MultipartFile;

public class EmployeeReg {
	private String empid;
	private String fullname;
	private int age;
	private String gender;
	private String pic_file_name;
	private String mobno;
	private String empgrade;
	private String designation;
	private String joiningdate;
	private String addedon;
	private MultipartFile profilepic;
	private byte[] profilepicData;
	public byte[] getProfilepicData() {
		return profilepicData;
	}
	public void setProfilepicData(byte[] profilepicData) {
		this.profilepicData = profilepicData;
	}
	public MultipartFile getProfilepic() {
		return profilepic;
	}
	public void setProfilepic(MultipartFile profilepic) {
		this.profilepic = profilepic;
	}
	public String getEmpid() {
		return empid;
	}
	public void setEmpid(String empid) {
		this.empid = empid;
	}
	public String getFullname() {
		return fullname;
	}
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getPic_file_name() {
		return pic_file_name;
	}
	public void setPic_file_name(String pic_file_name) {
		this.pic_file_name = pic_file_name;
	}
	public String getMobno() {
		return mobno;
	}
	public void setMobno(String mobno) {
		this.mobno = mobno;
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
	public String getJoiningdate() {
		return joiningdate;
	}
	public void setJoiningdate(String joiningdate) {
		this.joiningdate = joiningdate;
	}
	public String getAddedon() {
		return addedon;
	}
	public void setAddedon(String addedon) {
		this.addedon = addedon;
	}
	
	
}
