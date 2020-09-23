package com.ninos.domain;

import java.util.Collection;
import java.util.stream.Collectors;
import static java.util.Arrays.stream;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/* spring security needs to know all the information about user , and he can do that by 'UserDetails' interface who contain methods
 * like getUsername(), getPassword(),...... we will create a constructor of User inside 'UserPrincipal' to give spring security all
 * the information of User
*/
public class UserPrincipal implements UserDetails {
	private static final long serialVersionUID = 1L;
	private User user;
	
	// every time when we want to create 'UserPrincipal' , we can pass user
    public UserPrincipal(User user) {
		this.user = user;
	}

    // these methods comes from spring security
    
    // authorities are permissions for User and Admin ex: User_Role(read) Admin_Role(read,delete,..)
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		/* we will get all authorities which is a String of array by 'this.user.getAuthorities()' 
		 * and mapped each one to new object of class 'SimpleGrantedAuthority'
		 * and collect it to the list by 'Collectors'
		 */
		return stream(this.user.getAuthorities()).map(SimpleGrantedAuthority::new).collect(Collectors.toList());  
	}
	

	@Override
	public String getPassword() {
		return this.user.getPassword();
	}

	@Override
	public String getUsername() {
		return this.user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.user.isNotLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return this.user.isActive();
	}

}
