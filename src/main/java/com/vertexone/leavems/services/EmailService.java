package com.vertexone.leavems.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@Service
public class EmailService {
	@Autowired
	JavaMailSender esender;
	
	public void sendEmail(String sendTo, String subject, String Message) {
		SimpleMailMessage sm = new SimpleMailMessage();
		
		sm.setTo(sendTo);
		sm.setSubject(subject);
		sm.setText(Message);
		
		esender.send(sm);
	}
}
