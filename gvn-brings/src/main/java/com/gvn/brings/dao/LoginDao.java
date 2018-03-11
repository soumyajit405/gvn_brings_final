package com.gvn.brings.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gvn.brings.dto.OrderDto;
import com.gvn.brings.model.BrngLkpIsPaid;
import com.gvn.brings.model.BrngLkpPayPercent;
import com.gvn.brings.model.BrngLkpUsrRegType;
import com.gvn.brings.model.BrngLkpUsrType;
import com.gvn.brings.model.BrngUsrLogin;
import com.gvn.brings.model.BrngUsrPassChange;
import com.gvn.brings.model.BrngUsrReg;
import com.gvn.brings.util.MailUtility;
import com.gvn.brings.util.PushHelper;
import com.gvn.brings.util.TempPasswordUtil;
import com.gvn.brings.util.UserMessages;

@Transactional
@Repository
public class LoginDao extends AbstractBaseDao{

	@PersistenceContext
    private EntityManager manager;
	public HashMap<String,String> loginUser(Hashtable<String,String> logindetails){
		String message="";
		HashMap<String,String> response=new HashMap<>();
		try
		{
			
		
			BrngUsrReg brngusrreg1=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+logindetails.get("emailId")+"'",BrngUsrReg.class).getSingleResult();
			
			int userId=brngusrreg1.getId();
			System.out.println("userId : " + userId);
			if(brngusrreg1.getBrnglkpotpvalidated().getCode().equalsIgnoreCase("N"))
			{
				message=UserMessages.getUserMessagesNew("ONV");
				response.put("message", message);
				response.put("response", "11");
				return response;
			}
			if(logindetails.get("usrType").equalsIgnoreCase("S") && brngusrreg1.getBrnglkpservicemanvalidated().getCode().equalsIgnoreCase("N"))
			{
				message=UserMessages.getUserMessagesNew("PUP");
				response.put("message", message);
				response.put("response", "13");
				return response;
			}
			if(brngusrreg1.getEmailId().equalsIgnoreCase(logindetails.get("emailId")) && brngusrreg1.getPassword().equalsIgnoreCase(logindetails.get("password")))
			{
				 Timestamp loginTime=new Timestamp(System.currentTimeMillis());
					Query query = manager.
						      createQuery("Select count(*) from BrngUsrLogin where brngUsrReg.id="+userId);
							long count = (long)query.getSingleResult();
							
							if(count>0)
							{
								System.out.println("player Id "+logindetails.get("playerId"));
								BrngLkpUsrType brnglkpusrtype = manager.createQuery("Select a From BrngLkpUsrType a  where a.usrType='"+logindetails.get("usrType")+"'",BrngLkpUsrType.class).getSingleResult();
								System.out.println("inside If " );
							 query = manager
										.createQuery("UPDATE BrngUsrLogin a SET a.loginTime = :loginTime,a.playerId=:playerId,a.logoutTime=:logoutTime,a.brngLkpUsrType.id=:type "
										+ "WHERE a.brngUsrReg.id= :id");
										query.setParameter("id", userId);
										query.setParameter("loginTime",loginTime);
										query.setParameter("playerId",logindetails.get("playerId"));
										query.setParameter("logoutTime",null);
										query.setParameter("type",brnglkpusrtype.getId());
										
											query.executeUpdate();
							}
							else
							{
								System.out.println("player Id "+logindetails.get("playerId"));
								BrngLkpUsrType brnglkpusrtype = manager.createQuery("Select a From BrngLkpUsrType a where a.usrType='"+logindetails.get("usrType")+"'",BrngLkpUsrType.class).getSingleResult();
								
								System.out.println("inside else " );
								BrngUsrLogin brngusrlogin=new BrngUsrLogin();
								brngusrlogin.setPlayerId(logindetails.get("playerId"));
								BrngUsrReg brngusrreg=new BrngUsrReg();
								brngusrreg.setId(userId);
								brngusrlogin.setBrngUsrReg(brngusrreg);
								brngusrlogin.setBrngLkpUsrType(brnglkpusrtype);
								//System.out.println(brngusrlogin.getBrngUsrReg().getId());
								brngusrlogin.setLoginTime(loginTime);
								System.out.println("before setting " );
								//brngusrlogin.setBrngUsrReg(brngusrlogin.getBrngUsrReg());
								manager.persist(brngusrlogin);
							}
							message=UserMessages.getUserMessagesNew("LS");
							response.put("message", message);
							response.put("response", "1");
							
			}
			
			
			
			/*else
			{
				Query query = manager.
					      createQuery("Select count(*) from BrngUsrPassChange a where a.brngUsrReg.emailId= '"+logindetails.get("emailId")+"'");
						long count = (long)query.getSingleResult();
						
						if(count>0)
						{
				BrngUsrPassChange brngusrchangepass=manager.createQuery("Select a From BrngUsrPassChange a where a.brngUsrReg.emailId= '"+logindetails.get("emailId")+"' order by a.effectiveDate desc limit 1 ",BrngUsrPassChange.class).getSingleResult();
				String pwd=brngusrchangepass.getPassword();
				if(brngusrreg1.getEmailId().equalsIgnoreCase(logindetails.get("emailId")) && pwd.equalsIgnoreCase(logindetails.get("password")))
				{
					Timestamp loginTime=new Timestamp(System.currentTimeMillis());
					 query = manager.
						      createQuery("Select count(*) from BrngUsrLogin where brngUsrReg.id="+userId);
							 count = (long)query.getSingleResult();
							
							if(count>0)
							{
								BrngLkpUsrType brnglkpusrtype = manager.createQuery("Select a From BrngLkpUsrType a  where a.usrType='"+logindetails.get("usrType")+"'",BrngLkpUsrType.class).getSingleResult();
								System.out.println("inside If " );
							 query = manager
										.createQuery("UPDATE BrngUsrLogin a SET a.loginTime = :loginTime,a.playerId=:playerId,a.logoutTime=:logoutTime,a.brngLkpUsrType.id=:type "
										+ "WHERE a.brngUsrReg.id= :id");
										query.setParameter("id", userId);
										query.setParameter("loginTime",loginTime);
										query.setParameter("playerId",logindetails.get("playerId"));
										query.setParameter("logoutTime",null);
										query.setParameter("type",brnglkpusrtype.getId());
										
											query.executeUpdate();
							}
							else
							{
								BrngLkpUsrType brnglkpusrtype = manager.createQuery("Select a From BrngLkpUsrType a where a.usrType='"+logindetails.get("usrType")+"'",BrngLkpUsrType.class).getSingleResult();
								
								System.out.println("inside else " );
								BrngUsrLogin brngusrlogin=new BrngUsrLogin();
								brngusrlogin.setPlayerId(logindetails.get("playerId"));
								BrngUsrReg brngusrreg=new BrngUsrReg();
								brngusrreg.setId(userId);
								brngusrlogin.setBrngUsrReg(brngusrreg);
								brngusrlogin.setBrngLkpUsrType(brnglkpusrtype);
								//System.out.println(brngusrlogin.getBrngUsrReg().getId());
								brngusrlogin.setLoginTime(loginTime);
								System.out.println("before setting " );
								//brngusrlogin.setBrngUsrReg(brngusrlogin.getBrngUsrReg());
								manager.persist(brngusrlogin);
							}
							
							//
							PushHelper pushhelper=new PushHelper();
							pushhelper.pushTest(logindetails.get("playerId"), "Welcome");
							message=UserMessages.getUserMessages(1);
							response.put("message", message);
							response.put("response", "1");
							
				}
				else
				{
					message=UserMessages.getUserMessages(0);
					response.put("message", message);
					response.put("response", "0");
				}
						}
				else
				{
				message=UserMessages.getUserMessages(0);
				response.put("message", message);
				response.put("response", "0");
				}
			}*/
			
		
		}
		catch(NoResultException nre)
		{
			message=UserMessages.getUserMessagesNew("NR");
			response.put("message", message);
			response.put("response", "3");	
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			message=UserMessages.getUserMessagesNew("E");
			response.put("message", message);
			response.put("response", "-1");
		}
		return response;
		
	}
	
	public  HashMap<String,String> sendPasswordToMail(BrngUsrReg brngusrreg) throws SQLException{
		
		HashMap<String,String> response =new HashMap<>();
		String message="";
		try
		{
			String email=brngusrreg.getEmailId();
		String password = null;
		Query query = manager.
			      createQuery("SELECT id  FROM BrngUsrReg WHERE emailId = '"+email+"'");
		List<Integer> results = query.getResultList();
				if(results.size()>0)
				{
					   Date date = new Date();
		               
		                DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		               
		                //to convert Date to String, use format method of SimpleDateFormat class.
		                String strDate = dateFormat.format(date);
					TempPasswordUtil gtp=new TempPasswordUtil();
					 password=gtp.getPassword();
					 BrngUsrPassChange brngUsrPassChange =new BrngUsrPassChange();
					 BrngUsrReg brngUsrReg=new BrngUsrReg();
					 brngUsrReg.setId(results.get(0));
					 brngUsrPassChange.setBrngUsrReg(brngUsrReg);
					 brngUsrPassChange.setEffectiveDate(strDate);
					 brngUsrPassChange.setPassword(password);
					 
					 manager.persist(brngUsrPassChange);
					 MailUtility mailutility=new  MailUtility();
					 mailutility.sendEmail(email, password);
					 message=UserMessages.getUserMessagesNew("PSM");
						response.put("message", message);
						response.put("response", "1");
						query = manager
								.createQuery("UPDATE BrngUsrReg a SET a.password = :password "
								+ "WHERE a.emailId= :email");
						 query.setParameter("password", password);
							query.setParameter("email",email);
							query.executeUpdate();
				}
				else
				{
					message=UserMessages.getUserMessagesNew("NR");
					response.put("message", message);
					response.put("response", "3");
				}
		
				
			
	
		}catch(Exception e){
			e.printStackTrace();
			message=UserMessages.getUserMessagesNew("E");
			response.put("message", message);
			response.put("response", "-1");
		}  
		finally{
			
		}  
		
		return response;	 
	}
	
	
	public 	HashMap<String,String> changePassword(BrngUsrReg brngusrreg) throws SQLException, ClassNotFoundException
	{
		HashMap<String,String> response =new HashMap<>();
		String message="";
		try
		{
		String password = null;
		Query query = manager
				.createQuery("UPDATE BrngUsrReg a SET a.password = :password "
				+ "WHERE a.emailId= :email");
				query.setParameter("password", brngusrreg.getPassword());
				query.setParameter("email",brngusrreg.getEmailId());
				query.executeUpdate();
				message=UserMessages.getUserMessagesNew("PCS");
				response.put("message", message);
				response.put("response", "1");
				}
	catch(Exception e){
			e.printStackTrace();
			message=UserMessages.getUserMessagesNew("E");
			response.put("message", message);
			response.put("response", "-1");
		}  
		finally{
			
		} 
		return response;
			
	}
	
	
	public 	HashMap<String,String> logoutUser(BrngUsrReg brngUsrReg) throws SQLException, ClassNotFoundException
	{	
		HashMap<String,String> response =new HashMap<>();
		String message="";
		try
	{
	String password = null;
	Timestamp logOutTime=new Timestamp(System.currentTimeMillis());
	Query query = manager.
		      createQuery("SELECT id  FROM BrngUsrReg WHERE emailId = '"+brngUsrReg.getEmailId()+"'");
	List<Integer> results = query.getResultList();
	 query = manager
			.createQuery("UPDATE BrngUsrLogin a SET a.logoutTime =:time,a.playerId=:playerId "
			+ "WHERE a.brngUsrReg.id= :id");
			query.setParameter("time", logOutTime);
			query.setParameter("id",results.get(0));
			query.setParameter("playerId","NA");
			query.executeUpdate();
			message=UserMessages.getUserMessagesNew("LOS");
			response.put("message", message);
			response.put("response", "1");
			}
catch(Exception e){
		e.printStackTrace();
		message=UserMessages.getUserMessagesNew("E");
		response.put("message", message);
		response.put("response", "-1");
	}  
	finally{
		
	} 

return response;
	}
	
	public int checkLoginExistence(int userRegId)
	{
		try
		{
		BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= '"+userRegId+"' and a.logoutTime='NA'",BrngUsrReg.class).getSingleResult();
		}
		catch(NoResultException nre)
		{
			return 1;
		}
		return 0;
	}
}
