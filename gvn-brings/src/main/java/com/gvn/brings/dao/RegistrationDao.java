package com.gvn.brings.dao;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.ServletContext;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gvn.brings.dto.OrderDeliveryDto;
import com.gvn.brings.dto.RegistrationDto;
import com.gvn.brings.model.BrngBankDetails;
import com.gvn.brings.model.BrngLkpFilePath;
import com.gvn.brings.model.BrngLkpIsPaid;
import com.gvn.brings.model.BrngLkpOrderDelStatus;
import com.gvn.brings.model.BrngLkpOtpValidated;
import com.gvn.brings.model.BrngLkpPayType;
import com.gvn.brings.model.BrngLkpServicemanValidated;
import com.gvn.brings.model.BrngLkpUsrRegStatus;
import com.gvn.brings.model.BrngLkpUsrRegType;
import com.gvn.brings.model.BrngLkpUsrType;
import com.gvn.brings.model.BrngOrderDelivery;
import com.gvn.brings.model.BrngUsrAddress;
import com.gvn.brings.model.BrngUsrCode;
import com.gvn.brings.model.BrngUsrFiles;
import com.gvn.brings.model.BrngUsrLogin;
import com.gvn.brings.model.BrngUsrOtp;
import com.gvn.brings.model.BrngUsrReg;
import com.gvn.brings.model.BrngUsrRegAttr;
import com.gvn.brings.model.BrngUsrWallet;
import com.gvn.brings.model.BrngUsrWalletAttr;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import com.gvn.brings.util.CommonUtility;
import com.gvn.brings.util.GenerateRefCode;
import com.gvn.brings.util.MailUtility;
import com.gvn.brings.util.OTPUtil;
import com.gvn.brings.util.UserMessages;

import java.nio.file.Files;
@Transactional
@Repository
public class RegistrationDao extends AbstractBaseDao{
	@PersistenceContext
    private EntityManager manager;
	
	@Autowired
	ServletContext context;
	
	public List<RegistrationDto> getUserRegTypeList(){
		List<BrngLkpUsrRegType> brngLkpUsrRegTypes = manager.createQuery("Select a From BrngLkpUsrRegType a",BrngLkpUsrRegType.class).
				getResultList();
		List<RegistrationDto> dtoList = new ArrayList();
		RegistrationDto registrationDto = null;
		for(BrngLkpUsrRegType brngLkpUsrRegType:brngLkpUsrRegTypes){
			registrationDto = new RegistrationDto(brngLkpUsrRegType);
			dtoList.add(registrationDto);
		}
		return dtoList;
	}
	
	public List<RegistrationDto> getUserRegStatusList(){
		List<BrngLkpUsrRegStatus> brngLkpUsrRegStatuses = manager.createQuery("Select a From BrngLkpUsrRegStatus a",BrngLkpUsrRegStatus.class).
				getResultList();
		List<RegistrationDto> dtoList = new ArrayList();
		RegistrationDto registrationDto = null;
		for(BrngLkpUsrRegStatus brngLkpUsrRegStatus:brngLkpUsrRegStatuses){
			registrationDto = new RegistrationDto(brngLkpUsrRegStatus);
			dtoList.add(registrationDto);
		}
		return dtoList;
	}
	
