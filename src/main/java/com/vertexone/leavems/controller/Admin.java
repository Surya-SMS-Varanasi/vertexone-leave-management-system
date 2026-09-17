package com.vertexone.leavems.controller;

import java.io.File;
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
import com.vertexone.leavems.model.Contact;
import com.vertexone.leavems.model.EmployeeReg;
import com.vertexone.leavems.model.Enquiry;
import com.vertexone.leavems.model.Feedback;
import com.vertexone.leavems.model.Holiday;
import com.vertexone.leavems.model.LeaveApplication;
import com.vertexone.leavems.model.LeaveBalance;
import com.vertexone.leavems.model.Notification;
import com.vertexone.leavems.services.EmailService;

import jakarta.servlet.http.HttpSession;


	
@Controller
public class Admin {
	
	@Autowired
	JdbcTemplate jTemp;
	
	@Autowired
	EmailService eservice;
	
	@GetMapping("/admindashboard")
	public String dashboard(Model md) {
		md.addAttribute("totalemp",jTemp.queryForObject("SELECT COUNT(*) FROM employeemaster", Integer.class));
		md.addAttribute("totalcont",jTemp.queryForObject("SELECT COUNT(*) FROM contactmaster", Integer.class));
		md.addAttribute("totalenq",jTemp.queryForObject("SELECT COUNT(*) FROM enquirymasters", Integer.class));
		md.addAttribute("totalfeed",jTemp.queryForObject("SELECT COUNT(*) FROM feedmaster", Integer.class));
		md.addAttribute("totalnoti",jTemp.queryForObject("SELECT COUNT(*) FROM notifymaster", Integer.class));
		md.addAttribute("totalholi",jTemp.queryForObject("SELECT COUNT(*) FROM holidaymaster", Integer.class));
		md.addAttribute("totalleave",jTemp.queryForObject("SELECT COUNT(*) FROM leaveapplicationmaster", Integer.class));
		md.addAttribute("appliedleave",jTemp.queryForObject("SELECT COUNT(*) FROM leavebalancemaster", Integer.class));
		return "admin/dashboard";
	}
	
	@GetMapping("/addholiday")
	public String addHoliday() {
		
		return "admin/addholiday";
	}
	@PostMapping("/upcalender")
	public String uploadClend(MultipartFile file, RedirectAttributes ra) {
		String msg="cmm";
		try {
			if(file.isEmpty()) {
				msg="File is empty, Please upload the calender.";
			}
			else {
				String filename = file.getOriginalFilename();
				String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase();
				
				EncryptionManager em = new EncryptionManager();
				String fname = filename.substring(0,filename.lastIndexOf('.'));
				String encryptedfname = em.fileNameChanger(fname);
				String newFilename = encryptedfname.concat(extension);
				
				String uploadDir = System.getProperty("user.dir")+"/src/main/resources/static/holiday-calendar/";
				
				File dir = new File(uploadDir);
		        if (!dir.exists()) {
		            dir.mkdirs();
		        }
		        else {
		        	File destination = new File(uploadDir + newFilename);
		            file.transferTo(destination);
		            
		            String sql = "INSERT INTO calendermaster(imgname) VALUES ('"+newFilename+"')";
		            int res = jTemp.update(sql);
		            if(res>0) {
		            	msg="Calender uploaded successfully!";
		            }
		            else {
		            	msg="Sql injection error!";
		            }
		        }
			}
		}
		catch(Exception e) {
			msg="Technical Issue ouucred : " + e;
		}
		
		ra.addFlashAttribute("msg",msg);
		return "redirect:/addholiday";
	}
	@PostMapping("/adholi")
	public String addholidays(RedirectAttributes ra, @ModelAttribute Holiday h ) {
		String msg="";
		
		try {
			String sql = "INSERT INTO holidaymaster(hddate,hdname,session) VALUES ('"+h.getHddate()+"','"+h.getHdname()+"','"+h.getSession()+"')";
			int rr = jTemp.update(sql);
			if(rr>0) {
				msg="Holiday added successfully";
			}
			else {
				msg="SQL Injection error.";
			}
			
		}
		catch(Exception e) {
			msg="Techincal issue occured !"+e;
		}
		ra.addFlashAttribute("msg",msg);
		return "redirect:/addholiday";
	}
	
