package com.levent.pcd.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.levent.pcd.mail.Email;
import com.levent.pcd.mail.EmailComponent;
import com.levent.pcd.model.Order;
import com.levent.pcd.model.ShoppingCartEntry;
import com.levent.pcd.model.ShoppingCartMap;
import com.levent.pcd.model.UserEntry;
import com.levent.pcd.model.UserInfo;
import com.levent.pcd.model.Order.OrderStatus;
import com.levent.pcd.repository.OrderRepository;
import com.levent.pcd.service.PayPalClient;

@Controller
public class PaymentController {

	@Autowired
	EmailComponent emailComponent;
	@Autowired PayPalClient payPalClient;
	
	@Autowired ShoppingCartMap shoppingCartMap;


	@Autowired UserEntry entry;
	@Autowired OrderRepository rep;
	@GetMapping("/payment_start")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
	public String processPaymentStart( @RequestParam("paymentId") String paymentId,
			@RequestParam("PayerID") String payerId,  @RequestParam("sum") String sum, @RequestParam String orderId) throws Exception {
		
		return "redirect:/complete_payment?paymentId="+ paymentId+"&PayerID="+payerId+"&sum="+sum+"&orderId="+ orderId;
	}
	
	@GetMapping("/send_mail")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
	public String sendPaymentSuccessMail( @RequestParam String orderId) throws Exception {
		
		Optional<Order> orderOptional=rep.findById(orderId);
		if(!orderOptional.isPresent()) {
			throw new RuntimeException("Error processing order with orderId: "+ orderId);
		}
		Order order= orderOptional.get();
		UserInfo user= entry.getUser();
		Map<String, String> templateTokens = new HashMap<String, String>();
		templateTokens.put("LOGOPATH","Shopper's Club, Order Status");
		templateTokens.put("LABEL_HI", "Hello");
		templateTokens.put("EMAIL_CUSTOMER_FIRSTNAME",user.getNickname());
		templateTokens.put("EMAIL_ORDER_NUMBER","Your order number is:"+ orderId);
		templateTokens.put("EMAIL_ORDER_DATE", LocalDate.now().toString());
		templateTokens.put("EMAIL_ORDER_THANKS", "Thanks for taking our service!");
		templateTokens.put("EMAIL_ORDER_DETAILS_TITLE", "Your order details:");
		StringBuilder details= new StringBuilder();
		if(order.getProductsPlaced()!= null && order.getProductsPlaced().size()>0) {
			details.append("Following items woruld be shipped at given address:\n");
		for(ShoppingCartEntry entry: order.getProductsPlaced()) {
			
			details.append("Product Name: "+entry.getProductName()+"\n");
			details.append("Number of items : "+entry.getQuantity()+"\n");
			details.append("Product Price: "+entry.getProductTotalPrice()+"\n");
			
		}
		}
		
		
		if(order.getProductsCancelled()!= null && order.getProductsCancelled().size()>0) {
			details.append("Following items were cancelled due to non-availability \n");
			for(ShoppingCartEntry entry: order.getProductsCancelled()) {
				
				details.append("Product Name: "+entry.getProductName()+"\n");
				details.append("Number of items : "+entry.getQuantity()+"\n");
				details.append("Product Price: "+entry.getProductTotalPrice()+"\n");
				
			}
		}
		
		templateTokens.put("ORDER_PRODUCTS_DETAILS",details.toString());
		
		templateTokens.put("ADDRESS_BILLING_TITLE", "Address details:");
		templateTokens.put("ADDRESS_BILLING", user.getAddresses().toString());
		
		templateTokens.put("ORDER_STATUS", order.getStatus().toString());
		templateTokens.put("EMAIL_DISCLAIMER", "This email is sent on behalf of ShoppersClub");
		templateTokens.put("EMAIL_FOOTER_COPYRIGHT", "For any discretion, call our customer care.");
		
		Email email = new Email();
		email.setFrom("Shopper's club");
		email.setFromEmail("jahanvi.bansal@gmail.com");
		email.setSubject("Order Status for order number: "+ order.getOrderId());
		email.setTo("payal@rjtcompuquest.com");
		
		email.setTemplateName("email_template_checkout.ftl");
		email.setTemplateTokens(templateTokens);

		emailComponent.send(email);
		System.out.println("sent!");
		return "redirect:/shopping-cart";
	}


	
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
	@GetMapping( "/make_payment")
	public String makePayment( HttpServletRequest req) {
		 return payPalClient.createPayment( req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath(), entry.getUser().getUsername());
	}
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
	@GetMapping("/complete_payment")
	public String completePayment( @RequestParam("paymentId") String paymentId,
			@RequestParam("PayerID") String payerId , @RequestParam("sum") String sum,@RequestParam String orderId, HttpSession session) {
		String result= payPalClient.completePayment(paymentId, payerId, sum, orderId, entry.getUser().getUsername());
		session.setAttribute("shoppingCartMap", shoppingCartMap);
		return result;
	}
}
