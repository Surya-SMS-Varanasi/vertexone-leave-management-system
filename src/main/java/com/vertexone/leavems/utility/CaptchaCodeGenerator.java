package com.vertexone.leavems.utility;

import java.util.Random;

public class CaptchaCodeGenerator {
	public String captchaCodeGenerator() {
		String captchaData = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		String captcha="";
		
		Random r = new Random();
		
		for(int i = 0; i < 6; i++) {
			int cptInt = r.nextInt(captchaData.length()); //it gets the random value between 0 - captchaData.length()
			char ch = captchaData.charAt(cptInt);
			captcha = captcha + ch;
		}
		return captcha;
	}
}