	@GetMapping("/manageleave")
	public String manageLeave(@ModelAttribute LeaveApplication la, Model md) {
		String sql = "SELECT  l.*, e.fullname, e.empgrade, e.designation, e.pic_file_name, k.status FROM leaveapplicationmaster l INNER JOIN employeemaster e ON l.empid = e.empid INNER JOIN leavetrackingmaster k ON l.leaveid = k.leaveid ORDER BY l.leaveid DESC";
		List<LeaveApplication> lst = jTemp.query(sql, new BeanPropertyRowMapper<>(LeaveApplication.class));
		
		int num = jTemp.queryForObject("SELECT COUNT(*) FROM leavetrackingmaster WHERE status = 'pending'", Integer.class);
		md.addAttribute("ldet",lst);
		md.addAttribute("pending",num);
		return "admin/leavemng";
	}
	@GetMapping("/approveleave")
		public String approve(String leaveid, RedirectAttributes ra) {
			String msg="";
			try {
				String status = jTemp.queryForObject("SELECT status FROM leavetrackingmaster WHERE trackid = (SELECT trackid FROM leavetrackingmaster WHERE leaveid = '"+leaveid+"')", String.class);
				msg=""+status+" "+leaveid;
				
				if(status.equals("Approved") || status.equals("Rejected")) {
					msg="Leave is either Rejected or Approved already !";
					ra.addFlashAttribute("msg",msg);
					return "redirect:/manageleave";
				}
				
          		String sql = "SELECT fullname FROM employeemaster WHERE empid=(SELECT empid FROM leaveapplicationmaster WHERE leaveid = '"+leaveid+"')";
				String name = jTemp.queryForObject(sql,String.class);
				String sql1 = "SELECT leavetype, empid, fromdate, todate,noofdays, reason FROM leaveapplicationmaster WHERE leaveid = '"+leaveid+"'";
				LeaveApplication la = jTemp.queryForObject(sql1, new BeanPropertyRowMapper<>(LeaveApplication.class));
				System.out.println(la);
				String value = la.getLeavetype();
				
				System.out.println(la.getNoofdays());
				
				String sql2="SELECT * FROM leavebalancemaster WHERE empid=(SELECT empid FROM leaveapplicationmaster WHERE leaveid ='"+leaveid+"')";
				List<LeaveBalance> lbm_Lst=jTemp.query(sql2,new BeanPropertyRowMapper<>(LeaveBalance.class));
				LeaveBalance lbm=lbm_Lst.get(0);
				
				if(value.trim().equals("Casual-leave"))
				{
					
					lbm.setUsedcl(la.getNoofdays());					
					lbm.setRemainingcl(lbm.getRemainingcl() - la.getNoofdays());					
				
				}
				else if(value.trim().equals("Medical-leave"))
				{					
					lbm.setUsedml(la.getNoofdays());
					lbm.setRemainingml(lbm.getRemainingml() - la.getNoofdays());				
				}
				else if(value.trim().equals("Earned-leave"))	
				{
					lbm.setUsedel(la.getNoofdays());
					lbm.setRemainingel(lbm.getRemainingel() - la.getNoofdays());									
				}
				else if(value.trim().equals("Child-care-leave"))	
				{					
					lbm.setUsedccl(la.getNoofdays());
					lbm.setReamainingccl(lbm.getReamainingccl() - la.getNoofdays());								
				}
				else if(value.trim().equals("Maternity-leave"))	
			{
					lbm.setUsedmaternityleave(la.getNoofdays());
					lbm.setRemaingmaternityleave(lbm.getRemaingmaternityleave() - la.getNoofdays());			
				}
				else if(value.trim().equals("Restricted-holiday"))
				{
					lbm.setUsedrh(la.getNoofdays());
					lbm.setRemainingrh(lbm.getRemainingrh() - la.getNoofdays());
				}
				
				String myCommand ="UPDATE leavebalancemaster SET usedcl = '"+lbm.getUsedcl()+"',remainingcl = '"+lbm.getRemainingcl()+"',usedml = '"+lbm.getUsedml()+"',remainingml = '"+lbm.getRemainingml()+"',usedel = '"+lbm.getUsedel()+"',remainingel = '"+lbm.getRemainingel()+"',usedccl = '"+lbm.getUsedccl()+"',reamainingccl = '"+lbm.getReamainingccl()+"',usedmaternityleave = '"+lbm.getUsedmaternityleave()+"',remaingmaternityleave = '"+lbm.getRemaingmaternityleave()+"',usedrh = '"+lbm.getUsedrh()+"',remainingrh = '"+lbm.getRemainingrh()+"' WHERE empid = (SELECT empid FROM leaveapplicationmaster WHERE leaveid ='"+leaveid+"')";;
				System.out.println(myCommand);
				
				int res = jTemp.update(myCommand);
				
				String myCommand2 = "UPDATE leavetrackingmaster SET status = 'Approved' , remarks = 'Admin has approved the leave and approved message has been sent to the employee.' WHERE trackid=(SELECT trackid FROM leavetrackingmaster WHERE leaveid = '"+leaveid+"')";
				int res2 = jTemp.update(myCommand2);
				System.out.println(myCommand2);
				System.out.println(res2);
				if(res > 0 && res2 > 0) {
					msg="Leave Granted.";
					msg="Leave has been approved for Empid = "+ la.getEmpid();
					//put the email code here....
					String emailMsg =
					        "Dear " + name + ",\n\n" +

					        "We are pleased to inform you that your leave application has been approved.\n\n" +

					        "LEAVE DETAILS\n" +
					        "--------------------\n" +
					        "Leave Type  : " + la.getLeavetype() + "\n" +
	       			        "From Date   : " + la.getFromdate() + "\n" +
					        "To Date     : " + la.getTodate() + "\n" +
					        "Total Days  : " + la.getNoofdays()+ "\n" +
					        "Reason : " + la.getReason() + "\n\n" +

					        "Your leave has been successfully recorded in the " +
					        "VertexOne Leave Management System.\n\n" +

					        "Please ensure that you complete any necessary work " +
					        "handover before proceeding on leave.\n\n" +

					        "We wish you a pleasant leave.\n\n" +

					        "Regards,\n" +
					        "HR & Administration Team\n" +
					        "VertexOne\n" +
							"Leave Management System\n\n";
					eservice.sendEmail(la.getEmpid(),"Leave Application Approved – VertexOne Leave Management System", emailMsg);
				}
				else {
					msg="injection error";
				}
				
				
			}
			catch(Exception e) {
				msg="Techincal Issue Occured !"+e;
			}
			ra.addFlashAttribute("msg",msg);
			return "redirect:/manageleave";
		}
	@GetMapping("/declineleave")
	public String decline(String leaveid, RedirectAttributes ra) {
		String msg="";
		
		try {
			
			String status = jTemp.queryForObject("SELECT status FROM leavetrackingmaster WHERE trackid = (SELECT trackid FROM leavetrackingmaster WHERE leaveid = '"+leaveid+"')", String.class);
			
			if(status.equals("Approved") || status.equals("Rejected")) {
				msg="This action can't be overwrite !";
				ra.addFlashAttribute("msg",msg);
				return "redirect:/manageleave";
			}
			String sql = "SELECT fullname FROM employeemaster WHERE empid=(SELECT empid FROM leaveapplicationmaster WHERE leaveid = '"+leaveid+"')";
			String name = jTemp.queryForObject(sql,String.class);
			String sql1 = "SELECT leavetype, empid, fromdate, todate,noofdays, reason FROM leaveapplicationmaster WHERE leaveid = '"+leaveid+"'";
			LeaveApplication la = jTemp.queryForObject(sql1, new BeanPropertyRowMapper<>(LeaveApplication.class));
			System.out.println(la);
			int res = jTemp.update("UPDATE leavetrackingmaster SET status = 'Rejected', remarks ='Due to insufficent documents or invalid reasons as per company policy.' WHERE trackid =(SELECT trackid FROM leavetrackingmaster WHERE leaveid = '"+leaveid+"')");
			if(res > 0) {
				String reason = jTemp.queryForObject("SELECT remarks FROM leavetrackingmaster WHERE trackid=(SELECT trackid FROM leavetrackingmaster WHERE leaveid = '"+leaveid+"')", String.class);
				msg = "Leave rejected ! ";
				
				String emailMsg =
				        "Dear " + name + ",\n\n" +
				        "We regret to inform you that your leave application has been rejected.\n\n" +
				        "LEAVE DETAILS\n" +
				        "--------------------\n" +
				        "Leave Type : " + la.getLeavetype() + "\n" +
				        "From Date : " + la.getFromdate() + "\n" +
				        "To Date : " + la.getTodate() + "\n" +
				        "Total Days : " + la.getNoofdays() + "\n" +
				        "Reason : " + reason + "\n\n" +
				        "Your leave application has been reviewed by the " +
				        "HR & Administration Team and has not been approved.\n\n" +
				        "If you require further clarification regarding this decision, " +
				        "please contact the HR & Administration Team.\n\n" +
				        "Regards,\n" +
				        "HR & Administration Team\n" +
				        "VertexOne\n" +
				        "Leave Management System\n\n";

				eservice.sendEmail(la.getEmpid(),"Leave Application Rejected – VertexOne Leave Management System",emailMsg);
			}
			else {
				msg = "Leave can't be rejected ! ";
			}
		}
		catch(Exception e) {
			msg="Techincal Issue Occured !"+e;
		}
		ra.addFlashAttribute("msg",msg);
		return "redirect:/manageleave";
	}
	@GetMapping("/manageemployee")
	public String manageEmployee(@ModelAttribute EmployeeReg rg, Model md) {
		String sql = "SELECT * FROM employeemaster";
		List<EmployeeReg> lst = jTemp.query(sql, new BeanPropertyRowMapper<>(EmployeeReg.class));
		md.addAttribute("list",lst);
		return "admin/employeemng";
	}
	@GetMapping("/edit")
	public String editemployee(String id, Model md) {
		System.out.println("String : "+ id);
		String sql = "SELECT * FROM employeemaster WHERE empid = '"+id+"'";

	    EmployeeReg employee = jTemp.queryForObject(sql,new BeanPropertyRowMapper<>(EmployeeReg.class));

	    md.addAttribute("employee", employee);

		return "admin/editemployee";
	}
	@PostMapping("/updateemployee")
	public String updateemployee(RedirectAttributes ra, @ModelAttribute EmployeeReg er) {
		String msg="";
		try {
			String sql = "UPDATE employeemaster SET fullname = '"+er.getFullname()+"', age='"+er.getAge()+"', gender='"+er.getGender()+"', empgrade='"+er.getEmpgrade()+"', designation = '"+er.getDesignation()+"', joiningdate='"+er.getJoiningdate()+"' WHERE empid = '"+er.getEmpid()+"'";
			int res = jTemp.update(sql);
			if(res>0)
				msg="record updated successfully.";
			else
				msg="Sql injection error !";
		}
		catch(Exception e) {
			msg="Record cant be updated : " + e;
		}
	    ra.addFlashAttribute("msg", msg);
		return "redirect:/manageemployee";
	}
	@GetMapping("/deleteemp")
	public String updateemployee(RedirectAttributes ra, String empid) {
		String msg="";
		try {
			String sql ="DELETE FROM employeemaster WHERE empid = '"+empid+"'";
			String sql2 ="DELETE FROM loginmasters WHERE userid = '"+empid+"'";
			int res = jTemp.update(sql);
			int result = jTemp.update(sql2);
			if(res>0 && result > 0) {
				msg="Employee removed successfully.";
			}
			else
				msg="Sql injection error !";
		}
		catch(Exception e) {
			msg="Record cant be deleted : " + e;
		}
	    ra.addFlashAttribute("msg", msg);
		return "redirect:/manageemployee";
	}
	
