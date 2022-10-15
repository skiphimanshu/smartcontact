package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepo;
import com.smart.entites.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepo userRepo;
	
	
	@RequestMapping("/")
	public String home(Model m) {
		m.addAttribute("title", "Home - Smart contact");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model m) {
		m.addAttribute("title", "About - Smart contact");
		return "about";
	}
	
	//Register 
	
	@RequestMapping("/signup")
	public String signup(Model m) {
		m.addAttribute("title", "Register - Smart contact");
		m.addAttribute("user", new User());
		return "signup";
	}
	
	// save user
	@RequestMapping(value = "/doRegister",method = RequestMethod.POST)
	public String doRegister(@Valid @ModelAttribute("user") User user,BindingResult result,@RequestParam(value = "agreement",defaultValue = "false") boolean agreement,
			Model m,HttpSession session)
	{	
		
		try {
			
			if(!agreement) {
				System.out.println("Please Check Term and condition");
				
				throw new Exception("Please Check Term and condition");
				
			}
			
			if(result.hasErrors()) {
				m.addAttribute("user", user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageurl("default.png");
			
			user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			
			System.out.println("agreement "+agreement);
			System.out.println("user"+user);
			
			User UserRes = this.userRepo.save(user);
			m.addAttribute("user", new User());
			session.setAttribute("message", new Message("Suceesfully Registerd Please Login","alert-success"));
			return "login";
			
			
		} catch (Exception e) {
			e.printStackTrace();
			m.addAttribute("user",user);
			session.setAttribute("message", new Message("Something Went Wrong","alert-danger"));
			
			
			return "signup";
		}	
		
	}
	
	// login 
	@RequestMapping(value = "/signin")
	public String login(Model m) {
		
		m.addAttribute("title", "Login - Smart Contact");
		return "login";
	}

	
	
	
}
