package com.levent.pcd.model;

<<<<<<< HEAD
import com.levent.pcd.utils.PriceUtilities;

=======
>>>>>>> 65d298712e7d18ab59c9f792e89c96809d9f214e
public class ShoppingCartEntry {
	
	private String imageUrl;
	private String productName;
	private double price;
	private int quantity;
	private double productTotalPrice;
	
	public ShoppingCartEntry() {}
	
	public ShoppingCartEntry(String imageUrl, String productName, double price, int quantity,
			double productTotalPrice) {
		this.imageUrl = imageUrl;
		this.productName = productName;
		this.price = price;
		this.quantity = quantity;
		this.productTotalPrice = productTotalPrice;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public double getProductTotalPrice() {
		return productTotalPrice;
	}

	public void setProductTotalPrice(double productTotalPrice) {
<<<<<<< HEAD
		this.productTotalPrice = PriceUtilities.roundToNDecimalPlaces(productTotalPrice,2);
=======
		this.productTotalPrice = productTotalPrice;
>>>>>>> 65d298712e7d18ab59c9f792e89c96809d9f214e
	}
	
}