	public List<RegistrationDto> getUserTypeList(){
		List<BrngLkpUsrType> brngLkpUsrTypes = manager.createQuery("Select a From BrngLkpUsrType a",BrngLkpUsrType.class).
				getResultList();
		List<RegistrationDto> dtoList = new ArrayList();
		RegistrationDto registrationDto = null;
		for(BrngLkpUsrType brngLkpUsrType:brngLkpUsrTypes){
			registrationDto = new RegistrationDto(brngLkpUsrType);
			dtoList.add(registrationDto);
		}
		return dtoList;
	}
	
	
	public HashMap<String,String> registerUser(BrngUsrRegAttr brngusrregAttr){
		
		HashMap<String,String> response =new HashMap<>();
		String message="";
		MailUtility mlu=new MailUtility();
		try
		{
		int checkEmailStatus=checkEmailExistence(brngusrregAttr.getBrngUsrReg().getEmailId());
		if(checkEmailStatus==1)
		{
			message=UserMessages.getUserMessagesNew("D");
			response.put("response","2");
			response.put("message",message);
			return response;
		}
		else if(checkEmailStatus==-1)
		{
			message=UserMessages.getUserMessagesNew("E");
			response.put("response","-1");
			response.put("message",message);
			return response;
		}
		else if(mlu.checkValidMail(brngusrregAttr.getBrngUsrReg().getEmailId())==0)
		{
			message=UserMessages.getUserMessagesNew("NVM");
			response.put("response","-1");
			response.put("message",message);
			return response;
		}
		
		BrngLkpUsrRegStatus brngusrregstatus = manager.createQuery("Select a From BrngLkpUsrRegStatus a where a.statusType='"+brngusrregAttr.getBrngUsrReg().getBrngLkpUsrRegStatus().getStatusType()+"'",BrngLkpUsrRegStatus.class).getSingleResult();
		BrngLkpUsrRegType brngusrregtype = manager.createQuery("Select a From BrngLkpUsrRegType a where a.usrRegType='"+brngusrregAttr.getBrngUsrReg().getBrngLkpUsrRegType().getUsrRegType()+"'",BrngLkpUsrRegType.class).getSingleResult();
		BrngLkpOtpValidated brnglkpotpvalidated= manager.createQuery("Select a From BrngLkpOtpValidated a where a.code='N'",BrngLkpOtpValidated.class).getSingleResult();
		BrngLkpServicemanValidated brnglkpservicemanvalidated= manager.createQuery("Select a From BrngLkpServicemanValidated a where a.code='N'",BrngLkpServicemanValidated.class).getSingleResult();
		Timestamp registeredTime=new Timestamp(System.currentTimeMillis());
		brngusrregAttr.getBrngUsrReg().setBrngLkpUsrRegStatus(brngusrregstatus);
		brngusrregAttr.getBrngUsrReg().setBrngLkpUsrRegType(brngusrregtype);
		
		brngusrregAttr.getBrngUsrReg().setRegisteredDate(registeredTime);
		brngusrregAttr.getBrngUsrReg().setBrnglkpotpvalidated(brnglkpotpvalidated);
		brngusrregAttr.getBrngUsrReg().setBrnglkpservicemanvalidated(brnglkpservicemanvalidated);
		brngusrregAttr.setEffectiveDate(registeredTime);
		
		System.out.println("6 : " + registeredTime);
		
		//insert to brnguser
		manager.persist(brngusrregAttr.getBrngUsrReg());
		
		String email = brngusrregAttr.getBrngUsrReg().getEmailId();
		BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+email+"'",BrngUsrReg.class).getSingleResult();
		brngusrregAttr.setBrngUsrReg(brngusrreg);
		System.out.println("1 : " + brngusrreg.getEmailId() + " : 2: " + brngusrreg.getId());
		//insert to brnguserattr
		manager.persist(brngusrregAttr);
		
		
		BrngUsrOtp brngusrotp=new BrngUsrOtp();
		brngusrotp.setEffectiveDate(registeredTime);
		brngusrotp.setBrngUsrReg(brngusrregAttr.getBrngUsrReg());
		OTPUtil otputil=new OTPUtil();
		brngusrotp.setOtpCode(otputil.sendOTP(brngusrregAttr.getPhoneNumber()));
		manager.persist(brngusrotp);
		
		/*System.out.println(" Code "+brngusrregAttr.getBrngUsrReg().getBrngUsrCodes().get(0));
		
		BrngUsrCode brngusrcode=brngusrregAttr.getBrngUsrReg().getBrngUsrCodes().get(0);
		if(brngusrcode.getRefCode()!="NA")
		{
			System.out.println("2 : " + brngusrcode.getRefCode() );
		}
		GenerateRefCode grf=new GenerateRefCode();
		String refCode=grf.generateRefCode(brngusrregAttr.getBrngUsrReg().getEmailId());
		System.out.println("refCode  1:" +refCode);
		BrngUsrCode brngusrcode1=new BrngUsrCode();
		brngusrcode1.setBrngUsrReg(brngusrreg);
		brngusrcode1.setDescription("Default");
		brngusrcode1.setEffectiveDate(registeredTime);
		brngusrcode.setRefCode(refCode);
		manager.persist(brngusrcode1);*/
	//	BrngUsrCode brngusrcode=new BrngUsrCode();
		/*BrngUsrCode brngUsrCode = manager.createQuery("Select a From BrngUsrCode a where a.refCode='"+brngusrregAttr.getBrngUsrReg().getBrngUsrCodes().get(0).getRefCode()+"'",BrngUsrCode.class).getSingleResult();
		int userRegId=0;
		userRegId=brngUsrCode.getBrngUsrReg().getId();
		System.out.println("user Id " +userRegId);
		brngUsrCode.setBrngUsrReg(brngusrregAttr.getBrngUsrReg());
		brngUsrCode.setEffectiveDate(registeredTime);
		brngUsrCode.setRefCode(refCode);
		System.out.println("refCode " +refCode);
		if(userRegId!=0)
		{
		brngUsrCode.setRefCodeId(userRegId);	
		}*/
		//transaction.commit();
		message=UserMessages.getUserMessagesNew("S");
		response.put("response","1");
		response.put("message",message);
		return response;
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//transaction.rollback();
			message=UserMessages.getUserMessagesNew("E");
			response.put("response","-1");
			response.put("message",message);
			return response;
		}
		
	}
	
	
	public List<RegistrationDto> getProfileDetails(String email){
		
		BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+email+"'",BrngUsrReg.class).getSingleResult();
		
		//OrderDeliveryDao odao=new OrderDeliveryDao();
		int userId=brngusrreg.getId();
		System.out.println("userId : " + userId);
		List<BrngUsrRegAttr> brngUsrRegDetails = manager.createQuery("Select a From BrngUsrRegAttr a where a.brngUsrReg.id="+userId,BrngUsrRegAttr.class).
				getResultList();
		List<RegistrationDto> dtoList = new ArrayList();
		RegistrationDto registrationDto = null;
		for(BrngUsrRegAttr brngUsrRegDetail:brngUsrRegDetails){
			System.out.println("ghhh"+brngUsrRegDetail.getFirstName());
			registrationDto = new RegistrationDto(brngUsrRegDetail);
			
			float totalDistance=getTotalDistance(email);
			registrationDto.setTotalDistance(totalDistance);
			dtoList.add(registrationDto);
		}
		
		
		
		return dtoList;
	}
	
	public 	 HashMap<String,String>  updateProfile(BrngUsrRegAttr brngusrregattr) throws SQLException, ClassNotFoundException
	{
		
		HashMap<String,String> response =new HashMap<>();
		String message="";
		try
		{
		String password = null;
		
		
			BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+brngusrregattr.getBrngUsrReg().getEmailId()+"'",BrngUsrReg.class).getSingleResult();
		
		int userId=brngusrreg.getId();
		System.out.println("userId : " + userId);
		Query query = manager
				.createQuery("UPDATE BrngUsrRegAttr a SET a.firstName = :firstName,a.lastName=:lastName,a.middleName=:middleName,a.phoneNumber=:phoneNumber "
				+ "WHERE a.brngUsrReg.id= :id");
				query.setParameter("firstName", brngusrregattr.getFirstName());
				query.setParameter("lastName", brngusrregattr.getLastName());
				query.setParameter("middleName", brngusrregattr.getMiddleName());
				query.setParameter("phoneNumber", brngusrregattr.getPhoneNumber());
				
				query.setParameter("id",userId);
				query.executeUpdate();
				message=UserMessages.getUserMessagesNew("U");
				response.put("response","1");
				response.put("message",message);
				}
	catch(Exception e){
			e.printStackTrace();
			message=UserMessages.getUserMessagesNew("E");
			response.put("response","-1");
			response.put("message",message);
		}  
		finally{
			
		} 
		return response;
			
	}

	
	public HashMap<String,String> insertBankDetails(BrngBankDetails brngbankdetails){
		
		HashMap<String,String> response =new HashMap<>();
		String message="";
		Timestamp registeredTime =null;
		BrngUsrReg brngusrreg=null;
		//System.out.println("6 : " + registeredTime);
		try
		{
		/* transaction = manager.getTransaction();
		transaction.begin();*/
		//manager.persist(brngusrreg);
		//brngusrregattr.setBrngUsrReg(brngusrreg);
			 registeredTime =new Timestamp(System.currentTimeMillis());
			 brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+brngbankdetails.getBrngUsrReg().getEmailId()+"'",BrngUsrReg.class).getSingleResult();
			
			int userId=brngusrreg.getId();
			
			BrngBankDetails brngbankdetailstemp=manager.createQuery("Select a From BrngBankDetails a where a.brngUsrReg.id= "+userId,BrngBankDetails.class).getSingleResult();
			System.out.println("......  "+brngbankdetails);
			
				Query query = manager
			.createQuery("UPDATE BrngBankDetails a SET a.bankName = :bankName,a.ifscCode=:ifscCode,a.accountNumber=:accountNumber,a.accountName=:accountName,a.branch=:branch,a.effectiveDate=:effectiveDate "
			+ "WHERE a.brngUsrReg.id= :id");
			query.setParameter("bankName", brngbankdetails.getBankName());
			query.setParameter("ifscCode", brngbankdetails.getIfscCode());
			query.setParameter("accountNumber", brngbankdetails.getAccountNumber());
			query.setParameter("accountName", brngbankdetails.getAccountName());
			query.setParameter("branch", brngbankdetails.getBranch());
			query.setParameter("effectiveDate", registeredTime);
			query.setParameter("id", userId);
			query.executeUpdate();
				
			
		
		//transaction.commit();
			message=UserMessages.getUserMessagesNew("AB");
			response.put("message", message);
			response.put("response", "1");
		}
		catch (NoResultException nre) {
			brngbankdetails.setEffectiveDate(registeredTime);
			brngbankdetails.setBrngUsrReg(brngusrreg);
		manager.merge(brngbankdetails);
		message=UserMessages.getUserMessagesNew("E");
		response.put("message", message);
		response.put("response", "1");
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
			//transaction.rollback();
			message=UserMessages.getUserMessages(-1);
			response.put("message", message);
			response.put("response", "-1");
		}
		return response;
	}
	