	@GetMapping("/allowleave")
	public String allotLeave() {
		return "admin/allotleave";
	}
	
	@PostMapping("/leavesection")
	public String allotLeave(@ModelAttribute AllowedLeave al, RedirectAttributes rd) {
		String msg="";
		
		try {
			String sql2 = "SELECT COUNT(*) FROM allowedleavemaster WHERE empgrade='"+al.getEmpgrade()+"' && session='"+al.getSession()+"'";
			int checking = jTemp.queryForObject(sql2,Integer.class);
			
			if(checking > 0) {
				msg="The leaves are already allocated for the "+ al.getEmpgrade() + " employees for session " + al.getSession()+".";
			}
			else {
				String sql = "INSERT INTO allowedleavemaster(empgrade,allowedrh,allowedccl,allowedcl,allowedel,allowedmaternityleave,allowedml,session)VALUES('"+al.getEmpgrade()+"','"+al.getAllowedrh()+"','"+al.getAllowedccl()+"','"+al.getAllowedcl()+"','"+al.getAllowedel()+"','"+al.getAllowedmaternityleave()+"','"+al.getAllowedml()+"', '"+al.getSession()+"')";
				
				int res = jTemp.update(sql);
				if(res > 0 ) {
					msg="Leaves allocated to " + al.getEmpgrade() + " employees for session " + al.getSession()+".";
				}
				else{
					msg="Leaves can't be allocated due to Injection error!";
				}
			}			
		}
		catch(Exception e) {
			msg="Technical issue occured : "+e;
		}
		rd.addFlashAttribute("msg",msg);
		return "redirect:/allowleave";
	}
	
	
	@GetMapping("/feedadmin")
	public String feedback(Model m) {
		String sql = "SELECT f.*, e.fullname, e.age, e.gender, e.pic_file_name, e.mobno, e.empgrade, e.designation FROM feedmaster f INNER JOIN employeemaster e ON f.empid = e.empid ORDER BY f.feedid DESC";
		
		List<Feedback> flist = jTemp.query(sql, new BeanPropertyRowMapper<>(Feedback.class));
		m.addAttribute("list", flist);
		return "admin/feedback";
	}
	@GetMapping("/deletefeed")
	public String feedback(RedirectAttributes rd, int id) {
		String msg="";
		try {
			String sql="DELETE FROM feedmaster WHERE feedid = '"+id+"'";
			
			int res = jTemp.update(sql);
			if(res > 0) {
				msg = "Enquiry deleted Successfully";
			}
			else {
				msg="Enquiry is not present.";
			}			
		}
		catch(Exception em) {
			msg="Database Issue Occured : " + em;
		}
		
		rd.addFlashAttribute("msg",msg);
		return "redirect:/feedadmin";
	}
	
