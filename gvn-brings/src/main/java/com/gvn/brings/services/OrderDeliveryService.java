package com.gvn.brings.services;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gvn.brings.dao.OrderDao;
import com.gvn.brings.dao.OrderDeliveryDao;
import com.gvn.brings.dto.OrderDeliveryDto;
import com.gvn.brings.dto.OrderDto;
import com.gvn.brings.model.BrngOrderDelivery;

@Service("orderDeliveryService")
public class OrderDeliveryService extends AbstractBaseService{

	@Autowired
	private OrderDeliveryDao orderDeliveryDao;
	
	public List<OrderDeliveryDto> getIsAcceptedTypes(){
		return orderDeliveryDao.getIsAcceptedTypes();
	}
	public List<OrderDeliveryDto> getIsPickedTypes(){
		return orderDeliveryDao.getIsPickedTypes();
	}
	
	public List<OrderDeliveryDto> getOrderDelStatus(){
		return orderDeliveryDao.getOrderDelStatus();
	}
	public HashMap<String,String> acceptAnOrder(BrngOrderDelivery brngOrderDelivey){
		return orderDeliveryDao.acceptAnOrder(brngOrderDelivey);
	}
	
	public List<OrderDto> getListOfOpenOrders(Hashtable<String,String> locationData){
		return orderDeliveryDao.getListOfOpenOrders(locationData);
	}
	
	/*public  HashMap<String,String> pickAnOrder(HashMap<String,String> inputdetails){
		return orderDeliveryDao.pickAnOrder(inputdetails);
	}
	*/
	public HashMap<String,String> updateLocation(Hashtable<String,String> locationData){
		return orderDeliveryDao.updateLocation(locationData);
	}
	
	public HashMap<String,String> cancelOrderByServiceMan(HashMap<String,String> inputDetails){
		return orderDeliveryDao.cancelOrderByServiceMan(inputDetails);
	}
	
	public HashMap<String,String> checkServiceManAvailability(HashMap<String,String> inputDetails){
		return orderDeliveryDao.checkServiceManAvailability(inputDetails);
	}
	
	/*public HashMap<String,String> completeTrip(HashMap<String,String> inputDetails){
		return orderDeliveryDao.completeTrip(inputDetails);
	}*/
	public  HashMap<String,String>  orderPicUpload(MultipartFile orderPic,String email,String orderId,String pickupCode) throws ClassNotFoundException, SQLException, FileNotFoundException{
		return orderDeliveryDao.orderPicUpload(orderPic,email,orderId,pickupCode);
	}
	public  HashMap<String,String>  orderPicDropUpload(MultipartFile orderPic,String email,String orderId,String deliveryCode) throws ClassNotFoundException, SQLException, FileNotFoundException{
		return orderDeliveryDao.orderPicDropUpload(orderPic,email,orderId,deliveryCode);
	}
}