public List<RegistrationDto> getBankDetails(String email){
		
		
		Timestamp registeredTime =null;
		BrngUsrReg brngusrreg=null;
		//System.out.println("6 : " + registeredTime);
		try
		{
		/* transaction = manager.getTransaction();
		transaction.begin();*/
		//manager.persist(brngusrreg);
		//brngusrregattr.setBrngUsrReg(brngusrreg);
			 registeredTime =new Timestamp(System.currentTimeMillis());
			 brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+email+"'",BrngUsrReg.class).getSingleResult();
			
			int userId=brngusrreg.getId();
			
			List<BrngBankDetails> brngbankdetails = manager.createQuery("Select a From BrngBankDetails a where a.brngUsrReg.id="+userId,BrngBankDetails.class).
					getResultList();
			List<RegistrationDto> dtoList = new ArrayList();
			RegistrationDto registrationDto = null;
			for(BrngBankDetails brngbankdetail:brngbankdetails){
				//System.out.println("ghhh"+brngUsrRegDetail.getFirstName());
				registrationDto = new RegistrationDto(brngbankdetail);
				dtoList.add(registrationDto);
			}
			return dtoList;
		
		}
		
		
		catch(Exception e)
		{
			e.printStackTrace();
			//transaction.rollback();
			return null;
		}
		
	}

public HashMap<String,String> addMoney(BrngUsrWalletAttr brngusrwalletattr){
	
	HashMap<String,String> response =new HashMap<>();
	String message="";
	Timestamp registeredTime =null;
	BrngUsrReg brngusrreg=null;
	//System.out.println("6 : " + registeredTime);
	try
	{
	/* transaction = manager.getTransaction();
	transaction.begin();*/
	//manager.persist(brngusrreg);
	//brngusrregattr.setBrngUsrReg(brngusrreg);
		 registeredTime =new Timestamp(System.currentTimeMillis());
		 brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+brngusrwalletattr.getBrngUsrWallet().getBrngUsrReg().getEmailId()+"'",BrngUsrReg.class).getSingleResult();
		
		int userId=brngusrreg.getId();
		
		Query query = manager.
			      createQuery("Select count(*) from BrngUsrWallet a where a.brngUsrReg.id="+userId);
				long count = (long)query.getSingleResult();
				Timestamp updateTime=new Timestamp(System.currentTimeMillis());
				if(count>0)
				{
					
					int walletId=0;
					BigDecimal sum=new BigDecimal("0");
				List<BrngUsrWalletAttr> listbrngUsrWalletAttr=manager.createQuery("Select a From BrngUsrWalletAttr a where a.brngUsrWallet.brngUsrReg.id= "+userId +" and a.status='S'",BrngUsrWalletAttr.class).getResultList();
				System.out.println("size "+ listbrngUsrWalletAttr.size());
				List<BrngUsrWalletAttr> brngusrwalletattrtemp=manager.createQuery("Select a From BrngUsrWalletAttr a where a.brngUsrWallet.brngUsrReg.id= "+userId ,BrngUsrWalletAttr.class).getResultList();
				walletId=brngusrwalletattrtemp.get(0).getBrngUsrWallet().getId();
				if(listbrngUsrWalletAttr.size()>0)
				{
				//System.out.println(" :  "+listbrngUsrWalletAttr.get(0).getBrngUsrWallet().getId());
				
				for(int i=0;i<listbrngUsrWalletAttr.size();i++)
				{
					sum=sum.add(listbrngUsrWalletAttr.get(i).getTranAmt());
				}
				}
				brngusrwalletattr.getBrngUsrWallet().setBrngUsrReg(brngusrreg);
				brngusrwalletattr.getBrngUsrWallet().setId(walletId);
				brngusrwalletattr.setEffectiveDate(updateTime);
				brngusrwalletattr.setPayTxnId(brngusrwalletattr.getPayTxnId());
				brngusrwalletattr.setStatus(brngusrwalletattr.getStatus());
				manager.merge(brngusrwalletattr);
				if(brngusrwalletattr.getStatus().equalsIgnoreCase("S"))
				{
					sum=sum.add(brngusrwalletattr.getTranAmt());
				}
				
				//brngusrwalletattr.getBrngUsrWallet().setCuurAmt(sum);
				query = manager.
					      createQuery("update BrngUsrWallet a set a.currAmt=:currAmt where a.brngUsrReg.id="+userId);
				query.setParameter("currAmt", sum);
				query.executeUpdate();
				System.out.println("TXN AMOUNT"+brngusrwalletattr.getTranAmt());
				System.out.println("---"+brngusrwalletattr.getTranAmt().compareTo(BigDecimal.ZERO));
				if(brngusrwalletattr.getTranAmt().compareTo(BigDecimal.ZERO) < 0)
				{
					System.out.println("Inside if");
					BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= '"+userId+"'",BrngUsrLogin.class).getSingleResult();
					
					int userLoginId=brngusrlogin.getId();
					int paidId = 0;
					BrngLkpPayType brnglkppaytype=manager.createQuery("Select a From BrngLkpPayType a where a.type= 'W'",BrngLkpPayType.class).getSingleResult();
					int payTypeId=brnglkppaytype.getId();
					List<BrngLkpIsPaid> brngLkpIsPaidTypes = manager.createQuery("Select a From BrngLkpIsPaid a",BrngLkpIsPaid.class).
							getResultList();
					List<OrderDeliveryDto> dtoList = new ArrayList();
					OrderDeliveryDto orderDeliveryDto = null;
					for(BrngLkpIsPaid brngLkpIsPaidType:brngLkpIsPaidTypes){
						if(brngLkpIsPaidType.getIsPaid().equalsIgnoreCase("Y"))
						{
							paidId=brngLkpIsPaidType.getId();
							//paidStatus=brngLkpIsPaidType.getIsPaid();
							break;
						}
					}
					System.out.println(" Paid Id "+paidId);
					 query = manager
							.createQuery("UPDATE BrngOrder a SET a.brngLkpIsPaid.id =:isPaid,a.payTxId=:payTxId,a.brnglkppaytype.id=:payTypeId where a.id=:orderId and a.brngUsrLogin.id=:userLoginId");
							query.setParameter("isPaid",paidId);
							
							
							query.setParameter("orderId",brngusrwalletattr.getBrngOrder().getId());
							query.setParameter("userLoginId",userLoginId);
							query.setParameter("payTxId","NA");
							query.setParameter("payTypeId",payTypeId);
							
							query.executeUpdate();
					
				}

				
				}
				else
				{
					brngusrwalletattr.getBrngUsrWallet().setBrngUsrReg(brngusrreg);
					if(brngusrwalletattr.getStatus().equalsIgnoreCase("S"))
					{
						brngusrwalletattr.getBrngUsrWallet().setCuurAmt(brngusrwalletattr.getTranAmt());
						
					}
					else
					{
					brngusrwalletattr.getBrngUsrWallet().setCuurAmt(BigDecimal.ZERO);;
					}
					manager.merge(brngusrwalletattr.getBrngUsrWallet());
					BrngUsrWallet brngusrwallet= manager.
						      createQuery("Select a from BrngUsrWallet a where a.brngUsrReg.id="+userId,BrngUsrWallet.class).getSingleResult();
					brngusrwalletattr.setBrngUsrWallet(brngusrwallet);
					brngusrwalletattr.setEffectiveDate(updateTime);
					manager.merge(brngusrwalletattr);
								}
		
		
	
	//transaction.commit();
				message=UserMessages.getUserMessagesNew("WUS");
				response.put("message", message);
				response.put("response", "1");
	}
	
	catch(Exception e)
	{
		e.printStackTrace();
		//transaction.rollback();
		message=UserMessages.getUserMessagesNew("E");
		response.put("message", message);
		response.put("response", "-1");
	}
	return response;
}
	

