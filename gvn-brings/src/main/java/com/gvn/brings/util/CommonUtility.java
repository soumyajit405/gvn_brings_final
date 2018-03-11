package com.gvn.brings.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Random;

public class CommonUtility {

	public static String generateOTPCode()
	{
		Random random = new Random();
		String id = String.format("%04d", random.nextInt(10000));
		return id;
	}
	
	public String generateDeliveryCode()
	{
		
		Random random = new Random();
		String id = String.format("%04d", random.nextInt(10000));
		return id;
	}
	
	
	
	public  void copyFileUsingStream(File source, File dest) throws IOException {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        System.out.println("is "+is.toString());
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	        	System.out.println(" file "+length);
	            os.write(buffer, 0, length);
	        }
	    } finally {
	        is.close();
	        os.close();
	    }
	}


}
