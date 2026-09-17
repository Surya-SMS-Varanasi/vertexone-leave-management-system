package com.vertexone.leavems.encryption;

import java.util.Random;

public class EncryptionManager {
	
	public String passwordEncrypt(String password) {
		String encryptedPass ="";
		int asciiVal;
		int newAsciiVal;
		
		for(char ch : password.toCharArray()) {
			asciiVal = ch;
			
			if(asciiVal >= 'A' && asciiVal <= 'Z' ) {
				newAsciiVal = asciiVal + 32;
			}
			else if(asciiVal >= 'a' && asciiVal <= 'z') {
				newAsciiVal = asciiVal - 32;
			}
			else if(asciiVal >= '0' && asciiVal <= '9') {
				newAsciiVal = asciiVal + 3;
			}
			else {
				newAsciiVal = asciiVal;
			}
			char chr = (char)newAsciiVal;
			encryptedPass = encryptedPass + chr;
		}
		
		return encryptedPass;
	}
	
	public String passwordDecrypt(String encryptedPass) {
		String decryptedPass = "";
		int newAsciiVal;
		
		for(char ch : encryptedPass.toCharArray()) {
			int asciiVal = ch;
			
			if(asciiVal >= 'A' && asciiVal <= 'Z' ) {
				newAsciiVal = asciiVal + 32;
			}
			else if(asciiVal >= 'a' && asciiVal <= 'z') {
				newAsciiVal = asciiVal - 32;
			}
			else if(asciiVal >= '0' && asciiVal <= '9') {
				newAsciiVal = asciiVal - 3;
			}
			else if (asciiVal >= ':' && asciiVal <= '<') {
			    newAsciiVal = asciiVal - 3;
			}
			else {
				newAsciiVal = asciiVal;
			}
			char chr = (char)newAsciiVal;
			decryptedPass = decryptedPass + chr;
		}
		
		return decryptedPass;
	}
	
	public String fileNameChanger(String filename) {
		String newFileName ="";
		Random r = new Random();
		
		for(int i = 0; i < 15; i++) {
			int num = r.nextInt(filename.length());
			char ch = filename.charAt(num);
			
			newFileName = newFileName + ch;
		}
		
		return newFileName;
	}
}
