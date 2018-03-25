package com.gvn.brings.web.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.gvn.brings.dto.RegistrationDto;
import com.gvn.brings.model.BrngBankDetails;
import com.gvn.brings.model.BrngUsrAddress;
import com.gvn.brings.model.BrngUsrReg;
import com.gvn.brings.model.BrngUsrRegAttr;
import com.gvn.brings.model.BrngUsrWallet;
import com.gvn.brings.model.BrngUsrWalletAttr;
import com.gvn.brings.services.RegistrationService;

@RestController
public class RegistrationController extends AbstractBaseController{

	@Autowired
	private RegistrationService registrationService;
	
	@RequestMapping(value = REST+"usrRegType", method = RequestMethod.GET,headers="Accept=application/json")
	public List<RegistrationDto> getUserRegType(){
		return registrationService.getUserRegType();
	}
	@RequestMapping(value = REST+"userRegStatus", method = RequestMethod.GET,headers="Accept=application/json")
	public List<RegistrationDto> getUserRegStatus(){
		return registrationService.getUserRegStatus();
	}
	@RequestMapping(value = REST+"usrType", method = RequestMethod.GET,headers="Accept=application/json")
	public List<RegistrationDto> getUserType(){
		return registrationService.getUserType();
	}
	@RequestMapping(value = REST+"usrReg", method = RequestMethod.POST,headers="Accept=application/json")
	public HashMap<String,String> registerUser(@RequestBody BrngUsrRegAttr brngusrreg){
		return registrationService.registerUser(brngusrreg);
	}
	@RequestMapping(value = REST+"getProfileById", method = RequestMethod.POST,headers="Accept=application/json")
	public List<RegistrationDto> getProfileDetails(@RequestBody HashMap<String, String> details){
		System.out.println("email; " + details.get("email"));
		return registrationService.getProfileDetails(details.get("email"));
	}
	@RequestMapping(value = REST+"updateProfile", method = RequestMethod.POST,headers="Accept=application/json")
	public HashMap<String,String>  updateProfile(@RequestBody BrngUsrRegAttr brngusrregattr) throws ClassNotFoundException, SQLException{
		return registrationService.updateProfile(brngusrregattr);
	}
	
	@RequestMapping(value = REST+"insertBankDetails", method = RequestMethod.POST,headers="Accept=application/json")
	public HashMap<String,String>  insertBankDetails(@RequestBody BrngBankDetails brngbankdetails) throws ClassNotFoundException, SQLException{
		return registrationService.insertBankDetails(brngbankdetails);
	}
	
	@RequestMapping(value = REST+"getBankDetails", method = RequestMethod.POST,headers="Accept=application/json")
	public  List<RegistrationDto>   getBankDetails(@RequestBody Hashtable<String, String> userDetails) throws ClassNotFoundException, SQLException{
		return registrationService.getBankDetails(userDetails.get("email"));
	}
	
	/*@RequestMapping(value = REST+"fileupload", headers=("content-type=multipart/*"), method = RequestMethod.POST)
	public  String   fileupload(@RequestParam("aadhar") MultipartFile aadhar,@RequestParam("rc") MultipartFile rc,
			@RequestParam("pan") MultipartFile pan,@RequestParam("drivingLic") MultipartFile drivingLic,@RequestParam("email") String email) throws ClassNotFoundException, SQLException,IOException{
		
		
		File aadhar1 = new File(aadhar.getOriginalFilename());
		aadhar1.createNewFile();
		File rc1 = new File(rc.getOriginalFilename());
		File pan1 = new File(pan.getOriginalFilename());
		File drivingLic1 = new File(drivingLic.getOriginalFilename());
		InputStream is = new FileInputStream(aadhar1);
	        //os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        System.out.println("is "+is.toString());
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	        	System.out.println(" file data"+length);
	           // os.write(buffer, 0, length);
	        }
	    try {
	    	
	    	System.out.println("addhar size"+aadhar1.getTotalSpace());
	    	rc1.createNewFile();
	    	pan1.createNewFile();
	    	drivingLic1.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		registrationService.fileupload(aadhar1,rc1,pan1,drivingLic1,email);
		return null;
		//return registrationService.getBankDetails(userDetails.get("email"));
	}
	*/
	
