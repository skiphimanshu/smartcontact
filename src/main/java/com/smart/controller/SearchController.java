package com.smart.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.smart.dao.ContactRepo;
import com.smart.dao.UserRepo;
import com.smart.entites.Contact;
import com.smart.entites.User;

@RestController
public class SearchController {
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private ContactRepo contactRepo;
	
	@GetMapping("/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query,Principal principal) {
		
		System.out.println(query);
		User user = this.userRepo.getUserByUserName(principal.getName());
		List<Contact> contacts = this.contactRepo.findByNicknameContainingAndUser(query, user);
		
		return ResponseEntity.ok(contacts);
	}

}