public BigDecimal checkBalance(Hashtable<String,String> inputDetails){
	

	Timestamp registeredTime =null;
	BrngUsrReg brngusrreg=null;
	//System.out.println("6 : " + registeredTime);
	try
	{
	
	//manager.persist(brngusrreg);
	//brngusrregattr.setBrngUsrReg(brngusrreg);
		 registeredTime =new Timestamp(System.currentTimeMillis());
		 brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+inputDetails.get("email")+"'",BrngUsrReg.class).getSingleResult();
		
		int userId=brngusrreg.getId();
		
		System.out.println("  : "+userId);
		BrngUsrWallet brngusrwallet=manager.createQuery("select a From BrngUsrWallet a where a.brngUsrReg.id="+userId,BrngUsrWallet.class).getSingleResult();
		
		BigDecimal amount=brngusrwallet.getCuurAmt();
		
		
	
	//transaction.commit();
	return amount;
	}
	catch (NoResultException nre) {
	
		BigDecimal amount=new BigDecimal("0");
	return amount;
	}
	
	catch(Exception e)
	{
		e.printStackTrace();
		//transaction.rollback();
		//return 0;
	}
	return null;
	
	}

public HashMap<String, String> checkOTP(Hashtable<String,String> inputDetails){
	
	HashMap<String,String> response =new HashMap<>();
	BrngUsrReg brngusrreg=null;
	String message="";
	//System.out.println("6 : " + registeredTime);
	try
	{
	
	//manager.persist(brngusrreg);
	//brngusrregattr.setBrngUsrReg(brngusrreg);
		
		 brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+inputDetails.get("email")+"'",BrngUsrReg.class).getSingleResult();
		
		int userId=brngusrreg.getId();
		
		System.out.println("  : "+userId +inputDetails.get("otp"));
		Query query = manager.
			      createQuery("Select count(*) from BrngUsrOtp a where a.brngUsrReg.id="+userId+" and a.otpCode ='"+inputDetails.get("otp")+"'");
					System.out.println("query "+query);
				long count = (long)query.getSingleResult();
				System.out.println("count "+count);
				if(count>0)
				{
					BrngLkpOtpValidated brnglkpotpvalidated=manager.createQuery("Select a From BrngLkpOtpValidated a where a.code='Y'",BrngLkpOtpValidated.class).getSingleResult();
					query = manager
							.createQuery("UPDATE BrngUsrReg a SET a.brnglkpotpvalidated = :brngotpvalidated  where a.id= :id ");
					query.setParameter("brngotpvalidated", brnglkpotpvalidated);
					
					query.setParameter("id",userId);
						query.executeUpdate();
					message=UserMessages.getUserMessagesNew("OM");
					response.put("response","1");
					response.put("message",message);
				
				}
				else
				{
					message=UserMessages.getUserMessagesNew("ONG");
					response.put("response","9");
					response.put("message",message);
				}
	
		
		
	//transaction.commit();
	
	}
	catch (NoResultException nre) {
	
		System.out.println("  Test:1 ");
		message=UserMessages.getUserMessagesNew("ONM");
		response.put("response","8");
		response.put("message",message);
		
	}
	
	catch(Exception e)
	{
		e.printStackTrace();
		message=UserMessages.getUserMessagesNew("E");
		response.put("response","-1");
		response.put("message",message);
		
		System.out.println("  Test2: ");
		//transaction.rollback();
		//return 0;
	}
	return response;
	
	}

	public int checkEmailExistence(String mail)
	{
		try
		{
			System.out.println("mail :"+mail);
			Query query = manager.
				      createQuery("Select count(*) from BrngUsrReg a where  a.emailId='"+mail+"'");
					long count = (long)query.getSingleResult();
					if(count>0)
					{
						return 1;
					}
					else
					{
					return 0;	
					}
		//BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+mail+"'",BrngUsrReg.class).getSingleResult();
		}
		catch(Exception nre)
		{
			nre.printStackTrace();
			return -1;
		}
		//return 1;
		//brngusrregAttr.setBrngUsrReg(brngusrreg);
	}
	
	public HashMap<String,String> saveAddress(BrngUsrAddress brngusraddress){
		
		HashMap<String,String> response =new HashMap<>();
		String message="";
		Timestamp registeredTime =null;
		BrngUsrReg brngusrreg=null;
		//System.out.println("6 : " + registeredTime);
		try
		{
		/* transaction = manager.getTransaction();
		transaction.begin();*/
		//manager.persist(brngusrreg);
		//brngusrregattr.setBrngUsrReg(brngusrreg);
			 registeredTime =new Timestamp(System.currentTimeMillis());
			 brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+brngusraddress.getBrngUsrReg().getEmailId()+"'",BrngUsrReg.class).getSingleResult();
			
			//int userId=brngusrreg.getId();
			
			brngusraddress.setBrngUsrReg(brngusrreg);
			manager.persist(brngusraddress);
		
					message=UserMessages.getUserMessagesNew("AA");
					response.put("message", message);
					response.put("response", "1");
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
			//transaction.rollback();
			message=UserMessages.getUserMessagesNew("E");
			response.put("message", message);
			response.put("response", "-1");
		}
		return response;
	}
		
