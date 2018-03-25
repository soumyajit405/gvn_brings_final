package com.gvn.brings.dao;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gvn.brings.dto.OrderDeliveryDto;
import com.gvn.brings.dto.OrderDto;
import com.gvn.brings.dto.RegistrationDto;
import com.gvn.brings.model.BrngLkpIsAccepted;
import com.gvn.brings.model.BrngLkpIsCancelled;
import com.gvn.brings.model.BrngLkpIsPaid;
import com.gvn.brings.model.BrngLkpIsPicked;
import com.gvn.brings.model.BrngLkpIsRetry;
import com.gvn.brings.model.BrngLkpOrderDelStatus;
import com.gvn.brings.model.BrngLkpPayPercent;
import com.gvn.brings.model.BrngLkpPayType;
import com.gvn.brings.model.BrngLkpUsrRegStatus;
import com.gvn.brings.model.BrngLkpUsrRegType;
import com.gvn.brings.model.BrngLkpUsrType;
import com.gvn.brings.model.BrngOrder;
import com.gvn.brings.model.BrngOrderDelivery;
import com.gvn.brings.model.BrngServicemanLocationDtls;
import com.gvn.brings.model.BrngUsrFiles;
import com.gvn.brings.model.BrngUsrLogin;
import com.gvn.brings.model.BrngUsrPassChange;
import com.gvn.brings.model.BrngUsrReg;
import com.gvn.brings.model.BrngUsrRegAttr;
import com.gvn.brings.util.DistanceChecker;
import com.gvn.brings.util.PushHelper;
import com.gvn.brings.util.UserMessages;

@Transactional
@Repository
public class OrderDao {

	@PersistenceContext
    private EntityManager manager;
	
	public List<OrderDto> getpayPercent(){
		List<BrngLkpPayPercent> brngLkpPayPercents = manager.createQuery("Select a From BrngLkpPayPercent a",BrngLkpPayPercent.class).
				getResultList();
		List<OrderDto> dtoList = new ArrayList();
		OrderDto orderDto = null;
		for(BrngLkpPayPercent brngLkpPayPercent:brngLkpPayPercents){
			orderDto = new OrderDto(brngLkpPayPercent);
			dtoList.add(orderDto);
		}
		return dtoList;
	}
	
	public List<OrderDto> getIsPaidTypes(){
		List<BrngLkpIsPaid> brngLkpIsPaidTypes = manager.createQuery("Select a From BrngLkpIsPaid a",BrngLkpIsPaid.class).
				getResultList();
		List<OrderDto> dtoList = new ArrayList();
		OrderDto orderDto = null;
		for(BrngLkpIsPaid brngLkpIsPaid:brngLkpIsPaidTypes){
			orderDto = new OrderDto(brngLkpIsPaid);
			dtoList.add(orderDto);
		}
		return dtoList;
	}
	
