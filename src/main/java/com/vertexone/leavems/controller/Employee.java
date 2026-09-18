package com.vertexone.leavems.controller;


import java.io.File;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vertexone.leavems.encryption.EncryptionManager;
import com.vertexone.leavems.model.AllowedLeave;
import com.vertexone.leavems.model.Calender;
import com.vertexone.leavems.model.EmployeeReg;
import com.vertexone.leavems.model.Feedback;
import com.vertexone.leavems.model.Holiday;
import com.vertexone.leavems.model.LeaveApplication;
import com.vertexone.leavems.model.LeaveBalance;
import com.vertexone.leavems.services.EmailService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;


@Controller
public class Employee {
	@Autowired 
	JdbcTemplate jTemp;
	
	@Autowired
	EmailService eservice;
	
	String getCurrentYearSession() {
		Date dt = new Date();
		
		int month= dt.getMonth();
		String st = dt.toString();
		String year = st.substring(st.lastIndexOf(" ")+1);
		int cur_year = Integer.parseInt(year);
		
		String cur_session="";
		if(month > 3)
			cur_session = cur_year+ "-"+(cur_year+1);
		else
			cur_session = (cur_year-1)+ "-"+cur_year;
		
		return cur_session;
	}
	
	@GetMapping("/empdashboard")
	public String dashboard(HttpServletRequest req, HttpSession session, Model m) {
		String empid = session.getAttribute("EmpSession").toString();
		LeaveBalance lb = jTemp.queryForObject("SELECT reamainingccl,remainingel, remainingcl,remainingml, remainingrh,remaingmaternityleave FROM leavebalancemaster WHERE empid = '"+empid+"'", new BeanPropertyRowMapper<>(LeaveBalance.class));
		int totalbal = lb.getRemaingmaternityleave() + lb.getReamainingccl() + lb.getRemainingcl() + lb.getRemainingel() + lb.getRemainingml()+ lb.getRemainingrh();
		int pending = jTemp.queryForObject("SELECT COUNT(*) FROM leavetrackingmaster WHERE empid = '"+empid+"' AND status = 'pending'", Integer.class);
		int approved = jTemp.queryForObject("SELECT COUNT(*) FROM leavetrackingmaster WHERE empid = '"+empid+"' AND status = 'Approved'", Integer.class);
		int rejected = jTemp.queryForObject("SELECT COUNT(*) FROM leavetrackingmaster WHERE empid = '"+empid+"' AND status = 'Rejected'", Integer.class);
		
		List<LeaveApplication> lst = jTemp.query("SELECT l.leavetype, l.fromdate, l.todate, l.noofdays, k.status FROM leaveapplicationmaster l INNER JOIN leavetrackingmaster k ON l.leaveid = k.leaveid WHERE k.empid = '"+empid+"'", new BeanPropertyRowMapper<>(LeaveApplication.class));
		
		List<Holiday> holilist = jTemp.query("SELECT * FROM holidaymaster ORDER BY hdid DESC",new BeanPropertyRowMapper<>(Holiday.class));

		m.addAttribute("pending", pending);
		m.addAttribute("approved", approved);
		m.addAttribute("rejected", rejected);
		m.addAttribute("remBal", totalbal);
		m.addAttribute("lst", lst);
		m.addAttribute("hlist",holilist);
		return "employee/dashboard";
	}
	