	@GetMapping("/enq")
	public String enquiries(Model m) {
		
		String sql = "SELECT * FROM enquirymasters ORDER BY enqid DESC";
		
		List<Enquiry> enqlist = jTemp.query(sql, new BeanPropertyRowMapper<>(Enquiry.class));
		m.addAttribute("list", enqlist);
		
		return "admin/enquiry";
	}
	@GetMapping("/delenq")
	public String enquiries(RedirectAttributes rd, int id) {
		String msg="";
		try {
			String sql="DELETE FROM enquirymasters WHERE enqid = '"+id+"'";
			
			int res = jTemp.update(sql);
			if(res > 0) {
				msg = "Enquiry deleted Successfully";
			}
			else {
				msg="Enquiry is not present.";
			}			
		}
		catch(Exception em) {
			msg="Database Issue Occured : " + em;
		}
		
		rd.addFlashAttribute("msg",msg);
		return "redirect:/enq";
	}
	
	@GetMapping("/notify")
	public String notification(Model m) {
		String sql = "SELECT * FROM notifymaster ORDER BY nid DESC";
		
		List<Notification> list = jTemp.query(sql, new BeanPropertyRowMapper<>(Notification.class));
		m.addAttribute("nlist",list);
		return "admin/notification";
	}
	
	@PostMapping("/notifies")
	public String noticeboard(RedirectAttributes rd, String message) {
		String msg="";
		try {
			String sql = "INSERT INTO notifymaster(message) VALUES('"+message+"')";
			int res = jTemp.update(sql);
			if(res > 0) {
				msg="Notification Sent";
			}
			else {
				msg="Sorry ! notification didn't added";
			}
		}
		catch(Exception e) {
			msg="Technical issue occured : " + e;
		}
		rd.addFlashAttribute("msg",msg);
		return "redirect:/notify";
	}
	
