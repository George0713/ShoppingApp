package com.levent.pcd.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.levent.pcd.model.Order;
import com.levent.pcd.model.Order.OrderStatus;
import com.levent.pcd.repository.OrderRepository;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

@Component
	public class PayPalClient {

	@Autowired OrderRepository rep;
	    String clientId = "ASTze2vhR30SxI6vOYNPVLfy_F9KmqxIagySXObBtmbAyXJYMxvwBWNQHv42uYRqLrHdLxm-IHV95C5Y";
	    String clientSecret = "EHN-bcn02PKbwlEkBWtiMXTPMF0F39pLl_N-qy2rI4LzBcepeJlFkCL1bKOYwWJLT69tgYz-2OqLpfr1";

	 
	 
	    
	    @Autowired ProductService pService;
//	    PayPalClient(){}

	    public String createPayment(String path, String username){
	    	Order order= pService.updateProductsRemained(username);
	        Map<String, Object> response = new HashMap<String, Object>();
	        Amount amount = new Amount();
	        amount.setCurrency("INR");
	        amount.setTotal(order.getAmountDeducted()+"");
	        Transaction transaction = new Transaction();
	        transaction.setAmount(amount);
	        List<Transaction> transactions = new ArrayList<Transaction>();
	        transactions.add(transaction);

	        Payer payer = new Payer();
	        payer.setPaymentMethod("paypal");

	        Payment payment = new Payment();
	        payment.setIntent("sale");
	        payment.setPayer(payer);
	        payment.setTransactions(transactions);
	        Order o=rep.save(Order.builder().orderId(order.getOrderId()).date(LocalDateTime.now()).status(OrderStatus.PAYMENT_INITIATED).amountDeducted(order.getAmountDeducted()).productsCancelled(order.getProductsCancelled()).productsPlaced(order.getProductsPlaced()).username(username).build());
	        System.out.println(o);
	        RedirectUrls redirectUrls = new RedirectUrls();
	        redirectUrls.setCancelUrl(path+"/send_mail?orderId="+ o.getOrderId());
	        redirectUrls.setReturnUrl(path+"/payment_start?sum="+o.getAmountDeducted()+"&orderId="+order.getOrderId());
	        payment.setRedirectUrls(redirectUrls);
	        Payment createdPayment;
	        String redirectUrl = "";
	        try {
	          
	            APIContext context = new APIContext(clientId, clientSecret, "sandbox");
	            createdPayment = payment.create(context);
	            if(createdPayment!=null){
	                List<Links> links = createdPayment.getLinks();
	                for (Links link:links) {
	                    if(link.getRel().equals("approval_url")){
	                        redirectUrl = link.getHref();
	                        break;
	                    }
	                }
	                System.out.println(createdPayment);
	                response.put("status", "success");
	                response.put("redirect_url", redirectUrl);
	                
	            }
	        } catch (PayPalRESTException e) {
	        	 Order o1=rep.save(Order.builder().orderId(order.getOrderId()).date(LocalDateTime.now()).status(OrderStatus.PAYMENT_FAILED).amountDeducted(order.getAmountDeducted()).productsCancelled(order.getProductsCancelled()).productsPlaced(order.getProductsPlaced()).username(username).build());
	            System.out.println(o1);
	        	return "redirect:/send_mail?orderId="+ o.getOrderId();
	        }
	             
	        return "redirect:"+redirectUrl;
	    }


	    public String completePayment(String paymentId, String payerId, String sum, String orderId, String username){
	    	Optional<Order> optionalOrder= rep.findById(orderId);
	    	if(!optionalOrder.isPresent()) {
	    		throw new RuntimeException("Error processing order with orderId:"+ orderId);
	    	}
	    	Order order= optionalOrder.get();
	        Map<String, Object> response = new HashMap<>();
	        Payment payment = new Payment();
	        payment.setId(paymentId);
	        PaymentExecution paymentExecution = new PaymentExecution();
	        paymentExecution.setPayerId(payerId);
	        try {
	            APIContext context = new APIContext(clientId, clientSecret, "sandbox");
	            Payment createdPayment = payment.execute(context, paymentExecution);
	            if(createdPayment!=null){
	                response.put("status", "success");
	                response.put("payment", createdPayment);
	            }
	        } catch (PayPalRESTException e) {
	        	 Order o=rep.save(Order.builder().orderId(order.getOrderId()).date(LocalDateTime.now()).status(OrderStatus.PAYMENT_FAILED).amountDeducted(order.getAmountDeducted()).productsCancelled(order.getProductsCancelled()).productsPlaced(order.getProductsPlaced()).username(username).build());
	 	        System.out.println(o);
	            return "redirect:/send_mail?orderId="+ orderId;
	        }
	        Order o=rep.save(Order.builder().orderId(order.getOrderId()).date(LocalDateTime.now()).status(OrderStatus.PAYMENT_SUCCESS).amountDeducted(order.getAmountDeducted()).productsCancelled(order.getProductsCancelled()).productsPlaced(order.getProductsPlaced()).username(username).build());
	        System.out.println(o);
	        
	        return "redirect:/send_mail?orderId="+ orderId;
	    }



	
}