/*public String uploadFile(File aadhar,File rc,File pan,File drivingLic,String email) throws FileNotFoundException{
		
		HashMap<String,String> response =new HashMap<>();
		String message="";
		 
		
	      try {
		//System.out.println("6 : " + registeredTime);
		BrngLkpFilePath brnglkpfilepath=manager.createQuery("Select a From BrngLkpFilePath a where a.type='S'",BrngLkpFilePath.class).getSingleResult();
		
		BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId='"+email+"'",BrngUsrReg.class).getSingleResult();
		
		String path=brnglkpfilepath.getFilePath();
		
		int userRegId=brngusrreg.getId(); 
		System.out.println(path);
		
		File file = new File(path+""+userRegId+"/aadhar");
        file.mkdir();
       // file = new File(path+""+userRegId+"/vehiclerc/");
       // file.mkdir();
       // file = new File(path+""+userRegId+"/pan/");
      //  file.mkdir();
        file = new File(path+""+userRegId+"/aadhar/"+aadhar.getName());
       // file.mkdir();
        //file = new File(path+""+userRegId+"/driving_lic/");
      //  file.mkdir();
        System.out.println("Adddhar "+aadhar.getTotalSpace());
        FileOutputStream out = new FileOutputStream(new File(path+""+userRegId+"/aadhar/"+aadhar.getName()));  
        CommonUtility cmu=new CommonUtility();
        cmu.copyFileUsingStream(aadhar, file);
      //  Files.copy(, dest.toPath());
        
        byte[] buffer = new byte[1024];

        FileInputStream inputStream = 
            new FileInputStream(aadhar);

        // read fills buffer with data and returns
        // the number of bytes read (which of course
        // may be less than the buffer size, but
        // it will never be more).
        int total = 0;
        int nRead = 0;
        while((nRead = inputStream.read(buffer)) != -1) {
            // Convert to String so we can display it.
            // Of course you wouldn't want to do this with
            // a 'real' binary file.
           // System.out.println(new String(buffer));
            total += nRead;
            out.write(buffer,0,nRead);
        }   
System.out.println("total"+total);
        inputStream.close();
       // out.flush();  
        out.close(); 
        in = new FileInputStream(aadhar);
        out = new FileOutputStream(path+""+userRegId+"/aadhar/"+aadhar.getName());
        int c;
        while ((c = in.read()) != -1) {
           out.write(c);
        }
        System.out.println("Adddhar1 "+aadhar.getTotalSpace());
        in.close();
        out.close();
        in = new FileInputStream(pan);
        out = new FileOutputStream(path+""+userRegId+"/pan/"+pan.getName());
        c=0;
        while ((c = in.read()) != -1) {
           out.write(c);
        }
        in.close();
        out.close();
        in = new FileInputStream(drivingLic);
        out = new FileOutputStream(path+""+userRegId+"/driving_lic/"+drivingLic.getName());
        c=0;
        while ((c = in.read()) != -1) {
           out.write(c);
        }
        in.close();
        out.close();
        in = new FileInputStream(rc);
        out = new FileOutputStream(path+""+userRegId+"/vehiclerc/"+rc.getName());
        c=0;
        while ((c = in.read()) != -1) {
           out.write(c);
        }
        in.close();
        out.close();
        //saveNameInDB(userRegId, aadhar.getName(), drivingLic.getName(), rc.getName(), pan.getName());
					message=UserMessages.getUserMessages(1);
					response.put("message", message);
					response.put("response", "1");
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
			//transaction.rollback();
			message=UserMessages.getUserMessages(-1);
			response.put("message", message);
			response.put("response", "-1");
		}
	     
		return null;
	}

*/
public List<RegistrationDto> getSavedAddress(String email){
		
		
		Timestamp registeredTime =null;
		BrngUsrReg brngusrreg=null;
		//System.out.println("6 : " + registeredTime);
		try
		{
		/* transaction = manager.getTransaction();
		transaction.begin();*/
		//manager.persist(brngusrreg);
		//brngusrregattr.setBrngUsrReg(brngusrreg);
			 registeredTime =new Timestamp(System.currentTimeMillis());
			 brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+email+"'",BrngUsrReg.class).getSingleResult();
			
			int userId=brngusrreg.getId();
			
			List<BrngUsrAddress> brngusraddresses = manager.createQuery("Select a From BrngUsrAddress a where a.brngUsrReg.id="+userId,BrngUsrAddress.class).
					getResultList();
			List<RegistrationDto> dtoList = new ArrayList();
			RegistrationDto registrationDto = null;
			for(BrngUsrAddress brngusraddress:brngusraddresses){
				//System.out.println("ghhh"+brngUsrRegDetail.getFirstName());
				registrationDto = new RegistrationDto(brngusraddress);
				dtoList.add(registrationDto);
			}
			return dtoList;
		
		}
		
		
		catch(Exception e)
		{
			e.printStackTrace();
			//transaction.rollback();
			return null;
		}
		
	}


public HashMap<String,String> resendOtp(String email){
	
	
	Timestamp registeredTime =null;
	BrngUsrReg brngusrreg=null;
	HashMap<String,String> response =new HashMap<>();
	String message="";
	 
	//System.out.println("6 : " + registeredTime);
	try
	{
	/* transaction = manager.getTransaction();
	transaction.begin();*/
	//manager.persist(brngusrreg);
	//brngusrregattr.setBrngUsrReg(brngusrreg);
		 registeredTime =new Timestamp(System.currentTimeMillis());
		 brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+email+"'",BrngUsrReg.class).getSingleResult();
		
		int userId=brngusrreg.getId();
		BrngUsrRegAttr brngusrregattr=manager.createQuery("Select a From BrngUsrRegAttr a where a.brngUsrReg.id= '"+userId+"'",BrngUsrRegAttr.class).getSingleResult();
		OTPUtil otputil=new OTPUtil();
		
		Query query = manager.
			      createQuery("Select count(*) from BrngUsrOtp where brngUsrReg.id="+userId);
				long count = (long)query.getSingleResult();
				
				if(count>0)
				{ 
					query = manager
				.createQuery("UPDATE BrngUsrOtp a SET a.effectiveDate = :generatedTime,a.otpCode=:otpCode where a.brngUsrReg= :reg "
				+ "WHERE a.brngUsrReg.id= :id");
				query.setParameter("generatedTime", registeredTime);
				query.setParameter("otpCode",otputil.sendOTP(brngusrregattr.getPhoneNumber()));
				query.setParameter("reg",brngusrreg);
				
				
					query.executeUpdate();
					
				}
				else
				{
					BrngUsrOtp brngusrotp=new BrngUsrOtp();
				brngusrotp.setEffectiveDate(registeredTime);
				brngusrotp.setBrngUsrReg(brngusrreg);
				
				brngusrotp.setOtpCode(otputil.sendOTP(brngusrregattr.getPhoneNumber()));
				manager.persist(brngusrotp);
					
				}
		
				
		
		}
		
	
	
	
	catch(Exception e)
	{
		e.printStackTrace();
		//transaction.rollback();
		message=UserMessages.getUserMessagesNew("E");
		response.put("message", message);
		response.put("response", "-1");
	}
	message=UserMessages.getUserMessagesNew("ORS");
	response.put("message", message);
	response.put("response", "1");
	return response;
	
}

