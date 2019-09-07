package com.levent.pcd.controller;

import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import java.util.List;



import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.ModelAndView;


import com.levent.pcd.model.Product;
import com.levent.pcd.model.ShoppingCartMap;
import com.levent.pcd.service.CategoryService;
import com.levent.pcd.service.ProductService;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class ProductUnitTest {

	@Autowired
	ProductController productController;
	@Autowired
	MockMvc mockMvc;

	@MockBean
	ProductService productService;
	@MockBean
	CategoryService categoryService;
	@MockBean
	ShoppingCartMap shoppingCartMap;
	
	List<Product> products;
	List<String> categorys;

	@Test
	public void testListProducts() throws Exception {
		Integer i = 0;
		
		when(categoryService.findAll()).thenReturn(categorys);
		when(productService.findAll(anyInt(),anyInt())).thenReturn(products);
		
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/products"));
		MvcResult mvcResult = resultActions.andReturn();
		ModelAndView mav = mvcResult.getModelAndView();

		assertEquals("products", mav.getViewName());
		assertEquals(0, mav.getModel().get("page"));
		assertEquals(products,mav.getModel().get("productList"));

	}
	
	@Test
	public void testlistProductsByNameSearch() throws Exception {
		
		when(categoryService.findAll()).thenReturn(categorys);
		when(productService.searchProductsByRegex("batt")).thenReturn(products);
		
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/products").param("srch-term", "batt"));
        MvcResult mvcResult = resultActions.andReturn();
        ModelAndView mav = mvcResult.getModelAndView();
        
        assertEquals("products",mav.getViewName());	
        assertEquals(products,mav.getModel().get("productList"));
        assertEquals(categorys,mav.getModel().get("categoryList"));

	}
	
	@Test
	public void testlistProductsByCategory() throws Exception {
		
		String categoryName = "Housewares";
		when(categoryService.findAll()).thenReturn(categorys);
		when(productService.findProductsByCategory(eq(categoryName),anyInt(),anyInt())).thenReturn(products);		
		
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/products-by-category-"+ categoryName));
        MvcResult mvcResult = resultActions.andReturn();
        ModelAndView mav = mvcResult.getModelAndView();
        
        assertEquals("products",mav.getViewName());	
        assertEquals(products,mav.getModel().get("productList"));
        assertEquals(categorys,mav.getModel().get("categoryList"));
	}
	
	@Test
	public void testlistProductById() throws Exception {
		String Id = "1";
		Product p = new Product();
		when(productService.findById(anyString())).thenReturn(p);		
		
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/product-details-"+Id));
        MvcResult mvcResult = resultActions.andReturn();
        ModelAndView mav = mvcResult.getModelAndView();
        
        assertEquals("product-details",mav.getViewName());	
        assertEquals(p,mav.getModel().get("product"));
	}
	
	@Test	
	public void testShoppingCart() throws Exception {		
		
		
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/shopping-cart"));
        MvcResult mvcResult = resultActions.andReturn();
        ModelAndView mav = mvcResult.getModelAndView();
        
        assertEquals("shopping-cart",mav.getViewName());
	}
	
	@Test	
	public void testaddProductView() throws Exception {		
		
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/addproduct"));
        MvcResult mvcResult = resultActions.andReturn();
        ModelAndView mav = mvcResult.getModelAndView();
        
        assertEquals("/addproduct",mav.getViewName());
	}
	
	@Test	
	public void testaddRegisterView() throws Exception {		
		
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/register"));
        MvcResult mvcResult = resultActions.andReturn();
        ModelAndView mav = mvcResult.getModelAndView();
        
        assertEquals("/register",mav.getViewName());
	}
	
	@Test	
	public void testaddLoginForwardView() throws Exception {		
		
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/login-forward"));
        MvcResult mvcResult = resultActions.andReturn();
        ModelAndView mav = mvcResult.getModelAndView();
        
        assertEquals("redirect:/products",mav.getViewName());
	}
	



}
