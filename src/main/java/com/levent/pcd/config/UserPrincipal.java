package com.levent.pcd.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.levent.pcd.model.UserAuth;
import com.levent.pcd.model.UserInfo;
import com.levent.pcd.model.UserRole;

public class UserPrincipal implements OAuth2User, UserDetails {
	private static final long serialVersionUID = -5243630103550970816L;
	private String id;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    public UserPrincipal(String id, String email, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserPrincipal create(UserInfo userInfo, UserAuth userAuth) {
        List<GrantedAuthority> authorities=new ArrayList<GrantedAuthority>();


			for (UserRole role : userAuth.getUserRoles()) {
				authorities.add(new SimpleGrantedAuthority(role.name()));
			}
		

        return new UserPrincipal(
        		userAuth.getUsername(),
        		userInfo.getEmail(),
        		userAuth.getPassword(),
                authorities
        );
    }

    public static UserPrincipal create(UserInfo userInfo, UserAuth userAuth, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(userInfo,userAuth);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }
}