public int saveNameInDB(int usrRegId,String aadhar,String vehiclerc,String pvin,String drivinglic,String image){
	
	
	Timestamp uploadedTime =null;

	//System.out.println("6 : " + registeredTime);
	try
	{
	/* transaction = manager.getTransaction();
	transaction.begin();*/
	//manager.persist(brngusrreg);
	//brngusrregattr.setBrngUsrReg(brngusrreg);
		uploadedTime =new Timestamp(System.currentTimeMillis());
		 BrngLkpFilePath brnglkpfilepath=manager.createQuery("Select a From BrngLkpFilePath a where a.type='H'",BrngLkpFilePath.class).getSingleResult();
		
		String filePath=brnglkpfilepath.getFilePath();
		
		 BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.id="+usrRegId,BrngUsrReg.class).getSingleResult();
		// CommonsMultipartFile aFile =fileUpload[0];
		BrngUsrFiles brngusrfiles=new BrngUsrFiles();
		brngusrfiles.setAadhar(filePath+""+usrRegId+"/aadhar/"+aadhar);
		//aFile =fileUpload[3];
		brngusrfiles.setDriving_lic(filePath+""+usrRegId+"/driving_lic/"+drivinglic);
		//aFile =fileUpload[2];
		brngusrfiles.setPvin(filePath+""+usrRegId+"/pvin/"+pvin);
		//aFile =fileUpload[1];
		brngusrfiles.setVehiclerc(filePath+""+usrRegId+"/vehiclerc/"+vehiclerc);
		//aFile =fileUpload[1];
		brngusrfiles.setImage(filePath+""+usrRegId+"/image/"+image);
		brngusrfiles.setEffectiveDate(uploadedTime);
		brngusrfiles.setBrngUsrReg(brngusrreg);
		
		
		manager.merge(brngusrfiles);
		return 1;
	}
	
	
	catch(Exception e)
	{
		e.printStackTrace();
		//transaction.rollback();
		return -1;
	}
	
}


public int saveImageInDB(int usrRegId,String image){
	
	
	Timestamp uploadedTime =null;
	uploadedTime =new Timestamp(System.currentTimeMillis());
	//System.out.println("6 : " + registeredTime);
	try
	{
	/* transaction = manager.getTransaction();
	transaction.begin();*/
	//manager.persist(brngusrreg);
	//brngusrregattr.setBrngUsrReg(brngusrreg);
		 BrngLkpFilePath brnglkpfilepath=manager.createQuery("Select a From BrngLkpFilePath a where a.type='H'",BrngLkpFilePath.class).getSingleResult();
			
			String filePath=brnglkpfilepath.getFilePath();
			
		 Query query = manager.
			      createQuery("Select count(*) from BrngUsrFiles a where a.brngUsrReg.id="+usrRegId);
				long count = (long)query.getSingleResult();
				
				if(count>0)
				{
					 query = manager
							.createQuery("UPDATE BrngUsrFiles a SET a.image = :imageUrl,a.effectiveDate=:effectiveDate "
							+ "WHERE a.brngUsrReg.id= :usrRegId");	
					query.setParameter("imageUrl",filePath+""+usrRegId+"/image/"+image );
					query.setParameter("usrRegId",usrRegId);
					query.setParameter("effectiveDate",uploadedTime);
					
					
					query.executeUpdate();
				}
				else
				{
	
		
		 BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.id="+usrRegId,BrngUsrReg.class).getSingleResult();
		// CommonsMultipartFile aFile =fileUpload[0];
		BrngUsrFiles brngusrfiles=new BrngUsrFiles();
		
		brngusrfiles.setImage(filePath+""+usrRegId+"/image/"+image);
		brngusrfiles.setEffectiveDate(uploadedTime);
		brngusrfiles.setBrngUsrReg(brngusrreg);
		
		
		manager.merge(brngusrfiles);
				}
		return 1;
	}
	
	
	catch(Exception e)
	{
		e.printStackTrace();
		//transaction.rollback();
		return -1;
	}
	
}

