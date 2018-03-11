package com.gvn.brings.web.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gvn.brings.dto.OrderDeliveryDto;
import com.gvn.brings.dto.OrderDto;
import com.gvn.brings.model.BrngOrderDelivery;
import com.gvn.brings.services.OrderDeliveryService;
import com.gvn.brings.services.OrderService;

@RestController
public class OrderDeliveryController extends AbstractBaseController{

	@Autowired
	private OrderDeliveryService orderDelieveryService;
	
	@RequestMapping(value = REST+"isAcceptedTypes", method = RequestMethod.GET,headers="Accept=application/json")
	public List<OrderDeliveryDto> getIsAcceptedTypes(){
		return orderDelieveryService.getIsAcceptedTypes();
	}
	
	@RequestMapping(value = REST+"isPickedTypes", method = RequestMethod.GET,headers="Accept=application/json")
	public List<OrderDeliveryDto> getIsPickedTypes(){
		return orderDelieveryService.getIsPickedTypes();
	}
	
	@RequestMapping(value = REST+"orderDelStatus", method = RequestMethod.GET,headers="Accept=application/json")
	public List<OrderDeliveryDto> getOrderDelStatus(){
		return orderDelieveryService.getOrderDelStatus();
	}
	
	@RequestMapping(value = REST+"acceptAnOrder", method = RequestMethod.POST,headers="Accept=application/json")
	public HashMap<String,String> acceptAnOrder(@RequestBody BrngOrderDelivery brngOrderDelivey){
		return orderDelieveryService.acceptAnOrder(brngOrderDelivey);
	}
	
	@RequestMapping(value = REST+"getListOfOpenOrders", method = RequestMethod.POST,headers="Accept=application/json")
	public List<OrderDto> getListOfOpenOrders(@RequestBody Hashtable<String,String> locationData){
		return orderDelieveryService.getListOfOpenOrders(locationData);
	}
	
	/*@RequestMapping(value = REST+"pickAnOrder", method = RequestMethod.POST,headers="Accept=application/json")
	public  HashMap<String,String> pickAnOrder(@RequestBody HashMap<String,String> inputdetails){
		return orderDelieveryService.pickAnOrder(inputdetails);
	}*/
	
	@RequestMapping(value = REST+"updateLocation", method = RequestMethod.POST,headers="Accept=application/json")
	public HashMap<String,String> updateLocation(@RequestBody Hashtable<String,String> locationData){
		return orderDelieveryService.updateLocation(locationData);
	}
	
	@RequestMapping(value = REST+"cancelOrderByServiceMan", method = RequestMethod.POST,headers="Accept=application/json")
	public HashMap<String,String>   cancelOrderByServiceMan(@RequestBody HashMap<String,String> inputDetails){
		return orderDelieveryService.cancelOrderByServiceMan(inputDetails);
	}
	
	@RequestMapping(value = REST+"checkServiceManAvailability", method = RequestMethod.POST,headers="Accept=application/json")
	public HashMap<String,String>   checkServiceManAvailability(@RequestBody HashMap<String,String> inputDetails){
		return orderDelieveryService.checkServiceManAvailability(inputDetails);
	}
	
	/*@RequestMapping(value = REST+"completeTrip", method = RequestMethod.POST,headers="Accept=application/json")
	public HashMap<String,String>   completeTrip(@RequestBody HashMap<String,String> inputDetails){
		return orderDelieveryService.completeTrip(inputDetails);
	}*/
	
	@RequestMapping(value = REST+"orderPicUpload", headers=("content-type=multipart/*"), method = RequestMethod.POST)
	public  HashMap<String,String>   orderPicUpload(@RequestParam("orderPic") MultipartFile orderPic,@RequestParam("email") String email,@RequestParam("orderId") String orderId,@RequestParam("pickupCode") String pickupCode) throws ClassNotFoundException, SQLException,IOException{
		// String saveDirectory = "D:\\Softwares\\apache-tomcat-8.5.9-windows-x64\\apache-tomcat-8.5.9\\webapps\\WebSockets\\32\\aadhar";
		//System.out.println("description: " + request.getParameter("email"));
		
		return orderDelieveryService.orderPicUpload(orderPic,email,orderId,pickupCode); 
	}
	@RequestMapping(value = REST+"completeTrip", headers=("content-type=multipart/*"), method = RequestMethod.POST)
	public  HashMap<String,String>   orderPicDropUpload(@RequestParam("orderPic") MultipartFile orderPic,@RequestParam("email") String email,@RequestParam("orderId") String orderId,@RequestParam("deliveryCode") String deliveryCode) throws ClassNotFoundException, SQLException,IOException{
		// String saveDirectory = "D:\\Softwares\\apache-tomcat-8.5.9-windows-x64\\apache-tomcat-8.5.9\\webapps\\WebSockets\\32\\aadhar";
		//System.out.println("description: " + request.getParameter("email"));
		
		return orderDelieveryService.orderPicDropUpload(orderPic,email,orderId,deliveryCode); 
	}
	
	
	
}