	public HashMap<String,String> bookAnOrder(BrngOrder brngorder){
		
		HashMap<String,String> response=new HashMap<>();
		String message="";
		try
		{
			
		BrngLkpIsPaid brngispaid = manager.createQuery("Select a From BrngLkpIsPaid a where a.isPaid='N'",BrngLkpIsPaid.class).getSingleResult();
		BrngLkpIsPicked brngispicked = manager.createQuery("Select a From BrngLkpIsPicked a where a.isPicked='N'",BrngLkpIsPicked.class).getSingleResult();
		BrngLkpIsAccepted brngisaccepted = manager.createQuery("Select a From BrngLkpIsAccepted a where a.isAccepted.isAccepted='N'",BrngLkpIsAccepted.class).getSingleResult();
		BrngLkpIsCancelled brngiscancelled = manager.createQuery("Select a From BrngLkpIsCancelled a where a.isCancelled='N'",BrngLkpIsCancelled.class).getSingleResult();
		BrngLkpIsRetry brnglkpisretry = manager.createQuery("Select a From BrngLkpIsRetry a where a.isRetry='N'",BrngLkpIsRetry.class).getSingleResult();
		BrngLkpPayType brnglkppaytype = manager.createQuery("Select a From BrngLkpPayType a where a.type='N'",BrngLkpPayType.class).getSingleResult();
BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+brngorder.getBrngUsrLogin().getBrngUsrReg().getEmailId()+"'",BrngUsrReg.class).getSingleResult();
		
		int userId=brngusrreg.getId();
		
BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= '"+userId+"'",BrngUsrLogin.class).getSingleResult();
		
		int userLoginId=brngusrlogin.getId();
	//	BrngLkpIsComplete brnglkpiscomplete = manager.createQuery("Select a From BrngLkpIsComplete a where a.isComplete='N'",BrngLkpIsComplete.class).getSingleResult();
		Query query = manager.
			      createQuery("Select count(*) from BrngOrder a where a.brngUsrLogin.id="+userLoginId);
				long count = (long)query.getSingleResult();
				System.out.println("Count of Order Sequence " +count);
		Timestamp ts=new Timestamp(System.currentTimeMillis());

		
		/* transaction = manager.getTransaction();
		transaction.begin();*/
			brngorder.setBrngLkpIsPaid(brngispaid);
			brngorder.setBrngLkpIsPicked(brngispicked);
			brngorder.setIsAccepted(brngisaccepted);
			brngorder.setBrnglkpisretry(brnglkpisretry);
		//	brngorder.setBrnglkpiscomplete(brnglkpiscomplete);
			
			brngorder.setBrnglkpiscancelled(brngiscancelled);
			System.out.println("order seq"+(int)count+1);
			brngorder.setOrderSeq((int)count+1);
			brngorder.getBrngUsrLogin().setId(userLoginId);
			ArrayList<BigDecimal> payValues=calculatePrice(brngorder.getTotalPrice());
			brngorder.setServicePrice(payValues.get(0));
			brngorder.setCompanyPrice(payValues.get(1));
			brngorder.setOrderTime(ts);
			brngorder.setTripRating(-1);
			brngorder.setBrnglkppaytype(brnglkppaytype);
		manager.persist(brngorder);
		//transaction.commit();
		
		
		 query= manager.createQuery("Select a.id From BrngOrder a where a.orderSeq="+((int)count+1)+" and a.brngUsrLogin.id="+userLoginId);
		 int orderId = (Integer)query.getSingleResult();
		//int orderId=brngordertemp.getId();
		PushHelper pushhelper=new PushHelper();
		pushhelper.pushToAllServiceMan(getListOfServiceMan(brngorder.getFromLatitude(), brngorder.getFromLongitude()), Integer.toString(orderId));
		message=UserMessages.getUserMessagesNew("OPS");
		response.put("message", message);
		response.put("response", Integer.toString(orderId));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//transaction.rollback();
			message=UserMessages.getUserMessagesNew("E");
			response.put("message", message);
			response.put("response", "1");
		}
		return response;
	}
	

	public List<OrderDto> getOrdersByDate(Hashtable<String,String> inputDetails){
		
		OrderDao odao=new OrderDao();
		
BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+inputDetails.get("email")+"'",BrngUsrReg.class).getSingleResult();
List<OrderDto> dtoList = new ArrayList();	
try{
	

		int userId=brngusrreg.getId();
		
BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= '"+userId+"'",BrngUsrLogin.class).getSingleResult();
		
		int userLoginId=brngusrlogin.getId();
		
		BrngLkpIsRetry brnglkpsiretry = manager.createQuery("Select a From BrngLkpIsRetry a where a.isRetry='Y'",BrngLkpIsRetry.class).
				getSingleResult();
		
		System.out.println(userId);
		
		BrngLkpIsAccepted brnglkpisacceted = manager.createQuery("Select a From BrngLkpIsAccepted a where a.isAccepted='Y'",BrngLkpIsAccepted.class).
				getSingleResult();
		
		System.out.println("Select a From BrngOrder a where a.brngUsrLogin.id="+userLoginId+" and order_time between date('"+inputDetails.get("startDate")+ "') and DATE_ADD(date('"+inputDetails.get("endDate")+ "'),INTERVAL 1 DAY)");
		//List<BrngOrder> brngOrders = manager.createQuery("Select a From BrngOrder a where a.brngUsrLogin.id="+userLoginId+" and a.orderTime between date('"+inputDetails.get("startDate")+ "') and date('"+inputDetails.get("endDate")+ "')+1 ",BrngOrder.class).
		List<BrngOrder> brngOrders = manager.createQuery("Select a From BrngOrder a where a.brngUsrLogin.id="+userLoginId+" and (a.brnglkpisretry.id="+brnglkpsiretry.getId()+ " or a.isAccepted.id="+brnglkpisacceted.getId()+") and a.orderTime between date('"+inputDetails.get("startDate")+ "') and date('"+inputDetails.get("endDate")+ "')+1  order by a.orderTime desc",BrngOrder.class).
				
				getResultList();
	
		OrderDto orderDto = null;
		//System.out.println(brngOrders.get(0).getCompanyPrice());
		for(BrngOrder brngOrder:brngOrders){
			//odao.getCodes("A", brngOrder.getIsAccepted());
			
			orderDto = new OrderDto(brngOrder);
			dtoList.add(orderDto);
		}
}
catch(Exception e)
{
	return dtoList;
}
		return dtoList;
	}
	
