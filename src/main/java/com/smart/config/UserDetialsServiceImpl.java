package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smart.dao.UserRepo;
import com.smart.entites.User;

public class UserDetialsServiceImpl  implements UserDetailsService{
	
	@Autowired
	private UserRepo userRepo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		User user = this.userRepo.getUserByUserName(username);
		
		if(user==null) {
			throw new UsernameNotFoundException("Couldnot found user");
		}
		CustomUserdetails customUserdetails = new CustomUserdetails(user);	
		
		return customUserdetails;
	}

}
