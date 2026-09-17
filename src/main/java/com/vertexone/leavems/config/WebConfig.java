package com.vertexone.leavems.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.vertexone.leavems.authorization.AdminAuthorization;
import com.vertexone.leavems.authorization.EmployeeAuthorization;

@Configuration
public class WebConfig implements WebMvcConfigurer{

	@Autowired
	AdminAuthorization adminAuthorization;
	@Autowired
	EmployeeAuthorization employeeAuthorization;
	
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		
		registry.addInterceptor(adminAuthorization).addPathPatterns(
				"/admindashboard",
				"/addholiday", 
				"/manageleave", 
				"/manageemployee",
				"/allowleave",
				"/feedadmin",
				"/enq",
				"/notify",
				"/cngpass"
				);
		registry.addInterceptor(employeeAuthorization).addPathPatterns(
				"/empdashboard",
				"/leaveapplication", 
				"/leavebal", 
				"/calendar",
				"/feedbac",
				"/updateprofile",
				"/changepass",
				"/logout"
				);
	}
	
}
