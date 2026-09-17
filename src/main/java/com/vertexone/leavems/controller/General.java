package com.vertexone.leavems.controller;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vertexone.leavems.encryption.EncryptionManager;
import com.vertexone.leavems.model.AllowedLeave;
import com.vertexone.leavems.model.Contact;
import com.vertexone.leavems.model.EmployeeReg;
import com.vertexone.leavems.model.Enquiry;
import com.vertexone.leavems.model.LeaveBalance;
import com.vertexone.leavems.services.EmailService;
import com.vertexone.leavems.utility.CaptchaCodeGenerator;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class General {
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
	@Autowired
	JdbcTemplate jTemp;
	@Autowired
	EmailService eservice;
	
	public List<String> getNotification(){
		String sql ="SELECT message FROM notifymaster ORDER BY nid LIMIT 3";
		List<String> ls = jTemp.queryForList(sql, String.class);
		return ls;
	}
	@GetMapping("/home")
	public String homepage(Model m) {
		m.addAttribute("lst",getNotification()); // this can be used to decide at which page we have to 
		//eservice.sendEmail("suryaprataps2024@gmail.com", "Bablu is dead", "Babli is getting the divine punishment. :((");
		return "general/index";
	}
	
	@GetMapping("/flow")
	public String workflowpage() {
		return "general/workflow";
	}
	
	@GetMapping("/pics")
	public String gallerypage() {
		return "general/gallery";
	}
	
	@GetMapping("/policy")
	public String policypage() {
		return "general/policies";
	}
	
	@GetMapping("/contact")
	public String contactpage(Model m) {
		m.addAttribute("lst",getNotification());
		return "general/contact";
	}
	@PostMapping("/contsend")
	public String conta(RedirectAttributes ra, @ModelAttribute Contact c ) {
		String msg="";
		
		try {
			String sql = "INSERT INTO contactmaster(name, mobno, email, message) VALUES('"+c.getName()+"', '"+c.getMobno()+"', '"+c.getEmail()+"', '"+c.getMessage()+"')";
			
			int res = jTemp.update(sql);
			
			if(res > 0) {
				msg="Thank you for your support ! We will contact you very soon.";
			}
			else {
				msg="Contact service is unavilable right now.";
			}
		}
		catch(Exception ex) {
			msg="Techincal Issue Occured : " + ex;
		}
		
		ra.addFlashAttribute("msg",msg);
		
		return "redirect:/contact";
	}
	
	@GetMapping("/register")
	public String registerpage(Model m) {
		m.addAttribute("lst",getNotification());
		return "general/registration";
	}
	
	@PostMapping("/employee-registration")
	public String registerpage(@ModelAttribute EmployeeReg er, RedirectAttributes rd,HttpSession session,String pass, String confPass, String captchacode) {
		System.out.println("Controller reached");
		String msg="";
		try {
			String imgcode = session.getAttribute("cacode").toString(); 			
			if(imgcode.equals(captchacode)) {
				if(er.getProfilepic().isEmpty()) {
					msg="Please upload the profile pic.Image should be in (png,jpg or jpeg)format and size should be less than 200Kb.";
				}
				else {
					if(pass.equals(confPass)) {//Password checking successfull
						
						/* File Uploading */
						
						String folderpath = System.getProperty("user.dir")+"/src/main/resources/static/employee-images/";
						File dir = new File(folderpath);
						
						if(!dir.exists()) {
							dir.mkdir();			//-> Creating the directory if it doesnt exists.
						}
												
						String filename = er.getProfilepic().getOriginalFilename();
						long sizeInKb = er.getProfilepic().getSize()/1024;
						String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase();
						
						if(extension.equals(".png") || extension.equals(".jpg") || extension.equals(".jpeg")) {							
							if(sizeInKb > 200) {
								msg = "Image size should be less than 200Kb.";
							}
							else {
								
								/* Changing the file name to prevent same name type files */
								EncryptionManager em = new EncryptionManager();
								String fname = filename.substring(0,filename.lastIndexOf('.'));
								String encryptedfname = em.fileNameChanger(fname);
								String newFilename = encryptedfname.concat(extension);
								
								File destination = new File(folderpath+newFilename);
								er.getProfilepic().transferTo(destination); 				// file is now stored to the folder.
								
								String sql = "INSERT INTO employeemaster(empid,fullname,age,gender,pic_file_name,mobno,empgrade,designation,joiningdate) VALUES ('"+er.getEmpid()+"', '"+er.getFullname()+"', '"+er.getAge()+"', '"+er.getGender()+"','"+newFilename+"', '"+er.getMobno()+"','"+er.getEmpgrade()+"','"+er.getDesignation()+"','"+er.getJoiningdate()+"')";
								int rr = jTemp.update(sql);
								if(rr > 0) {
									pass = em.passwordEncrypt(pass);
									String userType = "Employee";
									
									String sqlLogin = "INSERT INTO loginmasters VALUES('"+er.getEmpid()+"','"+pass+"', '"+userType+"')";
									
									int result = jTemp.update(sqlLogin);
									if(result > 0) {
										String cur_ses = getCurrentYearSession();										
								    	AllowedLeave al = jTemp.queryForObject("SELECT * FROM allowedleavemaster WHERE empgrade = '"+er.getEmpgrade()+"' AND session = '"+cur_ses+"'", new BeanPropertyRowMapper<>(AllowedLeave.class)); 				    	
								    	if(er.getGender().trim().equals("Male")) {
								    		String intialSql = "INSERT INTO leavebalancemaster (empid,session,usedcl,remainingcl,usedml,remainingml,usedel,remainingel,usedccl,reamainingccl,usedmaternityleave,remaingmaternityleave,usedrh,remainingrh)VALUES('"+er.getEmpid()+"', '"+cur_ses+"','"+0+"','"+al.getAllowedcl()+"', '"+0+"', '"+al.getAllowedml()+"','"+0+"','"+al.getAllowedel()+"','"+0+"','"+al.getAllowedccl()+"','"+0+"','"+0+"','"+0+"', '"+al.getAllowedrh()+"')";
								    		int r = jTemp.update(intialSql); // Updating the row
								    		if(r > 0) {
								    			msg="Registration done successfully";
									    		
									    		String emailMsg ="Dear " + er.getFullname() + ",\n\n" +"Welcome to VertexOne Leave Management System!\n\n" +
												        "Your employee account has been successfully registered. " +
												        "You can now use the Leave Management System to apply for leave, " +
												        "check your leave balance, track leave applications, view holidays, " +
												        "and manage your employee profile.\n\n" +
												        "YOUR ACCOUNT DETAILS\n" +
												        "--------------------\n" +
												        "Account login id : "+er.getEmpid()+"\n"+
												        "Designation : " +er.getDesignation()+ "\n" +
												        "Grade       : " +er.getEmpgrade()+ "\n\n" +
												        "IMPORTANT\n" +
												        "For security reasons, please do not share your password with anyone.\n\n" +
												        "You can access your employee portal using the login option available " +
												        "on the VertexOne Leave Management System website.\n\n" +
												        "If you did not create this account, please contact the " +
												        "HR/Administration team immediately.\n\n" +
												        "Regards,\n" +
												        "HR & Administration Team\n" +
												        "VertexOne\n" +
												        "Leave Management System";
												
														eservice.sendEmail(er.getEmpid(), "Welcome to VertexOne Leave Management System", emailMsg);
								    		}
								    		
								    	}
								    	else {
								    		String intialSql = "INSERT INTO leavebalancemaster (empid,session,usedcl,remainingcl,usedml,remainingml,usedel,remainingel,usedccl,reamainingccl,usedmaternityleave,remaingmaternityleave,usedrh,remainingrh)VALUES('"+er.getEmpid()+"', '"+cur_ses+"','"+0+"','"+al.getAllowedcl()+"', '"+0+"', '"+al.getAllowedml()+"','"+0+"','"+al.getAllowedel()+"','"+0+"','"+al.getAllowedccl()+"','"+0+"','"+al.getAllowedmaternityleave()+"','"+0+"', '"+al.getAllowedrh()+"')";
								    		int r = jTemp.update(intialSql); // Updating the row
								    		if(r > 0) {
								    			msg="Registration done successfully";
									    		
									    		String emailMsg ="Dear " + er.getFullname() + ",\n\n" +"Welcome to VertexOne Leave Management System!\n\n" +
												        "Your employee account has been successfully registered. " +
												        "You can now use the Leave Management System to apply for leave, " +
												        "check your leave balance, track leave applications, view holidays, " +
												        "and manage your employee profile.\n\n" +
												        "YOUR ACCOUNT DETAILS\n" +
												        "--------------------\n" +
												        "Account login id : "+er.getEmpid()+"\n"+
												        "Designation : " +er.getDesignation()+ "\n" +
												        "Grade       : " +er.getEmpgrade()+ "\n\n" +
												        "IMPORTANT\n" +
												        "For security reasons, please do not share your password with anyone.\n\n" +
												        "You can access your employee portal using the login option available " +
												        "on the VertexOne Leave Management System website.\n\n" +
												        "If you did not create this account, please contact the " +
												        "HR/Administration team immediately.\n\n" +
												        "Regards,\n" +
												        "HR & Administration Team\n" +
												        "VertexOne\n" +
												        "Leave Management System";
												
														eservice.sendEmail(er.getEmpid(), "Welcome to VertexOne Leave Management System", emailMsg);
								    		}
								    		
								    	}	
										rd.addFlashAttribute("msg",msg);
										return "redirect:/emplogin";										
									}
									else {
										msg="Registration can't be done due to technical issue !";
									}									
								}																
							}
						}
						else {
							msg="Image format should only be .png, .jpg or .jpeg";
						}						
					}
					else {
						msg=" Password and Confirm Password should be same.";
					}
				}				
			}
			else{
				msg="Captcha code didn't matched.";
			}
		}
		catch(Exception e) {
			msg="Exception occured : "+e;
		}
		
		rd.addFlashAttribute("msg", msg);
		return "redirect:/register";
	}
	
	@GetMapping("/emplogin")
	public String emploginpage(Model m) {
		m.addAttribute("lst",getNotification());
		
		return "general/emplogin";
	}
	
	@GetMapping("/adminlogin")
	public String adminloginpage(Model m) {
		m.addAttribute("lst",getNotification());
		return "general/adminlogin";
	}
	
	@PostMapping("/login")
	public String loginattribute(RedirectAttributes rd, HttpSession session, String type, String userid, String password) {
		
		String msg="";
		
		EncryptionManager em = new EncryptionManager();
		password = em.passwordEncrypt(password);
		if(type.equals("Employee")) {
			
			
			try {
				String sql = "SELECT COUNT(*) FROM loginmasters WHERE userid='"+userid+"' && password = '"+password+"' && type = '"+type+"'";
				int res = jTemp.queryForObject(sql, Integer.class);
				
				if(res > 0) {
					session.setAttribute("EmpSession", userid);
					String sql2 = "SELECT * FROM employeemaster WHERE empid = '"+userid+"'";
					EmployeeReg emp = jTemp.queryForObject(sql2, new BeanPropertyRowMapper<>(EmployeeReg.class));
					
					session.setAttribute("emp", emp);
					/*session.setAttribute("uname", emp.getFullname());
					String folderpath = "employee-images/";
					session.setAttribute("upic", folderpath+emp.getPic_file_name());*/
														//so that i have session here.
					return "redirect:/empdashboard";
				}
				else {
					msg="Invalid EmployeeId or password.";					
				}
			}
			catch(Exception e) {
				msg = "Technical issue occured : " + "The requested User doesnt found!";
			}
			rd.addFlashAttribute("msg",msg);
			return "redirect:/emplogin";			
		}
		else if(type.equals("Admin")){
			
			try {
				String sql = "SELECT COUNT(*) FROM loginmasters WHERE userid='"+userid+"' && password = '"+password+"' && type = '"+type+"'";
				
				int res = jTemp.queryForObject(sql, Integer.class);
				if(res > 0) {
					session.setAttribute("AdminSession", userid);
					return "redirect:/admindashboard";
					
				}
				else {
					msg="Invalid AdminId or password.";					
				}
			}
			catch(Exception e) {
				msg = "Technical issue occured : " + e;
			}			
		}
		rd.addFlashAttribute("msg",msg);
		return "redirect:/adminlogin";
	}
	
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.removeAttribute("EmpSession");
		return "redirect:/emplogin";
	}
	
	@GetMapping("/logouts")
	public String logouts(HttpSession session) {
		session.removeAttribute("AdminSession");
		return "redirect:/adminlogin";
	}	
	
	@GetMapping("/captcha")
	public void getCaptcha(HttpServletResponse response, HttpSession session) {// here the captcha code location , so that we can access it directly for various pages
		try {
			
			CaptchaCodeGenerator ccg = new CaptchaCodeGenerator();
			String code = ccg.captchaCodeGenerator();
			session.setAttribute("cacode", code);
			
			BufferedImage bi = new BufferedImage(150,40,BufferedImage.TYPE_INT_RGB);
			Graphics g = bi.getGraphics();
			g.setColor(Color.GRAY);
			g.fillRect(0, 0, 150, 40);
			g.setColor(Color.RED);
			g.setFont(new Font("Arial", Font.BOLD, 28));
			g.drawString(code, 20, 30);
			response.setContentType("images/jpeg");
			ImageIO.write(bi,"jpg",response.getOutputStream());
			g.dispose();
			
		}catch(Exception e) {
			System.out.println(e);
		}
	}
	
	@PostMapping("/enqsend")
	public String enquirysent(@ModelAttribute Enquiry e, RedirectAttributes rd) {
		String msg="";
		
		try {
			String sql = "INSERT INTO enquirymasters(name, mobno, email, topic, querymsg) VALUES('"+e.getName()+"', '"+e.getMobno()+"', '"+e.getEmail()+"', '"+e.getTopic()+"', '"+e.getQuerymsg()+"')";
			
			int res = jTemp.update(sql);
			
			if(res > 0) {
				msg="Thank you for your enquiry ! We will contact you very soon.";
			}
			else {
				msg="Enquiry service is unavilable right now.";
			}
		}
		catch(Exception ex) {
			msg="Techincal Issue Occured : " + ex;
		}
		
		rd.addFlashAttribute("msg",msg);
		return "redirect:/home";
	}
	
	@GetMapping("/devs")
	public String devs() {
		return "general/developer";
	}
}