public HashMap<String,String> uploadFile(String email,CommonsMultipartFile[] fileUpload) throws FileNotFoundException{
	
	HashMap<String,String> response =new HashMap<>();
	String message="";
	 
	
      try {
	//System.out.println("6 : " + registeredTime);
	BrngLkpFilePath brnglkpfilepath=manager.createQuery("Select a From BrngLkpFilePath a where a.type='S'",BrngLkpFilePath.class).getSingleResult();
	
	BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId='"+email+"'",BrngUsrReg.class).getSingleResult();
	
	String path=brnglkpfilepath.getFilePath();
	
	int userRegId=brngusrreg.getId(); 
	System.out.println(userRegId);
	System.out.println(path);
	System.out.println("q1 : "+path+""+userRegId+"\\aadhar\\");
	File file = new File(path+""+userRegId+"\\aadhar\\");
    file.mkdirs();
     file = new File(path+""+userRegId+"\\vehiclerc\\");
    file.mkdirs();
    file = new File(path+""+userRegId+"\\pvin\\");
    file.mkdirs();
   // file = new File(path+""+userRegId+"/aadhar/"+aadhar.getName());
   // file.mkdir();
    file = new File(path+""+userRegId+"\\driving_lic\\");
    file.mkdirs();
    file = new File(path+""+userRegId+"\\image\\");
    file.mkdirs();
    System.out.println("after file upload ");
    CommonsMultipartFile aFile =fileUpload[0];
         
        if (!aFile.getOriginalFilename().equals("")) {
            aFile.transferTo(new File(path+""+userRegId+"\\aadhar\\" + aFile.getOriginalFilename()));
    }
        aFile =fileUpload[1];
        
        if (!aFile.getOriginalFilename().equals("")) {
            aFile.transferTo(new File(path+""+userRegId+"\\vehiclerc\\" + aFile.getOriginalFilename()));
    }
        aFile =fileUpload[2];
        
        if (!aFile.getOriginalFilename().equals("")) {
            aFile.transferTo(new File(path+""+userRegId+"\\pvin\\" + aFile.getOriginalFilename()));
    }
        aFile =fileUpload[3];
        
        if (!aFile.getOriginalFilename().equals("")) {
            aFile.transferTo(new File(path+""+userRegId+"\\driving_lic\\" + aFile.getOriginalFilename()));
    }
        aFile =fileUpload[4];
        
        if (!aFile.getOriginalFilename().equals("")) {
            aFile.transferTo(new File(path+""+userRegId+"\\image\\" + aFile.getOriginalFilename()));
    }
       
  //  saveNameInDB(userRegId, fileUpload);
				message=UserMessages.getUserMessages(1);
				response.put("message", message);
				response.put("response", "1");
	}
	
	catch(Exception e)
	{
		e.printStackTrace();
		//transaction.rollback();
		message=UserMessages.getUserMessages(-1);
		response.put("message", message);
		response.put("response", "-1");
	}
     
	return response;
}
public HashMap<String,String> getImageURL(String email){
	
	HashMap<String,String> response=new HashMap<>();
	Timestamp registeredTime =null;
	BrngUsrReg brngusrreg=null;
	//System.out.println("6 : " + registeredTime);
	try
	{
		 brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+email+"'",BrngUsrReg.class).getSingleResult();
			
			int userId=brngusrreg.getId();
			
		Query query = manager.
			      createQuery("Select count(*) from BrngUsrFiles where brngUsrReg.id="+userId);
				long count = (long)query.getSingleResult();
		if(count>0)
		{
		
		BrngUsrFiles brngusrfiles=manager.createQuery("Select a From BrngUsrFiles a where a.brngUsrReg.id="+userId,BrngUsrFiles.class).getSingleResult();
		response.put("response", brngusrfiles.getImage());
		}
		else
		{
			response.put("response", null);
		}
	
	}
	
	
	catch(Exception e)
	{
		e.printStackTrace();
		response.put("response", "-1");
		//transaction.rollback();
		
	}
	return response;
}
public HashMap<String,String> uploadFileTest(MultipartFile aadhar,MultipartFile vehiclerc,MultipartFile drivinglic,MultipartFile pvin,MultipartFile image,String email) throws FileNotFoundException{
	
	HashMap<String,String> response =new HashMap<>();
	String message="";
	 
	
      try {
    	  BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId='"+email+"'",BrngUsrReg.class).getSingleResult();
    	  int userRegId=brngusrreg.getId(); 
	//System.out.println("6 : " + registeredTime);
    	  Query query = manager.
			      createQuery("Select count(*) from BrngUsrFiles a where a.brngUsrReg.id="+userRegId);
				long count = (long)query.getSingleResult();
				
				if(count > 0)
				{
					message=UserMessages.getUserMessages(14);
					response.put("message", message);
					response.put("response", "14");
					return response;
				}
	BrngLkpFilePath brnglkpfilepath=manager.createQuery("Select a From BrngLkpFilePath a where a.type='S'",BrngLkpFilePath.class).getSingleResult();
	
	
	BufferedOutputStream outputStream =null;
	String path=brnglkpfilepath.getFilePath();
	
	
	
	
	System.out.println(userRegId);
	System.out.println(path);
	System.out.println("q1 : "+path+""+userRegId+"\\aadhar\\");
	File file = new File(path+""+userRegId);
	file.mkdirs();
	 file = new File(path+""+userRegId+"/aadhar/");
    file.mkdirs();
     file = new File(path+""+userRegId+"/vehiclerc/");
    file.mkdirs();
    file = new File(path+""+userRegId+"/pvin/");
    file.mkdirs();
   // file = new File(path+""+userRegId+"/aadhar/"+aadhar.getName());
   // file.mkdir();
    file = new File(path+""+userRegId+"/driving_lic/");
    file.mkdirs();
    file = new File(path+""+userRegId+"/image/");
    file.mkdirs();
    System.out.println("after file upload ");
    /*CommonsMultipartFile aFile =fileUpload[0];
         
        if (!aFile.getOriginalFilename().equals("")) {
            aFile.transferTo(new File(path+""+userRegId+"\\aadhar\\" + aFile.getOriginalFilename()));
    }
        aFile =fileUpload[1];
        
        if (!aFile.getOriginalFilename().equals("")) {
            aFile.transferTo(new File(path+""+userRegId+"\\vehiclerc\\" + aFile.getOriginalFilename()));
    }
        aFile =fileUpload[2];
        
        if (!aFile.getOriginalFilename().equals("")) {
            aFile.transferTo(new File(path+""+userRegId+"\\pvin\\" + aFile.getOriginalFilename()));
    }
        aFile =fileUpload[3];
        
        if (!aFile.getOriginalFilename().equals("")) {
            aFile.transferTo(new File(path+""+userRegId+"\\driving_lic\\" + aFile.getOriginalFilename()));
    }
        aFile =fileUpload[4];
        
        if (!aFile.getOriginalFilename().equals("")) {
            aFile.transferTo(new File(path+""+userRegId+"\\image\\" + aFile.getOriginalFilename()));
    }*/
    
    if (!aadhar.getOriginalFilename().isEmpty()) {
         outputStream = new BufferedOutputStream(
              new FileOutputStream(
                    new File(path+""+userRegId+"/aadhar/", aadhar.getOriginalFilename())));
        outputStream.write(aadhar.getBytes());
        outputStream.flush();
        outputStream.close();
    }
        if (!vehiclerc.getOriginalFilename().isEmpty()) {
             outputStream = new BufferedOutputStream(
                  new FileOutputStream(
                        new File(path+""+userRegId+"/vehiclerc/", vehiclerc.getOriginalFilename())));
            outputStream.write(vehiclerc.getBytes());
            outputStream.flush();
            outputStream.close();
        }
         if (!pvin.getOriginalFilename().isEmpty()) {
                 outputStream = new BufferedOutputStream(
                      new FileOutputStream(
                            new File(path+""+userRegId+"/pvin/", pvin.getOriginalFilename())));
                outputStream.write(pvin.getBytes());
                outputStream.flush();
                outputStream.close();
         }
          if (!drivinglic.getOriginalFilename().isEmpty()) {
                     outputStream = new BufferedOutputStream(
                          new FileOutputStream(
                                new File(path+""+userRegId+"/driving_lic/", drivinglic.getOriginalFilename())));
                    outputStream.write(drivinglic.getBytes());
                    outputStream.flush();
                    outputStream.close();
          }
           if (!image.getOriginalFilename().isEmpty()) {
                         outputStream = new BufferedOutputStream(
                              new FileOutputStream(
                                    new File(path+""+userRegId+"/image/", image.getOriginalFilename())));
                        outputStream.write(image.getBytes());
                        outputStream.flush();
                        outputStream.close();
           }
          
       
    saveNameInDB(userRegId,aadhar.getOriginalFilename(),vehiclerc.getOriginalFilename(),pvin.getOriginalFilename(),drivinglic.getOriginalFilename(),image.getOriginalFilename());
				
				BrngLkpServicemanValidated brnglkpservicemanvalidated= manager.createQuery("Select a From BrngLkpServicemanValidated a where a.code='Y'",BrngLkpServicemanValidated.class).getSingleResult();
				 query = manager
						.createQuery("UPDATE BrngUsrReg a SET a.brnglkpservicemanvalidated.id = :validatedId "
						+ "WHERE a.id= :id");	
				query.setParameter("validatedId",brnglkpservicemanvalidated.getId() );
				query.setParameter("id",userRegId);
				
				
				query.executeUpdate();
				message=UserMessages.getUserMessages(1);
				response.put("message", message);
				response.put("response", "1");
	}
      
	
	catch(Exception e)
	{
		e.printStackTrace();
		//transaction.rollback();
		message=UserMessages.getUserMessages(-1);
		response.put("message", message);
		response.put("response", "-1");
	}
     
	return response;
}


