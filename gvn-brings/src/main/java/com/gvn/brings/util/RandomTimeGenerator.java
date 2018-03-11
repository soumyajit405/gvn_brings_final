package com.gvn.brings.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RandomTimeGenerator {

	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	public  String randomAlphaNumeric(int count) {
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmssSS");
	    String strDate = sdf.format(cal.getTime());
		return builder.toString()+strDate;
	}
	
	
	public static void main(String args[])
	{
		RandomTimeGenerator rt=new RandomTimeGenerator();
		String pt=rt.randomAlphaNumeric(16);
		System.out.println(pt);
	}

}
