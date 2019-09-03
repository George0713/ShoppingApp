package com.levent.pcd.model;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {
	@Id
	private String orderId;
	 private String username;
	 LocalDateTime date;
	 private OrderStatus status;
	 List<ShoppingCartEntry> productsPlaced;
	 List<ShoppingCartEntry> productsCancelled;
	 double amountDeducted;
	 public enum OrderStatus{
			ORDER_INITIATED, PAYMENT_INITIATED, PAYMENT_SUCCESS, ORDER_ON_HOLD,ORDER_CONFIRMED, PAYMENT_FAILED
	 }
	 
	 public enum ProductStatus{
		 	NOT_AVAILABLE, AVAILABLE
	 }
}