	@RequestMapping(value = REST+"fileupload", headers=("content-type=multipart/*"), method = RequestMethod.POST)
	public  String   fileupload(HttpServletRequest request,@RequestParam CommonsMultipartFile[] fileUpload) throws ClassNotFoundException, SQLException,IOException{
		// String saveDirectory = "D:\\Softwares\\apache-tomcat-8.5.9-windows-x64\\apache-tomcat-8.5.9\\webapps\\WebSockets\\32\\aadhar";
		//System.out.println("description: " + request.getParameter("email"));
        String email=request.getParameter("description");
            registrationService.fileupload(email, fileUpload);
        
		return null;
		//return registrationService.getBankDetails(userDetails.get("email"));
	}
	@RequestMapping(value = REST+"addMoney", method = RequestMethod.POST,headers="Accept=application/json")
	public  HashMap<String,String> addMoney(@RequestBody BrngUsrWalletAttr brngusrwallet) throws ClassNotFoundException, SQLException{
		return registrationService.addMoney(brngusrwallet);
	}
	
	@RequestMapping(value = REST+"checkBalance", method = RequestMethod.POST,headers="Accept=application/json")
	public  BigDecimal checkBalance(@RequestBody Hashtable<String,String> inputDetails) throws ClassNotFoundException, SQLException{
		return registrationService.checkBalance(inputDetails);
	}
	
	@RequestMapping(value = REST+"checkOTP", method = RequestMethod.POST,headers="Accept=application/json")
	public  HashMap<String, String> checkOTP(@RequestBody Hashtable<String,String> inputDetails) throws ClassNotFoundException, SQLException{
		return registrationService.checkOTP(inputDetails);
	}
	
	@RequestMapping(value = REST+"saveAddress", method = RequestMethod.POST,headers="Accept=application/json")
	public  HashMap<String, String> saveAddress(@RequestBody BrngUsrAddress brngusraddress) throws ClassNotFoundException, SQLException{
		return registrationService.saveAddress(brngusraddress);
	}
	
	@RequestMapping(value = REST+"getSavedAddress", method = RequestMethod.POST,headers="Accept=application/json")
	public  List<RegistrationDto> getSavedAddress(@RequestBody Hashtable<String,String> inputDetails) throws ClassNotFoundException, SQLException{
		return registrationService.getSavedAddress(inputDetails);
	}
	
	@RequestMapping(value = REST+"resendOtp", method = RequestMethod.POST,headers="Accept=application/json")
	public  HashMap<String,String> resendOtp(@RequestBody Hashtable<String,String> inputDetails) throws ClassNotFoundException, SQLException{
		return registrationService.resendOtp(inputDetails);
	}
	
	@RequestMapping(value = REST+"getImageURL", method = RequestMethod.POST,headers="Accept=application/json")
	public  HashMap<String,String> getImageURL(@RequestBody Hashtable<String,String> inputDetails) throws ClassNotFoundException, SQLException{
		return registrationService.getImageURL(inputDetails);
	}
	
	@RequestMapping(value = REST+"fileuploadTest", headers=("content-type=multipart/*"), method = RequestMethod.POST)
	public  HashMap<String,String>   fileuploadTest(@RequestParam("aadhar") MultipartFile aadhar,@RequestParam("vehiclerc") MultipartFile vehiclerc,@RequestParam("drivinglic") MultipartFile drivinglic,@RequestParam("pvin") MultipartFile pvin,@RequestParam("image") MultipartFile image,@RequestParam("email") String email) throws ClassNotFoundException, SQLException,IOException{
		// String saveDirectory = "D:\\Softwares\\apache-tomcat-8.5.9-windows-x64\\apache-tomcat-8.5.9\\webapps\\WebSockets\\32\\aadhar";
		//System.out.println("description: " + request.getParameter("email"));
		
		return registrationService.fileuploadTest(aadhar,vehiclerc,drivinglic,pvin,image,email); 
	}
	
	@RequestMapping(value = REST+"uploadImage", headers=("content-type=multipart/*"), method = RequestMethod.POST)
	public  HashMap<String,String>   uploadImage(@RequestParam("image") MultipartFile image,@RequestParam("email") String email) throws ClassNotFoundException, SQLException,IOException{
		// String saveDirectory = "D:\\Softwares\\apache-tomcat-8.5.9-windows-x64\\apache-tomcat-8.5.9\\webapps\\WebSockets\\32\\aadhar";
		//System.out.println("description: " + request.getParameter("email"));
		
		return registrationService.uploadImage(image,email); 
	}
	
	@RequestMapping(value = REST+"getPayuDetails",  method = RequestMethod.GET,headers="Accept=application/json")
	public  HashMap<String,String>   getPayuDetails() throws ClassNotFoundException, SQLException,IOException{
		// String saveDirectory = "D:\\Softwares\\apache-tomcat-8.5.9-windows-x64\\apache-tomcat-8.5.9\\webapps\\WebSockets\\32\\aadhar";
		//System.out.println("description: " + request.getParameter("email"));
		
		return registrationService.getPayuDetails(); 
	}
		// return null;
	
	
}
 