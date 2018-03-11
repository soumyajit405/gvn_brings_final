package com.gvn.brings.util;

public class GenerateRefCode {

	
	public String generateRefCode(String email)
	{
		RandomTimeGenerator rd=new RandomTimeGenerator();
		String suffixCode=rd.randomAlphaNumeric(4);
		StringBuffer refCode=new StringBuffer("");
		refCode.append(email.substring(0,4));
		refCode.append(suffixCode.substring(0, 4));
		return refCode.toString();
	}
}