	@GetMapping("/leaveapplication")
	public String apply(HttpSession session, Model m) {
		String userId = session.getAttribute("EmpSession").toString();	
		String cur_ses = getCurrentYearSession();
		
		String sql ="SELECT * FROM leavebalancemaster WHERE empid = '"+userId+"' AND session = '"+cur_ses+"'";
		
		List<LeaveBalance> list = jTemp.query(sql, new BeanPropertyRowMapper<>(LeaveBalance.class));
		
		    if (list.isEmpty()) {	//if the list is empty then set values to the table.
		    	String sql2 = "SELECT empgrade FROM employeemaster WHERE empid = '"+userId+"'";
		    	String empgrade = jTemp.queryForObject(sql2, String.class);
		    	 
		    	String sql1 = "SELECT * FROM allowedleavemaster WHERE empgrade = '"+empgrade+"' AND session = '"+cur_ses+"'";
		    	
		    	AllowedLeave al = jTemp.queryForObject(sql1, new BeanPropertyRowMapper<>(AllowedLeave.class)); 
		    	
		    	String gensql = "SELECT gender FROM employeemaster WHERE empid='"+userId+"'";
		    	String genRes= jTemp.queryForObject(gensql, String.class);
		    	
		    	//Intializing the value from allowedleave table to leave balance table for gender-based separated employees
		    	if(genRes.trim().equals("Male")) {
		    		String intialSql = "INSERT INTO leavebalancemaster (empid,session,usedcl,remainingcl,usedml,remainingml,usedel,remainingel,usedccl,reamainingccl,usedmaternityleave,remaingmaternityleave,usedrh,remainingrh)VALUES('"+userId+"', '"+cur_ses+"','"+0+"','"+al.getAllowedcl()+"', '"+0+"', '"+al.getAllowedml()+"','"+0+"','"+al.getAllowedel()+"','"+0+"','"+al.getAllowedccl()+"','"+0+"','"+0+"','"+0+"', '"+al.getAllowedrh()+"')";
		    		jTemp.update(intialSql); // Updating the row
		    	}
		    	else {
		    		String intialSql = "INSERT INTO leavebalancemaster (empid,session,usedcl,remainingcl,usedml,remainingml,usedel,remainingel,usedccl,reamainingccl,usedmaternityleave,remaingmaternityleave,usedrh,remainingrh)VALUES('"+userId+"', '"+cur_ses+"','"+0+"','"+al.getAllowedcl()+"', '"+0+"', '"+al.getAllowedml()+"','"+0+"','"+al.getAllowedel()+"','"+0+"','"+al.getAllowedccl()+"','"+0+"','"+al.getAllowedmaternityleave()+"','"+0+"', '"+al.getAllowedrh()+"')";
		    		jTemp.update(intialSql); // Updating the row

		    	}		    	
		    	list = jTemp.query(sql, new BeanPropertyRowMapper(LeaveBalance.class)); // fetching the list		    
		        m.addAttribute("list", list.get(0));
		    }
		    else {
		    	LeaveBalance lb = jTemp.queryForObject(sql, new BeanPropertyRowMapper<>(LeaveBalance.class));
		    	m.addAttribute("list",lb);
		    }
		return "employee/application";
	}
	
	
	@PostMapping("/leaveapply")
	public String apply(RedirectAttributes ra,@ModelAttribute LeaveApplication la,HttpSession session, Model md) {
		
		String msg="";
		
		try {
			String userId = session.getAttribute("EmpSession").toString();
			
			String cur_ses=getCurrentYearSession();
			
			String sql="SELECT * FROM allowedleavemaster WHERE empgrade=(SELECT empgrade FROM employeemaster WHERE empid='"+userId+"') and session='"+cur_ses+"'";
			AllowedLeave al_db=jTemp.queryForObject(sql,new BeanPropertyRowMapper<>(AllowedLeave.class));
		
			String sql2="SELECT * FROM leavebalancemaster WHERE empid='"+userId+"'";
			List<LeaveBalance> lbm_Lst=jTemp.query(sql2,new BeanPropertyRowMapper<>(LeaveBalance.class));
			
			
			String myCommand="";
			String value = la.getLeavetype().trim();
			boolean isLeaveAvilable=true;
			String genSql = "SELECT gender FROM employeemaster WHERE empid = '"+userId+"'";
			String gender = jTemp.queryForObject(genSql, String.class);
			
			
				
			LeaveBalance lbm=lbm_Lst.get(0); // getting the latest leave balance for the employee
			
			if(value.trim().equals("Casual-leave"))
			{
				if(lbm.getRemainingcl() < la.getNoofdays()) {
					isLeaveAvilable = false;
				}
			}
			else if(value.trim().equals("Medical-leave"))
			{
				if(lbm.getRemainingml() < la.getNoofdays()) {
					isLeaveAvilable = false;
				}
			}
			else if(value.trim().equals("Earned-leave"))	
			{
				if(lbm.getRemainingel() < la.getNoofdays()) {
					isLeaveAvilable = false;
				}			
			}
			else if(value.trim().equals("Child-care-leave"))	
			{
				if(lbm.getReamainingccl() < la.getNoofdays()) {
					isLeaveAvilable = false;
				}				
			}
			else if(value.trim().equals("Maternity-leave"))	
			{
				if(lbm.getRemaingmaternityleave() < la.getNoofdays()) {
					isLeaveAvilable = false;
				}
				else if(gender.equals("Male")) {
					isLeaveAvilable = false;
				}				
			}
			else if(value.trim().equals("Restricted-holiday"))
			{
				if(lbm.getRemainingrh() < la.getNoofdays()) {
					isLeaveAvilable = false;
				}					
			}
			if(isLeaveAvilable == true) {
				String esql="INSERT INTO leaveapplicationmaster (empid,leavetype,fromdate,todate,noofdays,reason)VALUES('"+userId+"','"+la.getLeavetype()+"','"+la.getFromdate()+"','"+la.getTodate()+"','"+la.getNoofdays()+"','"+la.getReason()+"')";
				int res =jTemp.update(esql);
				if(res >0) {
					String leavesql = "SELECT leaveid FROM leaveapplicationmaster WHERE empid = '"+userId+"' ORDER BY leaveid DESC LIMIT 1";
					int leaveid = jTemp.queryForObject(leavesql, Integer.class);
					String tsql = "INSERT INTO leavetrackingmaster (leaveid,empid,status,remarks) VALUES('"+leaveid+"', '"+userId+"', 'pending', 'Leave has been applied by the employee.')";
					int r = jTemp.update(tsql);
					if(r > 0) {
						msg="Leave Apllied successfully. ";
					}
					else {
						String delsql ="DELETE FROM leaveapplicationmaster WHERE leaveid = '"+leaveid+"'";
						int result = jTemp.update(delsql);
						if(result > 0 )
							msg="Sorry ! leave not applied.";
						else
							msg="Technical Issue occured.";
					}
				}
				else {
					msg="Sorry ! Leave not applied";
				}
			}
			else {
				msg="Sorry ! You have insufficient balance for : "+ la.getLeavetype() + " ";					
			}
			
		}
		catch(Exception e) {			
			msg="Technical Issue occured ! "+e;
		}
		ra.addFlashAttribute("msg",msg);
		return "redirect:/leaveapplication";
	}
	
