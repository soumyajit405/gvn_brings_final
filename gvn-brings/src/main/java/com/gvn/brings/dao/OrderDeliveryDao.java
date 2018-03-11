package com.gvn.brings.dao;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gvn.brings.dto.OrderDeliveryDto;
import com.gvn.brings.dto.OrderDto;
import com.gvn.brings.dto.RegistrationDto;
import com.gvn.brings.model.BrngLkpFilePath;
import com.gvn.brings.model.BrngLkpIsAccepted;
import com.gvn.brings.model.BrngLkpIsCancelled;
import com.gvn.brings.model.BrngLkpIsPaid;
import com.gvn.brings.model.BrngLkpIsPicked;
import com.gvn.brings.model.BrngLkpOrderDelStatus;
import com.gvn.brings.model.BrngLkpServicemanValidated;
import com.gvn.brings.model.BrngLkpUsrRegType;
import com.gvn.brings.model.BrngOrder;
import com.gvn.brings.model.BrngOrderDelivery;
import com.gvn.brings.model.BrngOrderDeliveryAttr;

import com.gvn.brings.model.BrngServicemanLocationDtls;
import com.gvn.brings.model.BrngUsrFiles;
import com.gvn.brings.model.BrngUsrLogin;
import com.gvn.brings.model.BrngUsrOtp;
import com.gvn.brings.model.BrngUsrReg;
import com.gvn.brings.services.OrderDeliveryService;
import com.gvn.brings.util.CommonUtility;
import com.gvn.brings.util.DistanceChecker;
import com.gvn.brings.util.OTPUtil;
import com.gvn.brings.util.PushHelper;
import com.gvn.brings.util.RandomTimeGenerator;
import com.gvn.brings.util.UserMessages;

@Transactional
@Repository
public class OrderDeliveryDao {
	@PersistenceContext
    private EntityManager manager;
	
	public List<OrderDeliveryDto> getIsAcceptedTypes(){
		List<BrngLkpIsAccepted> brngLkpIsAcceptedTypes = manager.createQuery("Select a From BrngLkpIsAccepted a",BrngLkpIsAccepted.class).
				getResultList();
		List<OrderDeliveryDto> dtoList = new ArrayList();
		OrderDeliveryDto orderDeliveryDto = null;
		for(BrngLkpIsAccepted brngLkpIsAcceptedType:brngLkpIsAcceptedTypes){
			orderDeliveryDto = new OrderDeliveryDto(brngLkpIsAcceptedType);
			dtoList.add(orderDeliveryDto);
		}
		return dtoList;
	}
	
	public List<OrderDeliveryDto> getIsPickedTypes(){
		List<BrngLkpIsPicked> brngLkpIsPickedTypes = manager.createQuery("Select a From BrngLkpIsPicked a",BrngLkpIsPicked.class).
				getResultList();
		List<OrderDeliveryDto> dtoList = new ArrayList();
		OrderDeliveryDto orderDeliveryDto = null;
		for(BrngLkpIsPicked brngLkpIsPickedType:brngLkpIsPickedTypes){
			orderDeliveryDto = new OrderDeliveryDto(brngLkpIsPickedType);
			dtoList.add(orderDeliveryDto);
		}
		return dtoList;
	}
	
