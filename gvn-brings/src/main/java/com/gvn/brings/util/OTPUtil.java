package com.gvn.brings.util;



import java.net.HttpURLConnection;

import java.net.URL;
import java.net.URLEncoder;

import java.sql.ResultSet;
import java.sql.SQLException;


public class OTPUtil {
	public String  sendOTP(String phoneNo) throws ClassNotFoundException, SQLException {
		/*
		 * this method is used to send push messages to all users
		 */
		
		String otPassword=CommonUtility.generateOTPCode();
		try {
			String userId="Brings";
			String password="123456";
			String senderId="BRINGS";
		//	String mobileNo="9154748948";
			String message="Your verification code for signup is ";
			
			message=message+otPassword;
			
	   String jsonResponse;
			   
			 
			  // con.setRequestProperty("Authorization", "Basic YzY0NTVkZDAtZmZiZi00ZjExLTg5ZjgtYmQ2OTBlNGU3ZTdi");
	   URL obj = new URL("http://trans.msg360.in/websms/sendsms.aspx?userid="+userId+"&password="+password+"&sender="+senderId+"&mobileno="+phoneNo+"&msg="+URLEncoder.encode(message, "UTF-8").replace("+", "%20"));
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
			  

			
		
			} catch(Throwable t) {
			   t.printStackTrace();
			}
return otPassword;	
	}


	
}