	@GetMapping("/leavebal")
	public String leavebal(HttpSession session, Model m) {
		
		String userId = session.getAttribute("EmpSession").toString();
		
		String sql ="SELECT * FROM leavebalancemaster WHERE empid = '"+userId+"'";
		String sql2 ="SELECT * FROM allowedleavemaster WHERE empgrade=(SELECT empgrade FROM employeemaster WHERE empid = '"+userId+"') ";
		AllowedLeave al = jTemp.queryForObject(sql2, new BeanPropertyRowMapper<>(AllowedLeave.class));
		LeaveBalance lb = jTemp.queryForObject(sql, new BeanPropertyRowMapper<>(LeaveBalance.class));
		m.addAttribute("list",lb);
		m.addAttribute("grade", al);
		return "employee/leavebalence";
	}
	
	
	@GetMapping("/calendar")
	public String holicalendar(Model m, @ModelAttribute Holiday h) {
		
		 String sql = "SELECT * FROM calendermaster ORDER BY cid DESC LIMIT 1";
		    Calender cal =jTemp.queryForObject(sql, new BeanPropertyRowMapper<>(Calender.class));

		m.addAttribute("list", cal);
		
		String holisql = "SELECT * FROM holidaymaster ORDER BY hdid DESC";
		
		List<Holiday> holilist = jTemp.query(holisql,new BeanPropertyRowMapper<>(Holiday.class));
		
		m.addAttribute("hlist",holilist);
		
		return "employee/calendar";
	}
	