public HashMap<String,String> uploadImage(MultipartFile image,String email) throws FileNotFoundException{
	
	HashMap<String,String> response =new HashMap<>();
	String message="";
	 
	
      try {
	//System.out.println("6 : " + registeredTime);
    	  BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId='"+email+"'",BrngUsrReg.class).getSingleResult();
    	  int userRegId=brngusrreg.getId(); 
	//System.out.println("6 : " + registeredTime);
    	 
	BrngLkpFilePath brnglkpfilepath=manager.createQuery("Select a From BrngLkpFilePath a where a.type='S'",BrngLkpFilePath.class).getSingleResult();
	
	//BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId='"+email+"'",BrngUsrReg.class).getSingleResult();
	BufferedOutputStream outputStream =null;
	String path=brnglkpfilepath.getFilePath();
	//int userRegId=brngusrreg.getId(); 
	
	
	
	System.out.println(userRegId);
	System.out.println(path);
	System.out.println("q1 : "+path+""+userRegId+"\\aadhar\\");
	//File file = new File(path+""+userRegId);
	
	File file = new File(path+""+userRegId+"/image/");
    file.mkdirs();
    System.out.println("after file upload ");
    /*CommonsMultipartFile aFile =fileUpload[0];
         
        if (!aFile.getOriginalFilename().equals("")) {
            aFile.transferTo(new File(path+""+userRegId+"\\aadhar\\" + aFile.getOriginalFilename()));
    }
        aFile =fileUpload[1];
        
        if (!aFile.getOriginalFilename().equals("")) {
            aFile.transferTo(new File(path+""+userRegId+"\\vehiclerc\\" + aFile.getOriginalFilename()));
    }
        aFile =fileUpload[2];
        
        if (!aFile.getOriginalFilename().equals("")) {
            aFile.transferTo(new File(path+""+userRegId+"\\pvin\\" + aFile.getOriginalFilename()));
    }
        aFile =fileUpload[3];
        
        if (!aFile.getOriginalFilename().equals("")) {
            aFile.transferTo(new File(path+""+userRegId+"\\driving_lic\\" + aFile.getOriginalFilename()));
    }
        aFile =fileUpload[4];
        
        if (!aFile.getOriginalFilename().equals("")) {
            aFile.transferTo(new File(path+""+userRegId+"\\image\\" + aFile.getOriginalFilename()));
    }*/
    
  
           if (!image.getOriginalFilename().isEmpty()) {
                         outputStream = new BufferedOutputStream(
                              new FileOutputStream(
                                    new File(path+""+userRegId+"/image/", image.getOriginalFilename())));
                        outputStream.write(image.getBytes());
                        outputStream.flush();
                        outputStream.close();
           }
          
       
   // saveNameInDB(userRegId,aadhar.getOriginalFilename(),vehiclerc.getOriginalFilename(),pvin.getOriginalFilename(),drivinglic.getOriginalFilename(),image.getOriginalFilename());
			saveImageInDB(userRegId, image.getOriginalFilename())	;
				/*BrngLkpServicemanValidated brnglkpservicemanvalidated= manager.createQuery("Select a From BrngLkpServicemanValidated a where a.code='Y'",BrngLkpServicemanValidated.class).getSingleResult();
				Query query = manager
						.createQuery("UPDATE BrngUsrReg a SET a.brnglkpservicemanvalidated.id = :validatedId "
						+ "WHERE a.id= :id");	
				query.setParameter("validatedId",brnglkpservicemanvalidated.getId() );
				query.setParameter("id",userRegId);
				
				
				query.executeUpdate();*/
				message=UserMessages.getUserMessages(1);
				response.put("message", message);
				response.put("response", "1");
	}
      
	
	catch(Exception e)
	{
		e.printStackTrace();
		//transaction.rollback();
		message=UserMessages.getUserMessages(-1);
		response.put("message", message);
		response.put("response", "-1");
	}
     
	return response;
}

public float getTotalDistance(String email)
{
	System.out.println("Email"+email);
	float totalDistance=0;
	try{
		BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+email+"'",BrngUsrReg.class).getSingleResult();
		
		int userId=brngusrreg.getId();
		System.out.println("userId"+userId);
		BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= '"+userId+"'",BrngUsrLogin.class).getSingleResult();
		
		int userLoginId=brngusrlogin.getId();
		System.out.println("Login Id"+userLoginId);
		BrngLkpOrderDelStatus brnglkporderdelstatus = manager.createQuery("Select a From BrngLkpOrderDelStatus a where a.delStatus='Y'",BrngLkpOrderDelStatus.class).
				getSingleResult();
		int completeId=brnglkporderdelstatus.getId();
		List<BrngOrderDelivery> brngOrderDelivery=manager.createQuery("Select a From BrngOrderDelivery a where a.brngUsrLogin.id= "+userLoginId+" and a.brngLkpOrderDelStatus.id="+completeId,BrngOrderDelivery.class).getResultList();
		for(int i=0;i<brngOrderDelivery.size();i++ )
		{
			System.out.println(brngOrderDelivery.get(i).getBrngOrder().getTotalDistance());
			//BrngOrder brngorder1=manager.createQuery("Select a From BrngOrder a where a.id= "+brngOrderDelivery.get(i).getBrngOrder().getTotalDistance(),BrngOrder.class).getResultList();
			totalDistance=totalDistance+brngOrderDelivery.get(i).getBrngOrder().getTotalDistance().floatValue();
		}
			
	//	BrngOrder brngorder1=manager.createQuery("Select a From BrngOrder a where a.id= "+orderId,BrngOrder.class).getResultList();
		
	}
	catch(Exception e)
	{
		e.printStackTrace();
		//return -1;
	}
	return totalDistance;
}
}
