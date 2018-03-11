package com.gvn.brings.services;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gvn.brings.dao.OrderDao;
import com.gvn.brings.dao.RegistrationDao;
import com.gvn.brings.dto.OrderDto;
import com.gvn.brings.dto.RegistrationDto;
import com.gvn.brings.model.BrngOrder;

@Service("orderService")
public class OrderService extends AbstractBaseService{

	@Autowired
	private OrderDao orderDao;
	
	public List<OrderDto> getPayPercent(){
		return orderDao.getpayPercent();
	}
	public List<OrderDto> getIsPaidTypes(){
		return orderDao.getIsPaidTypes();
	}
	
	public HashMap<String,String> bookAnOrder(BrngOrder brngorder){
		return orderDao.bookAnOrder(brngorder);
	}
	
	public List<OrderDto> getOrdersById(String email){
		return orderDao.getOrdersById(email);
	}
	public List<OrderDto> getOrdersByDate(Hashtable<String,String> inputDetails){
		return orderDao.getOrdersByDate(inputDetails);
	}
	
	public List<OrderDto> getOrdersByDateServiceMan(Hashtable<String,String> inputDetails){
		return orderDao.getOrdersByDateServiceMan(inputDetails);
	}
	public List<OrderDto> getCurrentOrdersCustomers(Hashtable<String,String> inputDetails){
		return orderDao.getCurrentOrdersCustomers(inputDetails);
	}
	
	public List<OrderDto> getCurrentOrdersService(Hashtable<String,String> inputDetails){
		return orderDao.getCurrentOrdersService(inputDetails);
	}
	public HashMap<String,String> confirmPayment(Hashtable<String,String> inputDetails){
		return orderDao.confirmPayment(inputDetails);
	}
	
	public HashMap<String,String> retryOrder(Hashtable<String,String> inputDetails){
		return orderDao.retryOrder(inputDetails);
	}
	
	public List<OrderDto> getOrderDetailsById(Hashtable<String,String> inputDetails){
		return orderDao.getOrderDetailsById(Integer.parseInt(inputDetails.get("orderId")));
	}
	
	public List<OrderDto> getPastOrdersCustomers(Hashtable<String,String> inputDetails){
		return orderDao.getPastOrdersCustomers(inputDetails);
	}
	
	public HashMap<String,String> completeTrip(Hashtable<String,String> inputDetails){
		return orderDao.completeTrip(inputDetails);
	}
	
	public List<OrderDto> getPastOrdersServiceMan(Hashtable<String,String> inputDetails){
		return orderDao.getPastOrdersServiceMan(inputDetails);
	}
	
	public HashMap<String,String> cancelOrderByCustomer(Hashtable<String,String> inputDetails){
		return orderDao.cancelOrderByCustomer(inputDetails);
	}
	
	public HashMap<String,String> rateTrip(Hashtable<String,String> inputDetails){
		return orderDao.rateTrip(inputDetails);
	}
	/*public List<OrderDto> getUpcomingOrders(Hashtable<String,String> inputDetails){
		return orderDao.getUpcomingOrders(inputDetails);
	}*/
}
