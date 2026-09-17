package com.vertexone.leavems.authorization;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class EmployeeAuthorization implements HandlerInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handeler) throws Exception{
		
		HttpSession session = request.getSession(false);
		
		if(session == null || session.getAttribute("EmpSession") == null) {
			response.sendRedirect("/emplogin");
			return false;
		}
		return true;
	}
}