	@GetMapping("/delnoti")
	public String notification(RedirectAttributes rd, int id) {
		String msg="";
		try {
			String sql="DELETE FROM notifymaster WHERE nid = '"+id+"'";
			
			int res = jTemp.update(sql);
			if(res > 0) {
				msg = "Notification deleted Successfully";
			}
			else {
				msg="Notification was not present.";
			}			
		}
		catch(Exception em) {
			msg="Database Issue Occured : " + em;
		}
		
		rd.addFlashAttribute("msg",msg);
		return "redirect:/notify";
	}
	
	@GetMapping("/cont")
	public String cont(Model m) {
		String sql = "SELECT * FROM contactmaster ORDER BY conid DESC";
		
		List<Contact> enqlist = jTemp.query(sql, new BeanPropertyRowMapper<>(Contact.class));
		m.addAttribute("list", enqlist);
		return"admin/contact";
	}
	
	@GetMapping("/cngpass")
	public String change() {
		
		return "admin/change";
	}	
	@PostMapping("/changemypass")
	public String changepassword(RedirectAttributes rd, String cpass, String npass, String cfpass, HttpSession session) {
		String msg="";
		try {
			String sa = session.getAttribute("AdminSession").toString();
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
				msg="Invalid userId or password";
			}
		}
		catch(Exception e) {
			msg="Technical issue occured : " + e;
		}
		
		rd.addFlashAttribute("msg", msg);
		return "redirect:/cngpass";
	}	
}
