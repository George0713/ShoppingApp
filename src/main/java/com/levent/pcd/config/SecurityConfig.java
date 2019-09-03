package com.levent.pcd.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.levent.pcd.model.AuthProvider;
import com.levent.pcd.model.OAuth2UserInfo;
import com.levent.pcd.model.OAuth2UserInfoFactory;
import com.levent.pcd.model.UserAuth;
import com.levent.pcd.model.UserEntry;
import com.levent.pcd.model.UserInfo;
import com.levent.pcd.model.UserRole;
import com.levent.pcd.repository.UserAuthRepository;
import com.levent.pcd.repository.UserInfoRepository;

@Configuration
@EnableWebSecurity
//@EnableOAuth2Sso
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired UserEntry entry;
	@Autowired UserDetailServiceImpl service;
	@Autowired CustomAuthenticationSuccessHandler successHandler;
	@Autowired CustomAuthenticationFailureHandler failureHandler;
    
	@Autowired CustomOAuth2UserService customOAuth2UserService;
    @Autowired private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    @Autowired private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

//	@Bean
	public BCryptPasswordEncoder getEncoder() {
		return new BCryptPasswordEncoder(11);
	}
	@Bean
	public AuthorizationRequestRepository<OAuth2AuthorizationRequest> 
	  authorizationRequestRepository() {
	  
	    return new HttpSessionOAuth2AuthorizationRequestRepository();
	}

	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(service).passwordEncoder(NoOpPasswordEncoder.getInstance());
	}
	public void configure(HttpSecurity http) throws Exception {
		http.formLogin().successHandler(successHandler).failureHandler(failureHandler).
		and()
		.logout().logoutUrl("/logout").logoutSuccessUrl("/").
		and()
			.oauth2Login()
			.successHandler(oAuth2AuthenticationSuccessHandler)
			.failureHandler(oAuth2AuthenticationFailureHandler)
			.authorizationEndpoint()
			.authorizationRequestRepository(authorizationRequestRepository())
	        .baseUri("/login")
	        .and()
			.redirectionEndpoint()
			.baseUri("/login/oauth2/code/*")
			.and()
			.userInfoEndpoint()
			.userService(customOAuth2UserService)
			;
			
//		http.csrf().disable();
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		// add it
		web.httpFirewall(allowUrlEncodedSlashHttpFirewall());		
	}

	@Bean
	public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
		StrictHttpFirewall firewall = new StrictHttpFirewall();
		firewall.setAllowUrlEncodedSlash(true);
		   firewall.setAllowSemicolon(true);
		return firewall;
	}

}

@Component
class UserDetailServiceImpl implements UserDetailsService {

	@Autowired
	UserAuthRepository rep;
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserAuth u=rep.findByUsername(username);
		if (u==null ||u.getPassword() == null || u.getUserRoles().isEmpty()) {
			throw new UsernameNotFoundException("Invalid username");
		} else {
//			System.out.println(u);
			UserBuilder builder = org.springframework.security.core.userdetails.User.builder();
			UserDetails userDetails = builder.username(username).password(u.getPassword()).authorities(u.getUserRoles().stream()
					.map(x -> new SimpleGrantedAuthority(x.name())).collect(Collectors.toList())).build();
			return userDetails;
		}
	}
}

@Component
class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	 
    public static final String RETURN_TYPE = "html"; 
	@Autowired	UserEntry userEntry;
	@Autowired  UserInfoRepository rep;
	
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        userEntry.setUser(rep.findByUsername(authentication.getName()));
        userEntry.isLogin = true;
//        System.out.println("Success");
//        System.out.println(userEntry.getUser());
//        System.out.println(userEntry.getUser().getNickname());
//        System.out.println(userEntry);
        request.getSession(false).setAttribute("userEntry", userEntry);   
        super.setDefaultTargetUrl("/");
        super.onAuthenticationSuccess(request, response, authentication);
  
    }
}

@Component
class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
	 
    public static final String RETURN_TYPE = "html"; 
	@Autowired	UserEntry userEntry;
	@Autowired  UserInfoRepository rep;
	
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
//        System.out.println("Fail");
//        System.out.println(userEntry);
        super.setDefaultFailureUrl("/login-forward");
        super.onAuthenticationFailure(request, response, exception);
  
    }
}

@Component
class CustomOAuth2UserService extends DefaultOAuth2UserService{
	