public List<OrderDto> getOrdersByDateServiceMan(Hashtable<String,String> inputDetails){
		
	List<OrderDto> dtoList1 = new ArrayList();
	
		OrderDao odao=new OrderDao();
	try
	{
BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+inputDetails.get("email")+"'",BrngUsrReg.class).getSingleResult();
		
		int userId=brngusrreg.getId();
		
BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= '"+userId+"'",BrngUsrLogin.class).getSingleResult();
		
		int userLoginId=brngusrlogin.getId();
		BrngLkpOrderDelStatus brnglkporderdelstatus = manager.createQuery("Select a From BrngLkpOrderDelStatus a where a.delStatus='Y'",BrngLkpOrderDelStatus.class).
				getSingleResult();
		
		
		int completeId=brnglkporderdelstatus.getId();
		//Query query= manager.createQuery("Select a From BrngOrder a , BrngOrderDelivery b where a.id=b.brngOrder.id and b.brngUsrLogin.id="+userLoginId+ " and a.orderTime between date('"+inputDetails.get("startDate")+ "') and date('"+inputDetails.get("endDate")+"')");
		Query query= manager.createQuery("Select a From BrngOrder a , BrngOrderDelivery b where a.id=b.brngOrder.id and b.brngUsrLogin.id="+userLoginId+ " and a.orderTime between date('"+inputDetails.get("startDate")+ "') and date('"+inputDetails.get("endDate")+"') +1 order by a.orderTime desc");
		
		List<BrngOrder> orders = (List<BrngOrder>) query.getResultList();
		
		
		//System.out.println("query -Select a From BrngOrder a where a.brngUsrLogin.id="+userLoginId+" and orderTime >= "+inputDetails.get("startDate")+ " and orderTime <="+inputDetails.get("endDate"));
		
		
		OrderDto orderdto = null;
		//System.out.println(brngOrders.get(0).getCompanyPrice());
		for(BrngOrder order:orders){
			
			orderdto = new OrderDto(order);
			dtoList1.add(orderdto);
		}
		
		/*System.out.println(userId);
		System.out.println("query -Select a From BrngOrder a where a.brngUsrLogin.id="+userLoginId+" and orderTime >= "+inputDetails.get("startDate")+ " and orderTime <="+inputDetails.get("endDate"));
		List<BrngOrder> brngOrders = manager.createQuery("Select a From BrngOrder a where a.brngUsrLogin.id="+userLoginId+" and order_time between date('"+inputDetails.get("startDate")+ "') and date('"+inputDetails.get("endDate")+ "')",BrngOrder.class).
				getResultList();
		List<OrderDto> dtoList = new ArrayList();
		OrderDto orderDto = null;
		//System.out.println(brngOrders.get(0).getCompanyPrice());
		for(BrngOrder brngOrder:brngOrders){
			//odao.getCodes("A", brngOrder.getIsAccepted());
			
			orderDto = new OrderDto(brngOrder);
			dtoList.add(orderDto);
		}*/
	}
	catch(Exception e)
	{
		return dtoList1;
	}
		//return dtoList;
		return dtoList1;
	}
	
	
	public List<OrderDto> getCurrentOrdersCustomers(Hashtable<String,String> inputDetails){
		
		List<OrderDeliveryDto> dtoList = new ArrayList();
		List<OrderDto> dtoList1 = new ArrayList();
		
		int acceptanceId=0;
		int nonacceptanceId=0;
		try
		{
		BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+inputDetails.get("email")+"'",BrngUsrReg.class).getSingleResult();
				
				int userId=brngusrreg.getId();
				
		BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= '"+userId+"'",BrngUsrLogin.class).getSingleResult();
				
				int userLoginId=brngusrlogin.getId();
				
				System.out.println(userId);
				List<BrngLkpIsAccepted> brngLkpIsAcceptedTypes = manager.createQuery("Select a From BrngLkpIsAccepted a",BrngLkpIsAccepted.class).
						getResultList();
				
				OrderDeliveryDto orderDeliveryDto = null;
				for(BrngLkpIsAccepted brngLkpIsAcceptedType:brngLkpIsAcceptedTypes){
					if(brngLkpIsAcceptedType.getIsAccepted().equalsIgnoreCase("N"))
					{
						nonacceptanceId=brngLkpIsAcceptedType.getId();
					}
					if(brngLkpIsAcceptedType.getIsAccepted().equalsIgnoreCase("Y"))
					{
						acceptanceId=brngLkpIsAcceptedType.getId();
						
						
					}
				}
				BrngLkpIsRetry brnglkpsiretry = manager.createQuery("Select a From BrngLkpIsRetry a where a.isRetry='Y'",BrngLkpIsRetry.class).
						getSingleResult();
				System.out.println("Retry "+brnglkpsiretry.getId());
				BrngLkpIsCancelled brnglkpiscancelled = manager.createQuery("Select a From BrngLkpIsCancelled a where a.isCancelled='N'",BrngLkpIsCancelled.class).
						getSingleResult();
				int cancellationId=brnglkpiscancelled.getId();
				System.out.println("query -Select a From BrngOrder a where a.brngUsrLogin.id="+userLoginId+" and orderTime >= "+inputDetails.get("startDate")+ " and orderTime <="+inputDetails.get("endDate"));
				
				BrngLkpOrderDelStatus brnglkporderdelstatus = manager.createQuery("Select a From BrngLkpOrderDelStatus a where a.delStatus='N'",BrngLkpOrderDelStatus.class).
						getSingleResult();
				int completeId=brnglkporderdelstatus.getId();
				System.out.println("nonaccept "+nonacceptanceId);
				//Query query= manager.createQuery("Select a From BrngOrder a , BrngOrderDelivery b where a.id=b.brngOrder.id and b.brngLkpOrderDelStatus.id="+completeId+" and a.brngUsrLogin.id="+userLoginId+" and a.isAccepted.id="+acceptanceId+" and a.brnglkpiscancelled.id="+cancellationId + "union Select a From BrngOrder a , BrngOrderDelivery b where a.id=b.brngOrder.id and b.brngLkpOrderDelStatus.id="+completeId+" and a.brngUsrLogin.id="+userLoginId+" and a.brnglkpisretry.id="+brnglkpsiretry.getId()+" and a.brnglkpiscancelled.id="+cancellationId);
				//Query query= manager.createQuery("Select distinct a From BrngOrder a , BrngOrderDelivery b where a.id=b.brngOrder.id and b.brngLkpOrderDelStatus.id="+completeId+" and a.brngUsrLogin.id="+userLoginId+" and a.isAccepted.id="+acceptanceId+" and a.brnglkpiscancelled.id="+cancellationId +"  or (a.brngUsrLogin.id="+userLoginId+"  and a.brnglkpisretry.id="+brnglkpsiretry.getId()+")");
				Query query= manager.createQuery("Select distinct a From BrngOrder a , BrngOrderDelivery b where a.id=b.brngOrder.id and b.brngLkpOrderDelStatus.id="+completeId+" and a.brngUsrLogin.id="+userLoginId+" and a.isAccepted.id="+acceptanceId+" and a.brnglkpiscancelled.id="+cancellationId+" order by a.orderTime desc"  );
				 
						System.out.println("query ");
				List<BrngOrder> orders = (List<BrngOrder>) query.getResultList();
				
				
				//System.out.println("query -Select a From BrngOrder a where a.brngUsrLogin.id="+userLoginId+" and orderTime >= "+inputDetails.get("startDate")+ " and orderTime <="+inputDetails.get("endDate"));
				
				
				OrderDto orderdto = null;
				//System.out.println(brngOrders.get(0).getCompanyPrice());
				for(BrngOrder order:orders){
					
					orderdto = new OrderDto(order);
					dtoList1.add(orderdto);
				}
			query=manager.createQuery(" Select distinct a From BrngOrder a  where    a.brnglkpisretry.id="+brnglkpsiretry.getId()+ " and a.brngUsrLogin.id="+userLoginId+" and a.brnglkpiscancelled.id="+cancellationId +" and a.isAccepted="+nonacceptanceId );
			 orders = (List<BrngOrder>) query.getResultList();
			  orderdto = null;
			  for(BrngOrder order:orders){
					
					orderdto = new OrderDto(order);
					dtoList1.add(orderdto);
				}
				/*List<BrngOrder> brngOrders = manager.createQuery("Select a From BrngOrder a where a.brngUsrLogin.id="+userLoginId+" and a.isAccepted.id="+acceptanceId+" and a.brnglkpiscancelled.id="+cancellationId,BrngOrder.class).
						getResultList();
				List<OrderDto> dtoList1 = new ArrayList();
				OrderDto orderDto = null;
				//System.out.println(brngOrders.get(0).getCompanyPrice());
				for(BrngOrder brngOrder:brngOrders){
					
					orderDto = new OrderDto(brngOrder);
					dtoList1.add(orderDto);
				}*/
		}
		catch(Exception e)
		{
			return dtoList1;
		}
				return dtoList1;
			}
			
	
	
		public List<OrderDto> getPastOrdersCustomers(Hashtable<String,String> inputDetails){
		
		int completedId=0;
		List<OrderDto> dtoList1 = new ArrayList();
		List<OrderDeliveryDto> dtoList = new ArrayList();
		try
		{
		StringBuffer listorders=new StringBuffer("(");
		BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+inputDetails.get("email")+"'",BrngUsrReg.class).getSingleResult();
				
				int userId=brngusrreg.getId();
				
		BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= '"+userId+"'",BrngUsrLogin.class).getSingleResult();
				
				int userLoginId=brngusrlogin.getId();
				
				System.out.println(userId);
			
				
				OrderDeliveryDto orderDeliveryDto = null;
				
				BrngLkpOrderDelStatus brnglkporderdelstatus = manager.createQuery("Select a From BrngLkpOrderDelStatus a where a.delStatus='Y'",BrngLkpOrderDelStatus.class).
						getSingleResult();
				int completeId=brnglkporderdelstatus.getId();
				
				Query query= manager.createQuery("Select a From BrngOrder a , BrngOrderDelivery b where a.id=b.brngOrder.id and b.brngLkpOrderDelStatus.id="+completeId+" and a.brngUsrLogin.id="+userLoginId+" order by a.orderTime desc");
						
				List<BrngOrder> orders = (List<BrngOrder>) query.getResultList();
				
				
				//System.out.println("query -Select a From BrngOrder a where a.brngUsrLogin.id="+userLoginId+" and orderTime >= "+inputDetails.get("startDate")+ " and orderTime <="+inputDetails.get("endDate"));
				
				
				OrderDto orderdto = null;
				//System.out.println(brngOrders.get(0).getCompanyPrice());
				for(BrngOrder order:orders){
					
					orderdto = new OrderDto(order);
					orderdto.setOrderStatus("Completed");
					dtoList1.add(orderdto);
				}
		}catch(Exception e)
		{
			return dtoList1;
		}
		return dtoList1;
			}
		
		

		public List<OrderDto> getPastOrdersServiceMan(Hashtable<String,String> inputDetails){
		
		int completedId=0;
		BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+inputDetails.get("email")+"'",BrngUsrReg.class).getSingleResult();
				
				int userId=brngusrreg.getId();
				
		BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= '"+userId+"'",BrngUsrLogin.class).getSingleResult();
				
				int userLoginId=brngusrlogin.getId();
				
				System.out.println(userId);
				BrngLkpOrderDelStatus brnglkporderdelstatus = manager.createQuery("Select a From BrngLkpOrderDelStatus a where a.delStatus='Y'",BrngLkpOrderDelStatus.class).
						getSingleResult();
				int completeId=brnglkporderdelstatus.getId();
				
				//System.out.println("query -Select a From BrngOrder a where a.brngUsrLogin.id="+userLoginId+" and orderTime >= "+inputDetails.get("startDate")+ " and orderTime <="+inputDetails.get("endDate"));
				Query query= manager.createQuery("Select a From BrngOrder a , BrngOrderDelivery b where a.id=b.brngOrder.id and b.brngLkpOrderDelStatus.id="+completeId+" and b.brngUsrLogin.id="+userLoginId+" order by a.orderTime desc");
				
				List<BrngOrder> orders = (List<BrngOrder>) query.getResultList();
				
				
				//System.out.println("query -Select a From BrngOrder a where a.brngUsrLogin.id="+userLoginId+" and orderTime >= "+inputDetails.get("startDate")+ " and orderTime <="+inputDetails.get("endDate"));
				
				List<OrderDto> dtoList1 = new ArrayList();
				OrderDto orderdto = null;
				//System.out.println(brngOrders.get(0).getCompanyPrice());
				for(BrngOrder order:orders){
					
					orderdto = new OrderDto(order);
					orderdto.setOrderStatus("Completed");
					dtoList1.add(orderdto);
				}
				return dtoList1;
			}