	public List<OrderDeliveryDto> getOrderDelStatus(){
		List<BrngLkpOrderDelStatus> brngLkpOrderDelStatuses = manager.createQuery("Select a From BrngLkpOrderDelStatus a",BrngLkpOrderDelStatus.class).
				getResultList();
		List<OrderDeliveryDto> dtoList = new ArrayList();
		OrderDeliveryDto orderDeliveryDto = null;
		for(BrngLkpOrderDelStatus brngLkpOrderDelStatus:brngLkpOrderDelStatuses){
			orderDeliveryDto = new OrderDeliveryDto(brngLkpOrderDelStatus);
			dtoList.add(orderDeliveryDto);
		}
		return dtoList;
	}
	public synchronized  HashMap<String,String> acceptAnOrder(BrngOrderDelivery brngorderdelivery){
		
		//EntityTransaction transaction=null;
		int acceptanceId=0;
		Timestamp ts=new Timestamp(System.currentTimeMillis());
		HashMap<String,String> response =new HashMap<>();
		String message="";
		try
		{
		/* transaction = manager.getTransaction();
		transaction.begin();*/
			BrngOrder brngorder1=manager.createQuery("Select a From BrngOrder a where a.id= "+brngorderdelivery.getBrngOrder().getId(),BrngOrder.class).getSingleResult();
			String isCancelled = brngorder1.getBrnglkpiscancelled().getIsCancelled();
			String isAccepted = brngorder1.getIsAccepted().getIsAccepted();
			
			System.out.println("isCancelled : " + isCancelled);
			System.out.println("isAccepted : " + isAccepted);
			
			if(("Y".equalsIgnoreCase(isCancelled)))
			{
				message=UserMessages.getUserMessagesNew("OAC");
				response.put("message", message);
				response.put("response", "10");	
				return response;
			}	
			else if( "Y".equalsIgnoreCase(isAccepted))
			{
				message=UserMessages.getUserMessagesNew("OAA");
				response.put("message", message);
				response.put("response", "4");	
				return response;
			}

			BrngLkpIsAccepted brngLkpIsAcceptedTypes = manager.createQuery("Select a From BrngLkpIsAccepted a where a.isAccepted='Y'",BrngLkpIsAccepted.class).
					getSingleResult();
			acceptanceId=brngLkpIsAcceptedTypes.getId();
					System.out.println("Acceptance Id "+acceptanceId);
			BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+brngorderdelivery.getBrngUsrLogin().getBrngUsrReg().getEmailId()+"'",BrngUsrReg.class).getSingleResult();
			
			int userId=brngusrreg.getId();
			
			BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= '"+userId+"'",BrngUsrLogin.class).getSingleResult();
			
			int userLoginId=brngusrlogin.getId();
			
		//	OrderDeliveryDao odao=new OrderDeliveryDao();
			//int response1=odao.deleteAllTempOrders(brngorderdelivery.getBrngOrder().getId(), userLoginId);
			/*if(response1==-1)
			{
				message=UserMessages.getUserMessages(-1);
				response.put("message", message);
				response.put("response", "-1");	
				return response;
			}*/
			
			CommonUtility cmu=new CommonUtility();
			String pickupcode=cmu.generateDeliveryCode();	
			System.out.println(" user Login Id"+userLoginId);
			System.out.println("order Id "+brngorderdelivery.getBrngOrder().getId());
		Query query = manager
				.createQuery("UPDATE BrngOrder a SET a.isAccepted.id =:isAccepted,a.pickupCode=:pickupcode where a.id=:orderId ");
				query.setParameter("isAccepted",acceptanceId);
				query.setParameter("pickupcode",pickupcode);
				
				
				query.setParameter("orderId",brngorderdelivery.getBrngOrder().getId());
				//query.setParameter("userLoginId",userLoginId);
				query.executeUpdate();
				System.out.println(" user Login Id 1 "+acceptanceId);
		//transaction.commit();
				RandomTimeGenerator rd=new RandomTimeGenerator();
			//	rd.randomAlphaNumeric(16);
				BrngLkpOrderDelStatus brnglkpdelstatus = manager.createQuery("Select a From BrngLkpOrderDelStatus a where a.delStatus='N'",BrngLkpOrderDelStatus.class).getSingleResult();
				brngorderdelivery.setTransactionId(rd.randomAlphaNumeric(16));
				brngorderdelivery.setBrngLkpOrderDelStatus(brnglkpdelstatus);
				brngorderdelivery.setBrngUsrLogin(brngusrlogin);
				manager.merge(brngorderdelivery);
				
				/*BrngOrderDelivery brngOrderDeliverytemp=manager.createQuery("Select a From BrngOrderDelivery a where a.brngOrder.id="+brngorderdelivery.getBrngOrder().getId(),BrngOrderDelivery.class).getSingleResult();
				int brngOrderDeliveryid=brngOrderDeliverytemp.getId();
				BrngOrderDeliveryAttr brngOrderDeliveryAttr=new BrngOrderDeliveryAttr();
				brngOrderDeliveryAttr.setBrngOrderDelivery(brngOrderDeliveryid);
				manager.merge(brngOrderDeliveryAttr);*/
				message=UserMessages.getUserMessagesNew("OAS");
				response.put("message", message);
				response.put("response", "1");	
					String playerId=brngorder1.getBrngUsrLogin().getPlayerId();
				
				PushHelper pushhelper=new PushHelper();
				pushhelper.pushToUser("notifybuyer", brngorder1.getBrngUsrLogin().getPlayerId(), "Order accepted by serviceman. Order No",Integer.toString(brngorder1.getId()));
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

	
	/*public HashMap<String,String> pickAnOrder(HashMap<String,String> inputdetails){
		int pickedId=0;
		HashMap<String,String> response =new HashMap<>();
		String message="";
		try
		{
			BrngOrder brngorder1=manager.createQuery("Select a From BrngOrder a where a.id= "+inputdetails.get("orderId"),BrngOrder.class).getSingleResult();
			
			if(getCancelStatus(brngorder1.getId())==1)
			{
				message=UserMessages.getUserMessagesNew("OAC");
				response.put("message", message);
				response.put("response", "10");
				return response;
			}
			if(brngorder1.getBrngLkpIsPicked().getIsPicked().equalsIgnoreCase("Y"))
			{
				message=UserMessages.getUserMessagesNew("OAPI");
				response.put("message", message);
				response.put("response", "5");
				return response;
			}
			if(!brngorder1.getPickupCode().equalsIgnoreCase(inputdetails.get("pickupcode")))
			{
				message=UserMessages.getUserMessagesNew("PNM");
				response.put("message", message);
				response.put("response", "15");
				return response;
			}
					
		BrngLkpIsPicked brngLkpIsPickedTypes = manager.createQuery("Select a From BrngLkpIsPicked a where a.isPicked='Y'",BrngLkpIsPicked.class).
				getSingleResult();
		
				pickedId=brngLkpIsPickedTypes.getId();
	
		BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+inputdetails.get("email")+"'",BrngUsrReg.class).getSingleResult();
		
		int userId=brngusrreg.getId();
		
BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= '"+userId+"'",BrngUsrLogin.class).getSingleResult();
		
//BrngOrder brngorder=manager.createQuery("Select a From BrngOrder a where a.id= '"+brngorderdelivery.getBrngOrder().getId()+"'",BrngOrder.class).getSingleResult();

CommonUtility cmu=new CommonUtility();
String deliveryCode=cmu.generateDeliveryCode();
		int userLoginId=brngusrlogin.getId();

		System.out.println(" user Login Id"+userLoginId);
		System.out.println(" picked Id"+pickedId);
	Query query = manager
			.createQuery("UPDATE BrngOrder a SET a.brngLkpIsPicked.id =:isPicked,a.deliveryCode=:deliveryCode where id=:orderId");
			query.setParameter("isPicked",pickedId);
			query.setParameter("deliveryCode",deliveryCode);		
			query.setParameter("orderId",Integer.parseInt(inputdetails.get("orderId")));
			//query.setParameter("userLoginId",userLoginId);
			query.executeUpdate();
		//transaction.commit();
				
				
			message=UserMessages.getUserMessagesNew("OPIS");
			response.put("message", message);
			response.put("response", "1");

			PushHelper pushhelper=new PushHelper();
			pushhelper.pushToUser("notifybuyer",brngorder1.getBrngUsrLogin().getPlayerId() , "Order picked by serviceman. Order No",Integer.toString(brngorder1.getId()));
			
			OTPUtil otputil=new OTPUtil();
			brngusrotp.setOtpCode(otputil.sendOTP(brngusrregAttr.getPhoneNumber()));
				
			
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
	}*/

	
	public HashMap<String,String> pickAnOrder(String orderId,String pickupCode,String email){
		int pickedId=0;
		HashMap<String,String> response =new HashMap<>();
		String message="";
		try
		{
			BrngOrder brngorder1=manager.createQuery("Select a From BrngOrder a where a.id= "+Integer.parseInt(orderId),BrngOrder.class).getSingleResult();
			
			if(getCancelStatus(brngorder1.getId())==1)
			{
				message=UserMessages.getUserMessagesNew("OAC");
				response.put("message", message);
				response.put("response", "10");
				return response;
			}
			if(brngorder1.getBrngLkpIsPicked().getIsPicked().equalsIgnoreCase("Y"))
			{
				message=UserMessages.getUserMessagesNew("OAPI");
				response.put("message", message);
				response.put("response", "5");
				return response;
			}
			if(!brngorder1.getPickupCode().equalsIgnoreCase(pickupCode))
			{
				message=UserMessages.getUserMessagesNew("PNM");
				response.put("message", message);
				response.put("response", "15");
				return response;
			}
					
		BrngLkpIsPicked brngLkpIsPickedTypes = manager.createQuery("Select a From BrngLkpIsPicked a where a.isPicked='Y'",BrngLkpIsPicked.class).
				getSingleResult();
		
				pickedId=brngLkpIsPickedTypes.getId();
	
		BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+email+"'",BrngUsrReg.class).getSingleResult();
		
		int userId=brngusrreg.getId();
		
BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= '"+userId+"'",BrngUsrLogin.class).getSingleResult();
		
//BrngOrder brngorder=manager.createQuery("Select a From BrngOrder a where a.id= '"+brngorderdelivery.getBrngOrder().getId()+"'",BrngOrder.class).getSingleResult();

CommonUtility cmu=new CommonUtility();
String deliveryCode=cmu.generateDeliveryCode();
		int userLoginId=brngusrlogin.getId();

		System.out.println(" user Login Id"+userLoginId);
		System.out.println(" picked Id"+pickedId);
	Query query = manager
			.createQuery("UPDATE BrngOrder a SET a.brngLkpIsPicked.id =:isPicked,a.deliveryCode=:deliveryCode where id=:orderId");
			query.setParameter("isPicked",pickedId);
			query.setParameter("deliveryCode",deliveryCode);		
			query.setParameter("orderId",Integer.parseInt(orderId));
			//query.setParameter("userLoginId",userLoginId);
			query.executeUpdate();
		//transaction.commit();
				
				
			message=UserMessages.getUserMessagesNew("OPIS");
			response.put("message", message);
			response.put("response", "1");

			PushHelper pushhelper=new PushHelper();
			pushhelper.pushToUser("notifybuyer",brngorder1.getBrngUsrLogin().getPlayerId() , "Order picked by serviceman. Order No",Integer.toString(brngorder1.getId()));
			
			/*OTPUtil otputil=new OTPUtil();
			brngusrotp.setOtpCode(otputil.sendOTP(brngusrregAttr.getPhoneNumber()));*/
				
			
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
	
	public List<OrderDto> getListOfOpenOrders(Hashtable<String,String> locationData){
		
		//EntityTransaction transaction=null;
		int testRadius=200;
		int acceptanceId=0;
		TreeMap<Double,OrderDto> listOforders=new TreeMap<>();
		BrngUsrReg brngusrreg1=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+locationData.get("email")+"'",BrngUsrReg.class).getSingleResult();
		List<OrderDto>  dtoList1 = new ArrayList();
		if(brngusrreg1.getBrnglkpservicemanvalidated().getCode().equalsIgnoreCase("N"))
		{
			return dtoList1;
		}
		
		List<BrngLkpIsAccepted> brngLkpIsAcceptedTypes = manager.createQuery("Select a From BrngLkpIsAccepted a",BrngLkpIsAccepted.class).
				getResultList();
		List<OrderDeliveryDto> dtoList = new ArrayList();
		OrderDeliveryDto orderDeliveryDto = null;
		for(BrngLkpIsAccepted brngLkpIsAcceptedType:brngLkpIsAcceptedTypes){
			if(brngLkpIsAcceptedType.getIsAccepted().equalsIgnoreCase("N"))
			{
				acceptanceId=brngLkpIsAcceptedType.getId();
				break;
			}
		}
		BrngLkpIsCancelled brnglkpiscancelled = manager.createQuery("Select a From BrngLkpIsCancelled a where a.isCancelled='N'",BrngLkpIsCancelled.class).
				getSingleResult();
		int cancellationId=brnglkpiscancelled.getId();
		DistanceChecker distancechecker=new DistanceChecker();
		
		BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+locationData.get("email")+"'",BrngUsrReg.class).getSingleResult();
		
		int userId=brngusrreg.getId();
		BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= "+userId,BrngUsrLogin.class).getSingleResult();
		
		int loginId=brngusrlogin.getId();
		
		List<BrngOrder> brngOrders = manager.createQuery("Select a From BrngOrder a where a.isAccepted.id="+acceptanceId+" and a.brnglkpiscancelled.id="+cancellationId,BrngOrder.class).
				getResultList();
		
		/*List<BrngOrder> brngOrders = manager.createQuery("Select a From BrngOrder a where a.isAccepted.id="+acceptanceId+" and a.brnglkpiscancelled.id="+cancellationId+" and a.brngUsrLogin <> "+loginId,BrngOrder.class).
				getResultList();*/
		
		OrderDto orderDto = null;
		//System.out.println(brngOrders.get(0).getCompanyPrice());
		for(BrngOrder brngOrder:brngOrders){
			System.out.println("date :"+brngOrder.getOrderTime());
			Double distance=distancechecker.distanceBetween(brngOrder.getFromLatitude(),brngOrder.getFromLongitude(),locationData.get("lat"),locationData.get("lng"));
			if(distance<=testRadius)
			{
				System.out.println("Inside test radius");
				//listOforders.put(distance, orderDto);	
			orderDto = new OrderDto(brngOrder);
			orderDto.setOrderStatus("Open");
			orderDto.setPickDistance(distance);
			//listOforders.put(distance, orderDto);	
		dtoList1.add(orderDto);
			
			//Location Tracking is pending
			/*Query query = manager.
				      createQuery("update BrngOrderDeliveryAttr a set a.lat=:lat,a.lng=:lng where a.brngOrderDelivery.brngOrder.id="+brngOrder.getId());
			query.setParameter("lat",locationData.get("lat") );
			query.setParameter("lng",locationData.get("lng") );
			query.executeUpdate();*/
			
			}
		}
		  /*for (Map.Entry<Double,OrderDto> entry : listOforders.entrySet()) {
	            dtoList1.add(listOforders.get(entry.getKey()));
	    }*/
		//System.out.println("dtoList "+dtoList1.size());
		//OrderDeliveryService ods=new OrderDeliveryService();
		
		updateLocation(locationData);
		return dtoList1;
	}
	
public HashMap<String,String> updateLocation(Hashtable<String,String> locationData){
		
		//EntityTransaction transaction=null;
	HashMap<String,String> response =new HashMap<>();
	String message="";
	try
	{
		BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+locationData.get("email")+"'",BrngUsrReg.class).getSingleResult();
		
		int userId=brngusrreg.getId();
		System.out.println(" user Id"+userId);
		BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= "+userId,BrngUsrLogin.class).getSingleResult();
		
		int loginId=brngusrlogin.getId();
		System.out.println(" loginId"+loginId);
		Query query = manager.
			      createQuery("Select count(*) from BrngServicemanLocationDtls where brngUsrLogin.id="+loginId);
				long count = (long)query.getSingleResult();
				System.out.println(" count"+count);
				if(count>0)
				{
		
			
			//Location Tracking is pending
			 query = manager.
				      createQuery("update BrngServicemanLocationDtls a set a.lat=:lat,a.lng=:lng,a.address=:address where a.brngUsrLogin.id="+loginId);
			query.setParameter("lat",locationData.get("lat") );
			query.setParameter("lng",locationData.get("lng") );
			query.setParameter("address",locationData.get("address") );
			
			query.executeUpdate();
			
			}
				else
				{
					BrngServicemanLocationDtls brngservlocdtls=new BrngServicemanLocationDtls();
					brngservlocdtls.setAddress(locationData.get("address"));
					brngservlocdtls.setLat(locationData.get("lat"));
					brngservlocdtls.setLng(locationData.get("lng"));
					brngservlocdtls.setBrngUsrLogin(brngusrlogin);
					manager.persist(brngservlocdtls);
		}
				message=UserMessages.getUserMessages(1);
				response.put("message", message);
				response.put("response", "1");
				
	}
	catch(Exception e)
	{
		message=UserMessages.getUserMessages(-1);
		response.put("message", message);
		response.put("response", "-1");
	}
		return response;
	}

