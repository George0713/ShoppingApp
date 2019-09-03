package com.levent.pcd.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.levent.pcd.model.UserAuth;
import com.levent.pcd.model.UserEntry;
import com.levent.pcd.model.UserInfo;
import com.levent.pcd.model.UserRole;
import com.levent.pcd.repository.UserAuthRepository;
import com.levent.pcd.repository.UserInfoRepository;

@Controller
public class UserRegisterCtrl {
	@Autowired UserInfoRepository userInfoRep;
	@Autowired UserAuthRepository userAuthRep;
	@Autowired UserEntry userEntry;
	
//	@GetMapping("/addUser")
//	@ResponseBody
//	public String testPath(@RequestParam("username") String username) {
//		return "Path Correctly";
//	}
	
	@PostMapping("/addAdmin")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ModelAndView registAdmin(@ModelAttribute UserInfo userInfo,@ModelAttribute UserAuth userAuth, @RequestParam String username) {
		ModelAndView model = new ModelAndView();
		model.setViewName("/register");
		
		List<UserRole> roles = new ArrayList<UserRole>();
		roles.add(UserRole.ROLE_USER);
		roles.add(UserRole.ROLE_ADMIN);
		userAuth.setUserRoles(roles);
		
		userInfo.setUsername(username);
		userAuth.setUsername(username);
		if (userInfoRep.findByUsername(username)!=null) {			
			model.addObject("msg","Username has been used, try another one.");
			return model;
		}
		userInfoRep.save(userInfo);
		userAuthRep.save(userAuth);
		userEntry.setUser(userInfo);
		userEntry.setLogin(true);		
		model.addObject("msg", "Success!");		
		return model;
	}	
	
	@PostMapping("/addUser")
	public ModelAndView registUser(@ModelAttribute UserInfo userInfo,@ModelAttribute UserAuth userAuth, @RequestParam String username, 
			HttpSession session, HttpServletRequest request) throws ServletException {
		ModelAndView model = new ModelAndView();
		model.setViewName("redirect:/products");
		
		List<UserRole> roles = new ArrayList<UserRole>();
		roles.add(UserRole.ROLE_USER);
		userAuth.setUserRoles(roles);
		
		userInfo.setUsername(username);
		userAuth.setUsername(username);
		if (userInfoRep.findByUsername(username)!=null) {
			model.setViewName("/register");
			model.addObject("msg","Email has been used, try another one.");
			return model;
		}
		userInfoRep.save(userInfo);
		userAuthRep.save(userAuth);
		userEntry.setUser(userInfo);
		userEntry.isLogin=true;	
		
		System.out.println(userEntry);        
        request.login(userAuth.getUsername(),userAuth.getPassword());
        /*session.setAttribute("userEntry", userEntry);*/
        
        
		model.addObject("msg", "Success!");		
		return model;
	}
	@InitBinder
	public void fileBinder(WebDataBinder binder) {
		binder.setDisallowedFields("username");
	}
	
//	private boolean autoLogin( String username, String password, HttpServletRequest request) {
//	       
//        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
// 
//        Authentication authentication = authManager.authenticate(token);
// 
//        SecurityContextHolder.getContext().setAuthentication(authentication );
// 
//        //this step is important, otherwise the new login is not in session which is required by Spring Security
//        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
//        
//        
//        return true;
//    }
//	

}