public List<OrderDto> getCurrentOrdersService(Hashtable<String,String> inputDetails){
		
		int deliveredId=0;
		String orderList="(";
		List<OrderDto> dtoList1 = new ArrayList();
		try
		{
		BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+inputDetails.get("email")+"'",BrngUsrReg.class).getSingleResult();
				
				int userId=brngusrreg.getId();
				
		BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= '"+userId+"'",BrngUsrLogin.class).getSingleResult();
				
				int userLoginId=brngusrlogin.getId();
			
				System.out.println(userId);
				BrngLkpOrderDelStatus brngLkpDeliveredTypes = manager.createQuery("Select a From BrngLkpOrderDelStatus a where a.delStatus='N'",BrngLkpOrderDelStatus.class).
						getSingleResult();
				deliveredId= brngLkpDeliveredTypes.getId();
				System.out.println("deliver ID" +deliveredId);
				
				List<BrngOrderDelivery> brngDelOrders = manager.createQuery("Select a From BrngOrderDelivery a where a.brngUsrLogin.id="+userLoginId+" and a.brngLkpOrderDelStatus.id="+deliveredId,BrngOrderDelivery.class).
						getResultList();
				
				for(int i=0;i<brngDelOrders.size();i++ )
				{
					if(i==brngDelOrders.size()-1)
					{
						orderList=orderList+brngDelOrders.get(i).getBrngOrder().getId()+")";
					}
					else
					{
					orderList=orderList+brngDelOrders.get(i).getBrngOrder().getId()+",";
					}
				}
				
				if(orderList.equalsIgnoreCase("("))
				{
					return dtoList1;
				}
				System.out.println("orders list"+orderList);
				BrngLkpIsCancelled brnglkpiscancelled = manager.createQuery("Select a From BrngLkpIsCancelled a where a.isCancelled='N'",BrngLkpIsCancelled.class).
						getSingleResult();
				int cancellationId=brnglkpiscancelled.getId();
				List<BrngOrder> brngOrders = manager.createQuery("Select a From BrngOrder a where  a.id in "+orderList +" and a.brnglkpiscancelled.id="+cancellationId+" order by a.orderTime desc ",BrngOrder.class).
						getResultList();
			
				OrderDto orderDto = null;
				//System.out.println(brngOrders.get(0).getCompanyPrice());
				for(BrngOrder brngOrder:brngOrders){
					
					orderDto = new OrderDto(brngOrder);
					dtoList1.add(orderDto);
				}
		}
		catch(Exception e)
		{
			return dtoList1;
		}
		return dtoList1;
			}
			

	public List<OrderDto> getOrdersById(String email){
		
		
		BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+email+"'",BrngUsrReg.class).getSingleResult();
				
				int userId=brngusrreg.getId();
				
		BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= '"+userId+"'",BrngUsrLogin.class).getSingleResult();
				
				int userLoginId=brngusrlogin.getId();
				
				System.out.println(userId);
				List<BrngOrder> brngOrders = manager.createQuery("Select a From BrngOrder a where a.brngUsrLogin.id="+userLoginId,BrngOrder.class).
						getResultList();
				List<OrderDto> dtoList = new ArrayList();
				OrderDto orderDto = null;
				System.out.println(brngOrders.get(0).getCompanyPrice());
				for(BrngOrder brngOrder:brngOrders){
					
					orderDto = new OrderDto(brngOrder);
					dtoList.add(orderDto);
				}
				return dtoList;
			}
	
	
