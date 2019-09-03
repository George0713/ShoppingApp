package com.levent.pcd.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.FileEditor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import com.levent.pcd.exception.InventoryMismatchExcpetion;
import com.levent.pcd.model.Order;
import com.levent.pcd.model.Order.OrderStatus;
import com.levent.pcd.model.Product;
import com.levent.pcd.model.ShoppingCartEntry;
import com.levent.pcd.model.ShoppingCartEntry.ItemStatus;
import com.levent.pcd.model.ShoppingCartMap;
import com.levent.pcd.repository.OrderRepository;
import com.levent.pcd.repository.ProductRepository;
import com.levent.pcd.utils.PriceStrategyTax;


@Service
public class ProductServiceImpl implements ProductService {
	
	@Autowired ShoppingCartMap cart;
	@Autowired
	private ProductRepository productRepository;
	@Autowired OrderRepository rep;
	@Autowired PriceStrategyTax priceStrategy;
	
	@Override
	public List<Product> findAll(int page, int limit) {
		
		return productRepository.findByInStoreGreaterThan(0, PageRequest.of(page, limit));
	}

	@Override
	public Product findBySku(String sku) {
		return productRepository.findBySku(sku);
	}
	
	@Override
	public List<Product> findProductsByCategory(String categoryName, int page, int size) {
		return productRepository.findProductsByCategoryProductNameAndInStoreGreaterThan(categoryName, 0,PageRequest.of(page, size));
	}

	@Override
	public List<Product> findProductsByName(String searchString) {
		return productRepository.findProductsByProductNameRegex(searchString);
	}
	
	@Override
	public List<Product> searchProductsByRegex(String searchString){
		List<Product> r = productRepository.searchProduct(searchString);
		return r;
	}
	
	@Override
	public void updateProductsRemained(Product product, int inStore) {
		product.setInStore(inStore);
		productRepository.save(product);
	}
	

	@Override
	public void addProduct(Product product) {
		productRepository.save(product);
		
	}
	
	@InitBinder
	protected void imageFileBinder(WebDataBinder binder) {
		binder.registerCustomEditor(java.io.File.class, new FileEditor());
	}

	@Override
	public Product findById(String id) {
		return productRepository.findById(id).orElse(new Product());
	}

	@Override
	public Order updateProductsRemained(String username) {
	double sum=0;
	List<ShoppingCartEntry> orderPlaced= new ArrayList<>();
	List<ShoppingCartEntry> orderCancelled= new ArrayList<>();
		//If few items in cart gets out of stock, just place order with products in store. Cart will reflect items to be out_of_stock for which order not placed.
		Iterator<ShoppingCartEntry> it= cart.getCartItems().values().iterator();
		while(it.hasNext()){
			ShoppingCartEntry entry= it.next();
			Optional<Product> p=productRepository.findById(entry.getId());
			if(!p.isPresent()) {
				throw new InventoryMismatchExcpetion("Invalid cart details!. Product is invalid"+ entry);
			}else {
				Product prod=p.get();
				if(productRepository.deductQuantity(prod.getId(), entry.getQuantity())) {
					sum+= priceStrategy.gePriceAfterTax(prod.getPrice());
					orderPlaced.add(entry);
					it.remove();
					
				}else {
					orderCancelled.add(entry);
					entry.setStatus(ItemStatus.OUT_OF_STOCK);
				}
				
			}
		}
		
		
		 Order o=  rep.insert(Order.builder().date(LocalDateTime.now()).productsPlaced(orderPlaced).productsCancelled(orderCancelled).amountDeducted(sum).status(OrderStatus.ORDER_INITIATED).username(username).build());
		   System.out.println(o);
		return o;
	}

	
	
	
	
}