	@GetMapping("/feedbac")
	public String feedback() {
		return "employee/feedback";
	}
	@PostMapping("/feedback-submit")
	public String feedback( RedirectAttributes rd, HttpSession session, @ModelAttribute Feedback f) {
		String msg="";
		
		try {
			String empid = session.getAttribute("EmpSession").toString();
			
			f.setEmpid(empid);
			
			String sql = "INSERT INTO feedmaster(empid,subject,message)VALUES('"+empid+"','"+f.getSubject()+"', '"+f.getMessage()+"')";
			int res = jTemp.update(sql);
			if(res > 0) {
				msg="Feedback Submitted Successfully.";
			}
			else {
				msg="Feedback can't be submitted !";
			}
			
		}
		catch(Exception e) {
			msg="Technical issue occured !"+e;
		}
		rd.addFlashAttribute("msg",msg);
		
		return "redirect:/feedbac";
	}
	@GetMapping("/updateprofile")
	public String updateProfilePage(HttpSession session, Model m) {

	    String userid = session.getAttribute("EmpSession").toString();
	    String sql = "SELECT empid, fullname, age, gender, mobno, pic_file_name, empgrade, designation, joiningdate, addedOn FROM employeemaster WHERE empid='"+userid+"'";

	    EmployeeReg emp = jTemp.queryForObject(sql,new BeanPropertyRowMapper<>(EmployeeReg.class));

	    m.addAttribute("emp", emp);
	    String pic = "/employee-images/"+ emp.getPic_file_name();
	    m.addAttribute("profilepic",pic);
	    return "employee/update";
	}
	@PostMapping("/updatepro")
	public String updateProfilePage(EmployeeReg er, MultipartFile profilepic,HttpSession session, RedirectAttributes ra) {
		String msg="";
	    String userid = session.getAttribute("EmpSession").toString();
	    String sql = "SELECT pic_file_name FROM employeemaster WHERE empid='"+userid+"'";
	    String dbname = jTemp.queryForObject(sql, String.class);
	    String fname= profilepic.getOriginalFilename();
	    try {
	    	if(fname.isEmpty()) {
		    	String sql1 = "UPDATE employeemaster SET mobno = '"+er.getMobno()+"' WHERE empid= '"+userid+"'";
				int n = jTemp.update(sql1);
				if(n>0) {
					msg="Profile updated successfully";
				} 
				else {
					msg="Profile couldn't be updated.";
				}
		    }	    	
	    	else if(!profilepic.isEmpty()) {
				String folderpath = System.getProperty("user.dir")+"/src/main/resources/static/employee-images";
				File f = new File(folderpath);
				if(!f.exists()) {
					f.mkdir();
				}
				
				fname = profilepic.getOriginalFilename();
				long sizeInKb = profilepic.getSize()/1024;
				String extension = fname.substring(fname.lastIndexOf('.')).toLowerCase();
				if(extension.equals(".png") || extension.equals(".jpg") || extension.equals(".jpeg")) {							
					if(sizeInKb > 200) {
						msg = "Image size should be less than 200Kb.";
					}
					else {	
						String uploadPath = System.getProperty("user.dir")+ "/src/main/resources/static/employee-images/";

						if (dbname != null && !dbname.isEmpty()) {

						    File oldFile = new File(uploadPath + dbname);
						    if (oldFile.exists()) {
						        if (oldFile.delete()) {
						            msg = "Old image deleted successfully";
						        } 
						        else {
						            msg = "Unable to delete old image";
						        }
						    }
						    else {
						        msg = "Old image not found";
						    }						    
						    fname=dbname;						
							String destpath = folderpath+"/"+fname;						
							File ff = new File(destpath);						
							profilepic.transferTo(ff);							
							String sql1 = "UPDATE employeemaster SET mobno = '"+er.getMobno()+"', pic_file_name = '"+fname+"' WHERE empid= '"+userid+"'";
							int n = jTemp.update(sql1);
								if(n>0) {
									msg="Profile updated successfully";
								}
								else {
									msg="Profile couldn't be updated.";
								}
							} 						    
						}
						
					}
				}
				else {
					msg="invalid file type";
				}
		}
		catch(Exception e){
			msg="Intrucption Countered : " + e;
		}	    
	    ra.addFlashAttribute("msg",msg);
	    return "redirect:/updateprofile";
	}
	
	
	@GetMapping("/changepass")
	public String passwordchange() {
		
		return "employee/password";
	}
	
	@PostMapping("/changeemppass")
	public String changepassword(RedirectAttributes rd, String cpass, String npass, String cfpass, HttpSession session) {
		String msg="";
		try {
			String sa = session.getAttribute("EmpSession").toString();
			EncryptionManager  em = new EncryptionManager();
			String epass=em.passwordEncrypt(cpass);
			String sql="SELECT COUNT(*) FROM loginmasters WHERE userid='"+sa+"' && password='"+epass+"'";
			int r = jTemp.queryForObject(sql, Integer.class);
			if(r>0) {
				if(npass.equals(cfpass)) {
					if(cpass.equals(npass)) {
						msg="New password must be diffrent from current password";
					}
					else {
						String newencrpass=em.passwordEncrypt(npass);
						String sql1="UPDATE loginmasters SET password = '"+newencrpass+"' WHERE userid='"+sa+"'";
						int rr= jTemp.update(sql1);
						if(rr > 0) {
							msg="Password updated successfully.";
						}
						else {
							msg="Password can't be changed";
						}
					}
				}
				else {
					msg="New password and Confirm Password didn't matched.";
				}
			}
			else {
				msg="Invalid EmpId or password";
			}
		}
		catch(Exception e) {
			msg="Technical issue occured : " + e;
		}
		
		rd.addFlashAttribute("msg", msg);
		return "redirect:/changepass";
	}
}
