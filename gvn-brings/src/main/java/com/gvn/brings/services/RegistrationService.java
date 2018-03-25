package com.gvn.brings.services;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.mail.Multipart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.gvn.brings.dao.RegistrationDao;
import com.gvn.brings.dto.RegistrationDto;
import com.gvn.brings.model.BrngBankDetails;
import com.gvn.brings.model.BrngUsrAddress;
import com.gvn.brings.model.BrngUsrReg;
import com.gvn.brings.model.BrngUsrRegAttr;
import com.gvn.brings.model.BrngUsrWalletAttr;

@Service("registrationService")
public class RegistrationService extends AbstractBaseService{

	@Autowired
	private RegistrationDao registrationDao;
	
	public List<RegistrationDto> getUserRegType(){
		return registrationDao.getUserRegTypeList();
	}
	public List<RegistrationDto> getUserRegStatus(){
		return registrationDao.getUserRegStatusList();
	}
	public List<RegistrationDto> getUserType(){
		return registrationDao.getUserTypeList();
	}
	public HashMap<String,String> registerUser(BrngUsrRegAttr brngusrreg){
		return registrationDao.registerUser(brngusrreg);
	}
	public List<RegistrationDto> getProfileDetails(String email){
		return registrationDao.getProfileDetails(email);
	}
	public HashMap<String,String> updateProfile(BrngUsrRegAttr brngusrregattr) throws ClassNotFoundException, SQLException{
		return registrationDao.updateProfile(brngusrregattr);
	}
	
	public HashMap<String,String> insertBankDetails(BrngBankDetails brngbankdetails) throws ClassNotFoundException, SQLException{
		return registrationDao.insertBankDetails(brngbankdetails);
	}
	
	public  List<RegistrationDto>  getBankDetails(String email) throws ClassNotFoundException, SQLException{
		return registrationDao.getBankDetails(email);
	}
	
	public  HashMap<String,String>  addMoney(BrngUsrWalletAttr brngusrwallet) throws ClassNotFoundException, SQLException{
		return registrationDao.addMoney(brngusrwallet);
	}
	
	public  BigDecimal  checkBalance(Hashtable<String,String> inputDetails) throws ClassNotFoundException, SQLException{
		return registrationDao.checkBalance(inputDetails);
	}
	
	public  HashMap<String, String>  checkOTP(Hashtable<String,String> inputDetails) throws ClassNotFoundException, SQLException{
		return registrationDao.checkOTP(inputDetails);
	}
	
	public  HashMap<String, String>  saveAddress(BrngUsrAddress brngusraddress) throws ClassNotFoundException, SQLException{
		return registrationDao.saveAddress(brngusraddress);
	}
	
	public  List<RegistrationDto>  getSavedAddress(Hashtable<String,String> inputDetails) throws ClassNotFoundException, SQLException{
		return registrationDao.getSavedAddress(inputDetails.get("email"));
	}
	
	/*public  String  fileupload(File aadhar,File rc,File pan,File drivingLic,String email) throws ClassNotFoundException, SQLException, FileNotFoundException{
		return registrationDao.uploadFile(aadhar,rc,pan,drivingLic,email);
	}*/
	
	public  HashMap<String,String>  resendOtp(Hashtable<String,String> inputDetails) throws ClassNotFoundException, SQLException{
		return registrationDao.resendOtp(inputDetails.get("email"));
	}
	
	public  HashMap<String,String>  fileupload(String email,CommonsMultipartFile[] fileUpload) throws ClassNotFoundException, SQLException, FileNotFoundException{
		return registrationDao.uploadFile(email,fileUpload);
	}
	
	public  HashMap<String,String>  getImageURL(Hashtable<String,String> inputDetails) throws ClassNotFoundException, SQLException{
		return registrationDao.getImageURL(inputDetails.get("email"));
	}
	
	public  HashMap<String,String>  fileuploadTest(MultipartFile aadhar,MultipartFile vehiclerc,MultipartFile drivinglic,MultipartFile pvin,MultipartFile image,String email) throws ClassNotFoundException, SQLException, FileNotFoundException{
		return registrationDao.uploadFileTest(aadhar,vehiclerc,drivinglic,pvin,image,email);
	}
	
	public  HashMap<String,String>  uploadImage(MultipartFile image,String email) throws ClassNotFoundException, SQLException, FileNotFoundException{
		return registrationDao.uploadImage(image,email);
	}
	
	public  HashMap<String,String>  getPayuDetails() throws ClassNotFoundException, SQLException, FileNotFoundException{
		return registrationDao.getPayuDetails();
	}
	
	
	
}