public List<OrderDto> getOrderDetailsById(int orderId){
		
	List<OrderDto> dtoList = new ArrayList();
	try
	{
				List<BrngOrder> brngOrders = manager.createQuery("Select a From BrngO"
						+ "rder a where a.id="+orderId,BrngOrder.class).
						getResultList();
				
				OrderDto orderDto = null;
				System.out.println(brngOrders.get(0).getCompanyPrice());
				for(BrngOrder brngOrder:brngOrders){
					
					orderDto = new OrderDto(brngOrder);
					dtoList.add(orderDto);
				}
				if(orderDto.getIsAccepted().equalsIgnoreCase("Y"))
				{
					Query query = manager.
						      createQuery("Select count(*) from BrngOrderDelivery a where a.brngOrder.id="+orderId);
							long count = (long)query.getSingleResult();
							if(count==0)
							{
								
							}
							else
							{
				BrngOrderDelivery brngOrderDelivery = manager.createQuery("Select a From BrngOrderDelivery a where a.brngOrder.id="+orderId,BrngOrderDelivery.class).
						getSingleResult();
				int loginId=brngOrderDelivery.getBrngUsrLogin().getId();
				BrngUsrLogin brngusrlogin= manager.createQuery("Select a From BrngUsrLogin a where a.id="+loginId,BrngUsrLogin.class).
						getSingleResult();
				int regId=brngusrlogin.getBrngUsrReg().getId();
				
				
				BrngUsrRegAttr brngusrregattr= manager.createQuery("Select a From BrngUsrRegAttr a where a.brngUsrReg.id="+regId,BrngUsrRegAttr.class).
						getSingleResult();
				
				BrngServicemanLocationDtls brngservicemandlocationdtls=manager.createQuery("Select a From BrngServicemanLocationDtls a where a.brngUsrLogin.id="+loginId,BrngServicemanLocationDtls.class).
						getSingleResult();
				BrngUsrFiles brngusrfiles=manager.createQuery("Select a From BrngUsrFiles a where a.brngUsrReg.id="+brngusrregattr.getBrngUsrReg().getId(),BrngUsrFiles.class).
						getSingleResult();
				orderDto.setDriverName(brngusrregattr.getFirstName()+ " "+brngusrregattr.getLastName());
				orderDto.setDriverNumber(brngusrregattr.getPhoneNumber());
				orderDto.setDriverCurrentLat(brngservicemandlocationdtls.getLat());
				orderDto.setDriverCurrentLng(brngservicemandlocationdtls.getLng());
				orderDto.setDeliveryStatus(brngOrderDelivery.getBrngLkpOrderDelStatus().getDelStatus());
				orderDto.setDriverImage(brngusrfiles.getImage());
							}
				}
	}
	catch(Exception e)
	{
		return dtoList;
	}
						return dtoList;
			}

			
	
	public ArrayList<BigDecimal> calculatePrice(BigDecimal totalPrice )
	{
		System.out.println(" Price ");
		MathContext mc = new MathContext(4);
		BigDecimal constantDivisor=new BigDecimal("100");
		List<OrderDto> getPayvalue=getpayPercent();
		BigDecimal payPercent=getPayvalue.get(0).getPayPercent();
		ArrayList<BigDecimal> listOfPayValues=new ArrayList<>();
		listOfPayValues.add((constantDivisor.subtract(payPercent).multiply(totalPrice)).divide(constantDivisor));
		listOfPayValues.add((payPercent.multiply(totalPrice)).divide(constantDivisor));
		System.out.println(" listOfPayValues "+listOfPayValues);
		return listOfPayValues;
}
	
	

	public HashMap<String,String> confirmPayment(Hashtable<String,String> inputDetails){
		int paidId=0;
		HashMap<String,String> response =new HashMap<>();
		String message="";
		String paidStatus="";
		try
		{
			if(getCancelStatus(Integer.parseInt(inputDetails.get("orderId")))==1)
			{
				message=UserMessages.getUserMessagesNew("OAC");
				response.put("message", message);
				response.put("response", "10");
				return response;
			}
			List<BrngLkpIsPaid> brngLkpIsPaidTypes = manager.createQuery("Select a From BrngLkpIsPaid a",BrngLkpIsPaid.class).
					getResultList();
			List<OrderDeliveryDto> dtoList = new ArrayList();
			OrderDeliveryDto orderDeliveryDto = null;
			for(BrngLkpIsPaid brngLkpIsPaidType:brngLkpIsPaidTypes){
				if(brngLkpIsPaidType.getIsPaid().equalsIgnoreCase("Y"))
				{
					paidId=brngLkpIsPaidType.getId();
					paidStatus=brngLkpIsPaidType.getIsPaid();
					break;
				}
			}
			BrngOrder brngorder1=manager.createQuery("Select a From BrngOrder a where a.id= "+inputDetails.get("orderId"),BrngOrder.class).getSingleResult();
			System.out.println( "  " + brngorder1.getBrngLkpIsPaid().getIsPaid());
			if(brngorder1.getBrngLkpIsPaid().getIsPaid().equalsIgnoreCase(paidStatus))
			{
				message=UserMessages.getUserMessagesNew("OAP");
				response.put("message", message);
				response.put("response", "7");
				return response;
			}

		
		BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+inputDetails.get("email")+"'",BrngUsrReg.class).getSingleResult();
		
		int userId=brngusrreg.getId();
		
BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= '"+userId+"'",BrngUsrLogin.class).getSingleResult();
		
		int userLoginId=brngusrlogin.getId();
		
		BrngLkpPayType brnglkppaytype=manager.createQuery("Select a From BrngLkpPayType a where a.type= '"+inputDetails.get("type")+"'",BrngLkpPayType.class).getSingleResult();
		int payTypeId=brnglkppaytype.getId();
		System.out.println(" user Login Id"+userLoginId);
	Query query = manager
			.createQuery("UPDATE BrngOrder a SET a.brngLkpIsPaid.id =:isPaid,a.payTxId=:payTxId,a.brnglkppaytype.id=:payTypeId where a.id=:orderId and a.brngUsrLogin.id=:userLoginId");
			query.setParameter("isPaid",paidId);
			
			
			query.setParameter("orderId",Integer.parseInt(inputDetails.get("orderId")));
			query.setParameter("userLoginId",userLoginId);
			query.setParameter("payTxId",(inputDetails.get("payTxId")));
			query.setParameter("payTypeId",payTypeId);
			
			query.executeUpdate();
		//transaction.commit();
				
			BrngOrderDelivery brngorderdelivery=manager.createQuery("Select a From BrngOrderDelivery a where a.brngOrder.id= '"+inputDetails.get("orderId")+"'",BrngOrderDelivery.class).getSingleResult();
			BrngUsrLogin brngusrlogin1=manager.createQuery("Select a From BrngUsrLogin a where a.id= '"+brngorderdelivery.getBrngUsrLogin().getId()+"'",BrngUsrLogin.class).getSingleResult();
		
			message=UserMessages.getUserMessagesNew("PD");
			response.put("message", message);
			response.put("response", "1");
			PushHelper pushhelper=new PushHelper();
			pushhelper.pushToUser("notifyservicemanoforders", brngusrlogin1.getPlayerId(), "Payment done by customer. Order No",Integer.toString(brngorder1.getId()));
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

	
	public HashMap<String,String> retryOrder(Hashtable<String,String> inputDetails){
		int retryId=0;
		HashMap<String,String> response =new HashMap<>();
		String message="";
		String paidStatus="";
		try
		{
			System.out.println(" Order Id In Retry "+inputDetails.get("orderId"));
			System.out.println(" cancel status"+getCancelStatus(Integer.parseInt(inputDetails.get("orderId"))));
			if(getCancelStatus(Integer.parseInt(inputDetails.get("orderId")))==1)
			{
				message=UserMessages.getUserMessagesNew("OAC");
				response.put("message", message);
				response.put("response", "10");
				return response;
			}
			List<BrngLkpIsRetry> brngLkpIsretryTypes = manager.createQuery("Select a From BrngLkpIsRetry a",BrngLkpIsRetry.class).
					getResultList();
			List<OrderDeliveryDto> dtoList = new ArrayList();
			OrderDeliveryDto orderDeliveryDto = null;
			for(BrngLkpIsRetry brngLkpIsRetry:brngLkpIsretryTypes){
				if(brngLkpIsRetry.getIsRetry().equalsIgnoreCase("Y"))
				{
					retryId=brngLkpIsRetry.getId();
					//paidStatus=brngLkpIsPaidType.getIsPaid();
					break;
				}
			}
			BrngOrder brngorder1=manager.createQuery("Select a From BrngOrder a where a.id= "+inputDetails.get("orderId"),BrngOrder.class).getSingleResult();
			System.out.println( "  " + brngorder1.getBrngLkpIsPaid().getIsPaid());
			if(brngorder1.getIsAccepted().getIsAccepted().equalsIgnoreCase("Y"))
			{
				message=UserMessages.getUserMessagesNew("OAA");
				response.put("message", message);
				response.put("response", "4");
				return response;
			}

		
		BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+inputDetails.get("email")+"'",BrngUsrReg.class).getSingleResult();
		
		int userId=brngusrreg.getId();
		
BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= '"+userId+"'",BrngUsrLogin.class).getSingleResult();
		
		int userLoginId=brngusrlogin.getId();
		
		Query query = manager
			.createQuery("UPDATE BrngOrder a SET a.brnglkpisretry.id =:isRetry where a.id=:orderId and a.brngUsrLogin.id=:userLoginId");
			query.setParameter("isRetry",retryId);
			
			
			query.setParameter("orderId",Integer.parseInt(inputDetails.get("orderId")));
			query.setParameter("userLoginId",userLoginId);
			
			query.executeUpdate();
			//BrngOrder brngordertemp=manager.createQuery("Select a From BrngOrder a where a.id= "+inputDetails.get("orderId"),BrngOrder.class).getSingleResult();
		//transaction.commit();
			PushHelper pushhelper=new PushHelper();
			pushhelper.pushToAllServiceMan(getListOfServiceMan(brngorder1.getFromLatitude(), brngorder1.getFromLongitude()), Integer.toString(brngorder1.getId()));
			message=UserMessages.getUserMessagesNew("OR");
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
	
	public HashMap<String,String> completeTrip(Hashtable<String,String> inputDetails){
		int paidId=0;
		HashMap<String,String> response =new HashMap<>();
		String message="";
		try
		{
			BrngOrder brngorder1=manager.createQuery("Select a From BrngOrder a where a.id= "+inputDetails.get("orderId"),BrngOrder.class).getSingleResult();

			/*if(brngorder1.getBrngLkpIsPicked().getIsPicked().equalsIgnoreCase("Y"))
			{
				message=UserMessages.getUserMessages(7);
				response.put("message", message);
				response.put("response", "7");
				return response;
			}*/

		BrngLkpOrderDelStatus brnglkporderdelstatus = manager.createQuery("Select a From BrngLkpOrderDelStatus a where a.delStatus='Y'",BrngLkpOrderDelStatus.class).
				getSingleResult();
		int completeId=brnglkporderdelstatus.getId();
		
		BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+inputDetails.get("email")+"'",BrngUsrReg.class).getSingleResult();
		
		int userId=brngusrreg.getId();
		
BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= '"+userId+"'",BrngUsrLogin.class).getSingleResult();
		
		int userLoginId=brngusrlogin.getId();
		
		
		System.out.println(" user Login Id"+userLoginId);
	Query query = manager
			.createQuery("UPDATE BrngOrderDelivery a SET a.brngLkpOrderDelStatus.id =:iscomplete where a.brngOrder.id=:orderId");
			
			query.setParameter("orderId",inputDetails.get("orderId"));
			query.setParameter("iscomplete",completeId);
			
			
			query.executeUpdate();
		//transaction.commit();
				
				
			message=UserMessages.getUserMessagesNew("TC");
			response.put("message", message);
			response.put("response", "1");
			//PushHelper pushhelper=new PushHelper();
			//pushhelper.pushToUser("notifyservicemanoforders", brngorder1.getBrngUsrLogin().getPlayerId(), "Trip Completed",Integer.toString(brngorder1.getId()));
		//	pushhelper.pushToUser("notifybuyer", inputDetails.get("orderId"), "Trip Completed");
			
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
	
	public ArrayList<String> getListOfServiceMan(String fromLat,String fromLng)
	{
		double testRadius=200;
		StringBuffer listOfLogins=new StringBuffer("(");
		ArrayList<String> listOfLoginIds=new ArrayList<>();
		BrngLkpUsrType brnglkpusrtype=manager.createQuery("Select a From BrngLkpUsrType a where a.usrType= 'S'",BrngLkpUsrType.class).getSingleResult();
		int serviceManTypeId=brnglkpusrtype.getId();
		try
		{
			List<BrngUsrLogin> brngusrlogins =manager.createQuery("Select a From BrngUsrLogin a where a.logoutTime is NULL and a.brngLkpUsrType.id="+serviceManTypeId,BrngUsrLogin.class).getResultList();
			
			
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
			
			System.out.println(":    listOfLogins  "+listOfLogins);
			List<BrngServicemanLocationDtls> brngserviceManDetails = manager.createQuery("Select a From BrngServicemanLocationDtls a where a.brngUsrLogin.id in "+listOfLogins,BrngServicemanLocationDtls.class).getResultList();
					
			
			DistanceChecker distancechecker=new DistanceChecker();
			
			for(BrngServicemanLocationDtls BrngServicemanLocationDtl:brngserviceManDetails){
				Double distance=distancechecker.distanceBetween(fromLat,fromLng,BrngServicemanLocationDtl.getLat(),BrngServicemanLocationDtl.getLng());
				if(distance<=testRadius)
				{
					listOfLoginIds.add(BrngServicemanLocationDtl.getBrngUsrLogin().getPlayerId());
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
		System.out.println("list....."+listOfLoginIds);
		return listOfLoginIds;
	}
	
	public HashMap<String,String> cancelOrderByCustomer(Hashtable<String,String> inputDetails){
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
			
			if(brngorder1.getBrngLkpIsPicked().getIsPicked().equalsIgnoreCase("Y"))
			{
				message=UserMessages.getUserMessagesNew("OAPI");
				response.put("message", message);
				response.put("response", "5");
				return response;
			}

		BrngLkpIsCancelled brnglkpiscancelled = manager.createQuery("Select a From BrngLkpIsCancelled a where a.isCancelled='Y'",BrngLkpIsCancelled.class).
				getSingleResult();
		int cancelledId=brnglkpiscancelled.getId();
		
		BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+inputDetails.get("email")+"'",BrngUsrReg.class).getSingleResult();
		
		int userId=brngusrreg.getId();
		
		BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= '"+userId+"'",BrngUsrLogin.class).getSingleResult();
		
		int userLoginId=brngusrlogin.getId();
		
		
		System.out.println(" user Login Id"+userLoginId);
		Query query = manager
				.createQuery("UPDATE BrngOrder a SET a.brnglkpiscancelled.id =:iscancelled where a.id=:orderId and a.brngUsrLogin.id=:userLoginId");
				query.setParameter("iscancelled",cancelledId);
				query.setParameter("orderId",Integer.parseInt(inputDetails.get("orderId")));
				query.setParameter("userLoginId",userLoginId);
			
			query.executeUpdate();
		//transaction.commit();
				
				
			message=UserMessages.getUserMessagesNew("OCS");
			response.put("message", message);
			response.put("response", "1");
			
			if(brngorder1.getIsAccepted().getIsAccepted().equalsIgnoreCase("Y"))
			{
			BrngOrderDelivery brngorderdelivery=manager.createQuery("Select a From BrngOrderDelivery a where a.brngOrder.id= '"+inputDetails.get("orderId")+"'",BrngOrderDelivery.class).getSingleResult();
			BrngUsrLogin brngusrlogin1=manager.createQuery("Select a From BrngUsrLogin a where a.id= '"+brngorderdelivery.getBrngUsrLogin().getId()+"'",BrngUsrLogin.class).getSingleResult();
		
			/*message=UserMessages.getUserMessages(1);
			response.put("message", message);
			response.put("response", "1");*/
			PushHelper pushhelper=new PushHelper();
			pushhelper.pushToUser("notifyservicemanoforders", brngusrlogin1.getPlayerId(), "Order cancelled by customer. Order No",Integer.toString(brngorder1.getId()));
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
	
	
	public HashMap<String,String> rateTrip(Hashtable<String,String> inputDetails){
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
			
		
		BrngUsrReg brngusrreg=manager.createQuery("Select a From BrngUsrReg a where a.emailId= '"+inputDetails.get("email")+"'",BrngUsrReg.class).getSingleResult();
		
		int userId=brngusrreg.getId();
		
		BrngUsrLogin brngusrlogin=manager.createQuery("Select a From BrngUsrLogin a where a.brngUsrReg.id= '"+userId+"'",BrngUsrLogin.class).getSingleResult();
		
		int userLoginId=brngusrlogin.getId();
		
		
		System.out.println(" user Login Id"+userLoginId);
		Query query = manager
				.createQuery("UPDATE BrngOrder a SET a.tripRating =:rating where a.id=:orderId and a.brngUsrLogin.id=:userLoginId");
				query.setParameter("rating",Integer.parseInt(inputDetails.get("rating")));
				query.setParameter("orderId",Integer.parseInt(inputDetails.get("orderId")));
				query.setParameter("userLoginId",userLoginId);
			
			query.executeUpdate();
		//transaction.commit();
				
				
			message=UserMessages.getUserMessagesNew("RS");
			response.put("message", message);
			response.put("response", "1");
			
			if(brngorder1.getIsAccepted().getIsAccepted().equalsIgnoreCase("Y"))
			{
			BrngOrderDelivery brngorderdelivery=manager.createQuery("Select a From BrngOrderDelivery a where a.brngOrder.id= '"+inputDetails.get("orderId")+"'",BrngOrderDelivery.class).getSingleResult();
			BrngUsrLogin brngusrlogin1=manager.createQuery("Select a From BrngUsrLogin a where a.id= '"+brngorderdelivery.getBrngUsrLogin().getId()+"'",BrngUsrLogin.class).getSingleResult();
		
			/*message=UserMessages.getUserMessages(1);
			response.put("message", message);
			response.put("response", "1");*/
			PushHelper pushhelper=new PushHelper();
			pushhelper.pushToUser("notifyservicemanoforders", brngusrlogin1.getPlayerId(), "Order Rated By Customer. Order No",Integer.toString(brngorder1.getId()));
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
	public String getCodes(String type,int code)
	{
		try
		{
			if(type.equalsIgnoreCase("A"))
			{
				BrngLkpIsAccepted brnglkp=manager.createQuery("Select a From BrngLkpIsAccepted a where a.id="+code,BrngLkpIsAccepted.class).
						getSingleResult();
				return brnglkp.getIsAccepted();
			}
			else if(type.equalsIgnoreCase("PI"))
			{
				BrngLkpIsPicked brnglkp=manager.createQuery("Select a From BrngLkpIsPicked a where a.id="+code,BrngLkpIsPicked.class).
						getSingleResult();
				return brnglkp.getIsPicked();
			}
			else if(type.equalsIgnoreCase("PA"))
			{
				BrngLkpIsPaid brnglkp=manager.createQuery("Select a From BrngLkpIsPaid a where a.id="+code,BrngLkpIsPaid.class).
						getSingleResult();
				return brnglkp.getIsPaid();
			}
			else if(type.equalsIgnoreCase("C"))
			{
				BrngLkpIsCancelled brnglkp=manager.createQuery("Select a From BrngLkpIsCancelled a where a.id="+code,BrngLkpIsCancelled.class).
						getSingleResult();
				return brnglkp.getIsCancelled();
			}
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
			return "error";
		}
		return "";
	}
	public String getPlayerIdForBuyer(String wod){
		System.out.println("order Id "+wod);
		int orderId=Integer.parseInt(wod);
		List<OrderDto> listOfOrders=getOrderDetailsById(orderId);
		
		System.out.println("player Id"+listOfOrders.get(0).getBrngOrder().getBrngUsrLogin().getPlayerId());
			return listOfOrders.get(0).getBrngOrder().getBrngUsrLogin().getPlayerId();
	}
	
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
}
