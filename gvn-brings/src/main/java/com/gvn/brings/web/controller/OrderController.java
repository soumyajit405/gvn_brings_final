package com.gvn.brings.web.controller;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.gvn.brings.dto.OrderDto;
import com.gvn.brings.dto.RegistrationDto;
import com.gvn.brings.model.BrngOrder;
import com.gvn.brings.services.OrderService;
import com.gvn.brings.services.RegistrationService;

@RestController
public class OrderController extends AbstractBaseController{

	@Autowired
	private OrderService orderService;
	@RequestMapping(value = REST+"payPercent", method = RequestMethod.GET,headers="Accept=application/json")
	public List<OrderDto> getPayPercent(){
		return orderService.getPayPercent();
	}
	
	@RequestMapping(value = REST+"isPaidTypes", method = RequestMethod.GET,headers="Accept=application/json")
	public List<OrderDto> getIsPaidTypes(){
		return orderService.getIsPaidTypes();
	}
	@RequestMapping(value = REST+"bookAnOrder", method = RequestMethod.POST,headers="Accept=application/json")
	public HashMap<String,String> bookAnOrder(@RequestBody BrngOrder brngorder){
		return orderService.bookAnOrder(brngorder);
	}
	
	@RequestMapping(value = REST+"getOrdersById", method = RequestMethod.POST,headers="Accept=application/json")
	public List<OrderDto> getOrdersById(@RequestBody Hashtable<String,String> inputDetails){
		return orderService.getOrdersById(inputDetails.get("email"));
	}
	
	@RequestMapping(value = REST+"getOrdersByDate", method = RequestMethod.POST,headers="Accept=application/json")
	public List<OrderDto> getOrdersByDate(@RequestBody Hashtable<String,String> inputDetails){
		return orderService.getOrdersByDate(inputDetails);
	}
	
	@RequestMapping(value = REST+"getOrdersByDateServiceMan", method = RequestMethod.POST,headers="Accept=application/json")
	public List<OrderDto> getOrdersByDateServiceMan(@RequestBody Hashtable<String,String> inputDetails){
		return orderService.getOrdersByDateServiceMan(inputDetails);
	}
	
	@RequestMapping(value = REST+"getCurrentOrdersCustomers", method = RequestMethod.POST,headers="Accept=application/json")
	public List<OrderDto> getCurrentOrdersCustomers(@RequestBody Hashtable<String,String> inputDetails){
		return orderService.getCurrentOrdersCustomers(inputDetails);
	}
	
	@RequestMapping(value = REST+"getCurrentOrdersService", method = RequestMethod.POST,headers="Accept=application/json")
	public List<OrderDto> getCurrentOrdersService(@RequestBody Hashtable<String,String> inputDetails){
		return orderService.getCurrentOrdersService(inputDetails);
	}


	@RequestMapping(value = REST+"confirmPayment", method = RequestMethod.POST,headers="Accept=application/json")
	public HashMap<String,String> confirmPayment(@RequestBody Hashtable<String,String> inputDetails){
		return orderService.confirmPayment(inputDetails);
	}
	
	@RequestMapping(value = REST+"retryOrder", method = RequestMethod.POST,headers="Accept=application/json")
	public HashMap<String,String> retryOrder(@RequestBody Hashtable<String,String> inputDetails){
		return orderService.retryOrder(inputDetails);
	}
	
	@RequestMapping(value = REST+"getOrderDetailsById", method = RequestMethod.POST,headers="Accept=application/json")
	public List<OrderDto> getOrderDetailsById(@RequestBody Hashtable<String,String> inputDetails){
		return orderService.getOrderDetailsById(inputDetails);
	}
	
	@RequestMapping(value = REST+"getPastOrdersCustomers", method = RequestMethod.POST,headers="Accept=application/json")
	public List<OrderDto> getPastOrdersCustomers(@RequestBody Hashtable<String,String> inputDetails){
		return orderService.getPastOrdersCustomers(inputDetails);
	}
	
	@RequestMapping(value = REST+"getPastOrdersServiceMan", method = RequestMethod.POST,headers="Accept=application/json")
	public List<OrderDto> getPastOrdersServiceMan(@RequestBody Hashtable<String,String> inputDetails){
		return orderService.getPastOrdersServiceMan(inputDetails);
	}
	
	@RequestMapping(value = REST+"cancelOrderByCustomer", method = RequestMethod.POST,headers="Accept=application/json")
	public HashMap<String,String>   cancelOrderByCustomer(@RequestBody Hashtable<String,String> inputDetails){
		return orderService.cancelOrderByCustomer(inputDetails);
	}
	
	@RequestMapping(value = REST+"rateTrip", method = RequestMethod.POST,headers="Accept=application/json")
	public HashMap<String,String>   rateTrip(@RequestBody Hashtable<String,String> inputDetails){
		return orderService.rateTrip(inputDetails);
	}
	
	/*@RequestMapping(value = REST+"getUpcomingOrders", method = RequestMethod.POST,headers="Accept=application/json")
	public List<OrderDto> getUpcomingOrders(@RequestBody Hashtable<String,String> inputDetails){
		//return orderService.getUpcomingOrders(inputDetails);
	}*/

}