	@Autowired UserInfoRepository userInfoRepository;
	@Autowired UserAuthRepository userAuthRepository;
	
    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
//    	RestTemplate restTemplate = new RestTemplate();
//    	RestOperations restOperations =restTemplate ;
//    	ParameterizedTypeReference<Map<String, Object>> PARAMETERIZED_RESPONSE_TYPE =new ParameterizedTypeReference<Map<String, Object>>() {};
//    	Converter<OAuth2UserRequest, RequestEntity<?>> requestEntityConverter = new OAuth2UserRequestEntityConverter();
//    	System.out.println(oAuth2UserRequest.getAccessToken());
//    	System.out.println(oAuth2UserRequest.getClientRegistration().getProviderDetails()
//				.getUserInfoEndpoint());
//    	RequestEntity<?> request = requestEntityConverter.convert(oAuth2UserRequest);
//    	System.out.println(restOperations.exchange(request, PARAMETERIZED_RESPONSE_TYPE));
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
        	ex.printStackTrace();
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) throws Exception {
    	OAuth2UserInfo oAuth2UserInfo =OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        if(StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new Exception("Email not found from OAuth2 provider");
        }
        
        System.out.println(oAuth2UserInfo.getEmail());

        Optional<UserInfo> userOptional = Optional.ofNullable( userInfoRepository.findByUsername(oAuth2UserInfo.getEmail()));
        UserAuth userAuth = userAuthRepository.findByUsername(oAuth2UserInfo.getEmail());
        if (userAuth==null) {
        	userAuth = new UserAuth();
        	List<UserRole> roles = new ArrayList<UserRole>();
        	roles.add(UserRole.ROLE_USER);
        	userAuth.setUsername(oAuth2UserInfo.getEmail());
        	userAuth.setUserRoles(roles);        	
        }
        
        UserInfo userInfo;
        if(userOptional.isPresent()) {
        	userInfo = userOptional.get();
        	userInfo.setUsername(oAuth2UserInfo.getEmail());
            if(!userInfo.getProvider().equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                throw new Exception("Looks like you're signed up with " +
                		userInfo.getProvider() + " account. Please use your " + userInfo.getProvider() +
                        " account to login.");
            }
            userInfo = updateExistingUser(userInfo, oAuth2UserInfo);
        } else {
        	userInfo = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        	userAuth = userAuthRepository.save(userAuth);
        }

        return UserPrincipal.create(userInfo,userAuth, oAuth2User.getAttributes());
    }

    private UserInfo registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        UserInfo userInfo = new UserInfo();

        userInfo.setProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
        userInfo.setProviderId(oAuth2UserInfo.getId());
        userInfo.setNickname(oAuth2UserInfo.getName());
        userInfo.setEmail(oAuth2UserInfo.getEmail());
        userInfo.setUsername(oAuth2UserInfo.getEmail());
        userInfo.setImageUrl(oAuth2UserInfo.getImageUrl());
        return userInfoRepository.save(userInfo);
    }

    private UserInfo updateExistingUser(UserInfo existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.setNickname(oAuth2UserInfo.getName());
        existingUser.setImageUrl(oAuth2UserInfo.getImageUrl());
        return userInfoRepository.save(existingUser);
    }

}

@Component
class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {


	@Autowired	UserEntry userEntry;
	@Autowired  UserInfoRepository rep;
	@Autowired  OAuth2Config appProperties;
	
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
       
        userEntry.setUser(rep.findByUsername(authentication.getName()));
        userEntry.isLogin = true;
//        System.out.println("OAUTH Success");
//        System.out.println(authentication.getName());
//        System.out.println(userEntry.getUser());
//        System.out.println(userEntry.getUser().getNickname());
//        System.out.println(userEntry);
        request.getSession(false).setAttribute("userEntry", userEntry);   
        super.setDefaultTargetUrl("/");
        super.onAuthenticationSuccess(request, response, authentication);
  
    }

}

@Component
class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
	 
    public static final String RETURN_TYPE = "html"; 
	@Autowired	UserEntry userEntry;
	@Autowired  UserInfoRepository rep;
	@Autowired  OAuth2Config appProperties;
	
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        System.out.println("OAUTH Fail");
        System.out.println(userEntry);
        super.setDefaultFailureUrl("/login-forward");
        super.onAuthenticationFailure(request, response, exception);
  
    }
}