public int deleteAllTempOrders(int orderId,int usrLoginId)
{
	try
	{
		Query query = manager
				.createQuery("delete from BrngUpcomingOrder  a  where a.brngUsrLogin.id <> :loginId and a.brngOrder.id=:orderId");
				
				
				
				query.setParameter("orderId",orderId);
				query.setParameter("loginId",usrLoginId);
				query.executeUpdate();
				
				query = manager
						.createQuery("update BrngUpcomingOrder  a set a.status='S'  where a.brngUsrLogin.id = :loginId and a.brngOrder.id=:orderId");
						
						
						
						query.setParameter("orderId",orderId);
						query.setParameter("loginId",usrLoginId);
						query.executeUpdate();
				return 1;
	}
	catch(Exception e)
	{
		return -1;
	}
}



public HashMap<String,String> cancelOrderByServiceMan(HashMap<String,String> inputDetails){
	int pickedId=0;
	HashMap<String,String> response =new HashMap<>();
	String message="";
	try
	{
		BrngOrder brngorder1=manager.createQuery("Select a From BrngOrder a where a.id= "+inputDetails.get("orderId"),BrngOrder.class).getSingleResult();
		OrderDeliveryDao odao=new OrderDeliveryDao();
		int checkcancellationStatus= odao.checkCancellationStatus(brngorder1);
		if(checkcancellationStatus==0)
		{
			message=UserMessages.getUserMessages(10);
			response.put("message", message);
			response.put("response", "10");
		}
	
				
		BrngLkpIsCancelled brnglkpiscancelled = manager.createQuery("Select a From BrngLkpIsCancelled a where a.isCancelled='Y'",BrngLkpIsCancelled.class).
				getSingleResult();
		int cancelledId=brnglkpiscancelled.getId();
		
		BrngLkpIsAccepted brnglkpisaccepted = manager.createQuery("Select a From BrngLkpIsAccepted a where a.isAccepted.id='N'",BrngLkpIsAccepted.class).
				getSingleResult();
		int acceptanceId=brnglkpisaccepted.getId();
	BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+inputDetails.get("email")+"'",BrngUsrReg.class).getSingleResult();
	
	int userId=brngusrreg.getId();
	
BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= '"+userId+"'",BrngUsrLogin.class).getSingleResult();
	
	int userLoginId=brngusrlogin.getId();

	System.out.println(" user Login Id"+userLoginId);
Query query = manager
		.createQuery("UPDATE BrngOrder a SET a.isAccepted.id =:acceptanceId where id=:orderId");
		query.setParameter("acceptanceId",acceptanceId);
		
		
		query.setParameter("orderId",inputDetails.get("orderId"));
		query.setParameter("userLoginId",userLoginId);
		query.executeUpdate();
		
		 query = manager
				.createQuery("delete from  BrngOrderDelivery a where a.brngOrderid=:orderId");
				
				
				query.setParameter("orderId",inputDetails.get("orderId"));
				
				query.executeUpdate();	
		
	//transaction.commit();
			
			
		message=UserMessages.getUserMessages(1);
		response.put("message", message);
		response.put("response", "1");

		PushHelper pushhelper=new PushHelper();
		pushhelper.pushToUser("notifybuyer", brngorder1.getBrngUsrLogin().getPlayerId(), "Order Cancelled By ServiceMan",Integer.toString(brngorder1.getId()));

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

public int checkCancellationStatus(BrngOrder brngorder)
{
	
	if(brngorder.getBrnglkpiscancelled().getIsCancelled().equalsIgnoreCase("Y"))
	{
		return 0;
	}
	else
	{
		return 1;
	}
}


public HashMap<String,String> checkServiceManAvailability(HashMap<String,String> inputDetails){
	int pickedId=0;
	HashMap<String,String> response =new HashMap<>();
	String message="";
	try
	{
		//OrderDao odao=new OrderDao();
		ArrayList<Integer> listOfServiceMan=checkServiceMan(inputDetails.get("fromLat"),inputDetails.get("fromLng"));
		if(listOfServiceMan.size()==0)
		{
			response.put("response", "0");
			response.put("message", "No serviceman nearby");
		}
		else
		{
			response.put("response", "1");
			response.put("message","Serviceman Available nearby");
		}

		//PushHelper pushhelper=new PushHelper();
		//pushhelper.pushToUser("notifybuyer", Integer.toString(brngorderdelivery.getBrngOrder().getId()), "Order Picked By ServiceMan");

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


/*public HashMap<String,String> completeTrip(HashMap<String,String> inputDetails){
	int paidId=0;
	HashMap<String,String> response =new HashMap<>();
	String message="";
	try
	{
		BrngOrder brngorder1=manager.createQuery("Select a From BrngOrder a where a.id= "+inputDetails.get("orderId"),BrngOrder.class).getSingleResult();

		
		
		if(getCancelStatus(brngorder1.getId())==1)
		{
			message=UserMessages.getUserMessagesNew("OAC");
			response.put("message", message);
			response.put("response", "10");
			return response;
		}
		else if(brngorder1.getBrngLkpIsPaid().getIsPaid().equalsIgnoreCase("N"))
		{
			message=UserMessages.getUserMessagesNew("ONP");
			response.put("message", message);
			response.put("response", "7");
			return response;
		}
		else if(brngorder1.getBrngLkpIsPicked().getIsPicked().equalsIgnoreCase("N"))
		{
			message=UserMessages.getUserMessagesNew("ONPI");
			response.put("message", message);
			response.put("response", "5");
			return response;
		}
if(!brngorder1.getDeliveryCode().equalsIgnoreCase(inputDetails.get("deliveryCode")))
{
	message=UserMessages.getUserMessagesNew("DCNM");
	response.put("message", message);
	response.put("response", "12");
	return response;
}

	BrngLkpOrderDelStatus brnglkporderdelstatus = manager.createQuery("Select a From BrngLkpOrderDelStatus a where a.delStatus='Y'",BrngLkpOrderDelStatus.class).
			getSingleResult();
	int completeId=brnglkporderdelstatus.getId();
	
Query query = manager
		.createQuery("UPDATE BrngOrderDelivery a SET a.brngLkpOrderDelStatus.id =:iscomplete where a.brngOrder.id=:orderId");
		
		query.setParameter("orderId",Integer.parseInt(inputDetails.get("orderId")));
		query.setParameter("iscomplete",completeId);
		
		
		query.executeUpdate();
	//transaction.commit();
			
			
		message=UserMessages.getUserMessagesNew("TC");
		response.put("message", message);
		response.put("response", "1");
		PushHelper pushhelper=new PushHelper();
		//pushhelper.pushToUser("notifyservicemanoforders", brngorder1.getBrngUsrLogin().getPlayerId(), "Trip Completed",Integer.toString(brngorder1.getId()));
		pushhelper.pushToUser("notifybuyer", brngorder1.getBrngUsrLogin().getPlayerId(), "Order Completed. Order No",Integer.toString(brngorder1.getId()));
		
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
}*/

public HashMap<String,String> completeTrip(String orderId,String deliveryCode,String email){
	int paidId=0;
	HashMap<String,String> response =new HashMap<>();
	String message="";
	try
	{
		BrngOrder brngorder1=manager.createQuery("Select a From BrngOrder a where a.id= "+Integer.parseInt(orderId),BrngOrder.class).getSingleResult();

		/*if(brngorder1.getBrngLkpIsPicked().getIsPicked().equalsIgnoreCase("Y"))
		{
			message=UserMessages.getUserMessages(7);
			response.put("message", message);
			response.put("response", "7");
			return response;
		}*/
		
		if(getCancelStatus(brngorder1.getId())==1)
		{
			message=UserMessages.getUserMessagesNew("OAC");
			response.put("message", message);
			response.put("response", "10");
			return response;
		}
		else if(brngorder1.getBrngLkpIsPaid().getIsPaid().equalsIgnoreCase("N"))
		{
			message=UserMessages.getUserMessagesNew("ONP");
			response.put("message", message);
			response.put("response", "7");
			return response;
		}
		else if(brngorder1.getBrngLkpIsPicked().getIsPicked().equalsIgnoreCase("N"))
		{
			message=UserMessages.getUserMessagesNew("ONPI");
			response.put("message", message);
			response.put("response", "5");
			return response;
		}
if(!brngorder1.getDeliveryCode().equalsIgnoreCase(deliveryCode))
{
	message=UserMessages.getUserMessagesNew("DCNM");
	response.put("message", message);
	response.put("response", "12");
	return response;
}

	BrngLkpOrderDelStatus brnglkporderdelstatus = manager.createQuery("Select a From BrngLkpOrderDelStatus a where a.delStatus='Y'",BrngLkpOrderDelStatus.class).
			getSingleResult();
	int completeId=brnglkporderdelstatus.getId();
	
Query query = manager
		.createQuery("UPDATE BrngOrderDelivery a SET a.brngLkpOrderDelStatus.id =:iscomplete where a.brngOrder.id=:orderId");
		
		query.setParameter("orderId",Integer.parseInt(orderId));
		query.setParameter("iscomplete",completeId);
		
		
		query.executeUpdate();
	//transaction.commit();
			
			
		message=UserMessages.getUserMessagesNew("TC");
		response.put("message", message);
		response.put("response", "1");
		PushHelper pushhelper=new PushHelper();
		//pushhelper.pushToUser("notifyservicemanoforders", brngorder1.getBrngUsrLogin().getPlayerId(), "Trip Completed",Integer.toString(brngorder1.getId()));
		pushhelper.pushToUser("notifybuyer", brngorder1.getBrngUsrLogin().getPlayerId(), "Order Completed. Order No",Integer.toString(brngorder1.getId()));
		
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
public HashMap<String,String> orderPicUpload(MultipartFile orderPic,String email,String orderId,String pickupCode) throws FileNotFoundException{
	
	HashMap<String,String> response =new HashMap<>();
	String message="";
	 
	
      try {
    	
    	  
    	  HashMap<String,String>response1=pickAnOrder(orderId,pickupCode,email);
    	 /* if(response1.get("response").equalsIgnoreCase("1"))
    	  {*/
    	  if(response1.get("response").equalsIgnoreCase("1"))
    	  {
	BrngLkpFilePath brnglkpfilepath=manager.createQuery("Select a From BrngLkpFilePath a where a.type='O'",BrngLkpFilePath.class).getSingleResult();
	BrngOrder brngorder1=manager.createQuery("Select a From BrngOrder a where a.id= "+Integer.parseInt(orderId),BrngOrder.class).getSingleResult();
	
	BufferedOutputStream outputStream =null;
	String path=brnglkpfilepath.getFilePath();
	
	
	
	
	//System.out.println(userRegId);
	System.out.println(path);
	
	File file = new File(path+""+brngorder1.getId());
    file.mkdir();
    
    System.out.println("after file upload ");
   
    
    if (!orderPic.getOriginalFilename().isEmpty()) {
         outputStream = new BufferedOutputStream(
              new FileOutputStream(
                    new File(path+""+brngorder1.getId(), orderPic.getOriginalFilename())));
        outputStream.write(orderPic.getBytes());
        outputStream.flush();
        outputStream.close();
    }
       System.out.println("orderPic.getOriginalFilename()"+orderPic.getOriginalFilename());
    System.out.println("after file upload 1 ");
    saveNameInDB(brngorder1.getId(),orderPic.getOriginalFilename());
				
				/*BrngLkpServicemanValidated brnglkpservicemanvalidated= manager.createQuery("Select a From BrngLkpServicemanValidated a where a.code='Y'",BrngLkpServicemanValidated.class).getSingleResult();
				 query = manager
						.createQuery("UPDATE BrngUsrReg a SET a.brnglkpservicemanvalidated.id = :validatedId "
						+ "WHERE a.id= :id");	
				query.setParameter("validatedId",brnglkpservicemanvalidated.getId() );
				query.setParameter("id",userRegId);
				
				
				query.executeUpdate();*/
				message=UserMessages.getUserMessages(1);
				response.put("message", message);
				response.put("response", "1");
	}
    	  else
    	  {
    		  return response1;
    	  }
      
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


public HashMap<String,String> orderPicDropUpload(MultipartFile orderPic,String email,String orderId,String deliveryCode) throws FileNotFoundException{
	
	HashMap<String,String> response =new HashMap<>();
	String message="";
	 
	
      try {
    	
    	  /*HashMap<String,String> inputMap=new HashMap<>();
    	  inputMap.put("orderId", orderId);
    	  inputMap.put("deliveryCode", deliveryCode);
    	  HashMap<String,String>response1=completeTrip(inputMap);
    	  if(response1.get("response").equalsIgnoreCase("1"))
    	  {*/
    	  HashMap<String,String> response1=completeTrip(orderId,deliveryCode,email);
    	  if (response1.get("response").equalsIgnoreCase("1"))
    	  {
	BrngLkpFilePath brnglkpfilepath=manager.createQuery("Select a From BrngLkpFilePath a where a.type='D'",BrngLkpFilePath.class).getSingleResult();
	BrngOrder brngorder1=manager.createQuery("Select a From BrngOrder a where a.id= "+Integer.parseInt(orderId),BrngOrder.class).getSingleResult();
	
	BufferedOutputStream outputStream =null;
	String path=brnglkpfilepath.getFilePath();
	
	
	
	
	//System.out.println(userRegId);
	System.out.println(path);
	//System.out.println("q1 : "+path+""+userRegId+"\\aadhar\\");
	//File file = new File(path+""+userRegId);
	//file.mkdirs();
	File file = new File(path+""+brngorder1.getId());
    file.mkdir();
    
    System.out.println("after file upload ");
   
    
    if (!orderPic.getOriginalFilename().isEmpty()) {
         outputStream = new BufferedOutputStream(
              new FileOutputStream(
                    new File(path+""+brngorder1.getId(), orderPic.getOriginalFilename())));
        outputStream.write(orderPic.getBytes());
        outputStream.flush();
        outputStream.close();
    }
       System.out.println("orderPic.getOriginalFilename()"+orderPic.getOriginalFilename());
    System.out.println("after file upload 1 ");
    saveDeliveryNameInDB(brngorder1.getId(),orderPic.getOriginalFilename());
				
				message=UserMessages.getUserMessages(1);
				response.put("message", message);
				response.put("response", "1");
	}
    	  else
    	  {
    		  return response1;
    	  }
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



public ArrayList<Integer> checkServiceMan(String fromLat,String fromLng)
{
	double testRadius=200;
	StringBuffer listOfLogins=new StringBuffer("(");
	ArrayList<Integer> listOfLoginIds=new ArrayList<>();
	try
	{
		
		Query query= manager.createQuery("Select a From BrngUsrLogin a where a.logoutTime is  NULL");
		//Query query= manager.createNativeQuery("Select a From BRNG_USR_LOGIN a where a.logoutTime is  NULL");
		//List<BrngUsrLogin> orders = (List<BrngUsrLogin>) query.getResultList();
		
		
		List<BrngUsrLogin> brngusrlogins =manager.createQuery("Select a From BrngUsrLogin a where a.logoutTime is  NULL",BrngUsrLogin.class).getResultList();
		
		System.out.println("list :"+brngusrlogins.size());
		if(brngusrlogins.size()==0)
		{
			System.out.println("insode");
			listOfLogins.append(")");
			return listOfLoginIds;
		}
		
		for(int i=0;i<brngusrlogins.size();i++)
		{
			if(i==brngusrlogins.size()-1)
			{
				listOfLogins=listOfLogins.append(brngusrlogins.get(i).getId()+")");
			}
			else
			{
				listOfLogins=listOfLogins.append(brngusrlogins.get(i).getId()+",");
			}
		}
		System.out.println("listOfLogins "+listOfLogins);
		List<BrngServicemanLocationDtls> brngserviceManDetails = manager.createQuery("Select a From BrngServicemanLocationDtls a where a.brngUsrLogin.id in "+listOfLogins,BrngServicemanLocationDtls.class).getResultList();
				
		
		DistanceChecker distancechecker=new DistanceChecker();
		
		for(BrngServicemanLocationDtls BrngServicemanLocationDtl:brngserviceManDetails){
			Double distance=distancechecker.distanceBetween(fromLat,fromLng,BrngServicemanLocationDtl.getLat(),BrngServicemanLocationDtl.getLng());
			if(distance<=testRadius)
			{
				listOfLoginIds.add(BrngServicemanLocationDtl.getBrngUsrLogin().getId());
				/*BrngUpcomingOrder brngupcomingorder=new BrngUpcomingOrder();
				brngupcomingorder.setBrngOrder(brngorder);
				brngupcomingorder.setBrngUsrLogin(BrngServicemanLocationDtl.getBrngUsrLogin());
				brngupcomingorder.setStatus("O");
				manager.persist(brngupcomingorder);
*/				}
		}
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
	return listOfLoginIds;
}
/*public int insertOtpForPickUp(BrngOrder brngorder,String otp)
{
	try
	{
	Timestamp generatedTime=new Timestamp(System.currentTimeMillis());
	BrngOrderOtp brngorderotp=new BrngOrderOtp();
	brngorderotp.setBrngorder(brngorder);
	brngorderotp.setOtpCode(otp);
	brngorderotp.setEffectiveDate(generatedTime);
	manager.merge(brngorderotp);
	return 1;
	}
	catch(Exception e)
	{
		return 0;
	}*/
	
//}
public int getCancelStatus(int orderId)
{
	try
	{
		BrngOrder brngorder1=manager.createQuery("Select a From BrngOrder a where a.id= "+orderId,BrngOrder.class).getSingleResult();
		if(brngorder1.getBrnglkpiscancelled().getIsCancelled().equalsIgnoreCase("Y"))
			return 1;
	}
	catch(Exception e)
	{
		return -1;
	}
	return 0;
}

public int saveNameInDB(int orderId,String orderPic){
	
	
	Timestamp uploadedTime =null;

	//System.out.println("6 : " + registeredTime);
	try
	{
	/* transaction = manager.getTransaction();
	transaction.begin();*/
	//manager.persist(brngusrreg);
	//brngusrregattr.setBrngUsrReg(brngusrreg);
		uploadedTime =new Timestamp(System.currentTimeMillis());
		 BrngLkpFilePath brnglkpfilepath=manager.createQuery("Select a From BrngLkpFilePath a where a.type='M'",BrngLkpFilePath.class).getSingleResult();
		
		String filePath=brnglkpfilepath.getFilePath();
		
		// BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.id="+usrRegId,BrngUsrReg.class).getSingleResult();
		// CommonsMultipartFile aFile =fileUpload[0];
		 Query query = manager
					.createQuery("UPDATE BrngOrder a SET a.orderPic =:orderPic where a.id=:orderId ");
					query.setParameter("orderPic",filePath+orderId+"/"+orderPic);
					query.setParameter("orderId",orderId);
					
					query.executeUpdate();
		
	//	manager.merge(brngusrfiles);
		return 1;
	}
	
	
	catch(Exception e)
	{
		e.printStackTrace();
		//transaction.rollback();
		return -1;
	}
	
}


public int saveDeliveryNameInDB(int orderId,String orderPic){
	
	
	Timestamp uploadedTime =null;

	//System.out.println("6 : " + registeredTime);
	try
	{
	/* transaction = manager.getTransaction();
	transaction.begin();*/
	//manager.persist(brngusrreg);
	//brngusrregattr.setBrngUsrReg(brngusrreg);
		uploadedTime =new Timestamp(System.currentTimeMillis());
		 BrngLkpFilePath brnglkpfilepath=manager.createQuery("Select a From BrngLkpFilePath a where a.type='N'",BrngLkpFilePath.class).getSingleResult();
		
		String filePath=brnglkpfilepath.getFilePath();
		
		// BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.id="+usrRegId,BrngUsrReg.class).getSingleResult();
		// CommonsMultipartFile aFile =fileUpload[0];
		 Query query = manager
					.createQuery("UPDATE BrngOrder a SET a.orderDropPic =:orderPic where a.id=:orderId ");
					query.setParameter("orderPic",filePath+orderId+"/"+orderPic);
					query.setParameter("orderId",orderId);
					
					query.executeUpdate();
		
	//	manager.merge(brngusrfiles);
		return 1;
	}
	
	
	catch(Exception e)
	{
		e.printStackTrace();
		//transaction.rollback();
		return -1;
	}
	
}
/*public float getTotalDistance(String email)
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
		List<BrngOrderDelivery> brngOrderDelivery=manager.createQuery("Select a From BrngOrderDelivery a where a.brngUsrLogin.id= "+userLoginId,BrngOrderDelivery.class).getResultList();
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
}*/



}
